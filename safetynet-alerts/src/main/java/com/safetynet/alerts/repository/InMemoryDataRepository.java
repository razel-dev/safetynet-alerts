package com.safetynet.alerts.repository;

import com.safetynet.alerts.model.DataSet;
import com.safetynet.alerts.model.FirestationMapping;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryDataRepository implements DataRepository {

    // ---------- Index ----------
    private final ConcurrentMap<String, List<Person>>  personsByAddress         = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>>   addressesByStation       = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String>        stationByAddress         = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, MedicalRecord> medicalRecordByPersonKey = new ConcurrentHashMap<>();

    // Pour /personInfo & /communityEmail
    private final ConcurrentMap<String, List<Person>>  personsByLastName        = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>>   emailsByCity             = new ConcurrentHashMap<>();

    // Snapshot global & accès direct par identité
    private final List<Person> persons = new ArrayList<>();
    private final ConcurrentMap<String, Person> personsByKey = new ConcurrentHashMap<>();

    // -------------------- Helpers --------------------
    private static String norm(String s) { return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT); }
    private static String key(String first, String last) { return norm(first) + "|" + norm(last); }
    private static boolean samePerson(Person a, Person b) {
        return norm(a.getFirstName()).equals(norm(b.getFirstName()))
                && norm(a.getLastName()).equals(norm(b.getLastName()));
    }

    // ÉCRITURES -> COHÉRENCE DES INDEX
    // Cette méthode (ré)indexe une personne dans TOUS les index dérivés.
    // Invariant maintenu: après savePerson(), les vues par adresse, nom, ville/email et le snapshot global sont alignés.
    private void indexPerson(Person p) {
        // index principal
        personsByKey.put(key(p.getFirstName(), p.getLastName()), p);

        // adresse -> personnes
        personsByAddress.compute(norm(p.getAddress()), (addr, list) -> {
            if (list == null) list = new ArrayList<>();
            list.removeIf(old -> samePerson(old, p));
            list.add(p);
            return list;
        });

        // nom -> personnes
        personsByLastName.compute(norm(p.getLastName()), (ln, list) -> {
            if (list == null) list = new ArrayList<>();
            list.removeIf(old -> samePerson(old, p));
            list.add(p);
            return list;
        });

        // city -> emails
        if (p.getEmail() != null) {
            emailsByCity.compute(norm(p.getCity()), (c, set) -> {
                if (set == null) set = ConcurrentHashMap.newKeySet();
                set.add(p.getEmail());
                return set;
            });
        }

        // snapshot global
        persons.removeIf(old -> samePerson(old, p));
        persons.add(p);
    }

    private void deindexPerson(Person p) {
        personsByKey.remove(key(p.getFirstName(), p.getLastName()));

        personsByAddress.computeIfPresent(norm(p.getAddress()), (a, list) -> { list.removeIf(old -> samePerson(old, p)); return list; });
        personsByLastName.computeIfPresent(norm(p.getLastName()), (ln, list) -> { list.removeIf(old -> samePerson(old, p)); return list; });

        // recalcul léger des emails pour la ville concernée
        emailsByCity.compute(norm(p.getCity()), (c, set) -> {
            Set<String> recompute = persons.stream()
                    .filter(x -> norm(x.getCity()).equals(norm(p.getCity())))
                    .map(Person::getEmail).filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
            return recompute.isEmpty() ? null : recompute;
        });

        persons.removeIf(old -> samePerson(old, p));
    }

    // -------------------- Init (idempotent) --------------------
    @Override
    public void init(final DataSet dataSet) {
        Objects.requireNonNull(dataSet, "dataSet must not be null");

        // Reset
        personsByAddress.clear();
        addressesByStation.clear();
        stationByAddress.clear();
        medicalRecordByPersonKey.clear();
        personsByLastName.clear();
        emailsByCity.clear();
        personsByKey.clear();
        persons.clear();

        // -------- Persons --------
        final List<Person> ps = Optional.ofNullable(dataSet.getPersons()).orElseGet(List::of);
        if (!ps.isEmpty()) {
            persons.addAll(ps);
            ps.forEach(p -> personsByKey.put(key(p.getFirstName(), p.getLastName()), p));

            // adresse -> personnes
            personsByAddress.putAll(
                    (Map<? extends String, ? extends List<Person>>) ps.stream().collect(Collectors.groupingBy(
                            p -> norm(p.getAddress()),
                            ConcurrentHashMap::new,
                            Collectors.toList()))
            );

            // lastName -> personnes
            personsByLastName.putAll(
                    (Map<? extends String, ? extends List<Person>>) ps.stream().collect(Collectors.groupingBy(
                            p -> norm(p.getLastName()),
                            ConcurrentHashMap::new,
                            Collectors.toList()))
            );

            // city -> emails
            emailsByCity.putAll(
                    (Map<? extends String, ? extends Set<String>>) ps.stream()
                            .filter(p -> p.getEmail() != null)
                            .collect(Collectors.groupingBy(
                                    p -> norm(p.getCity()),
                                    ConcurrentHashMap::new,
                                    Collectors.mapping(Person::getEmail, Collectors.toCollection(ConcurrentHashMap::newKeySet))))
            );
        }

        // -------- Firestations --------
        final List<FirestationMapping> fs = Optional.ofNullable(dataSet.getFirestations()).orElseGet(List::of);
        if (!fs.isEmpty()) {
            // station -> adresses
            addressesByStation.putAll(
                    (Map<? extends String, ? extends Set<String>>) fs.stream().collect(Collectors.groupingBy(
                            m -> norm(m.getStation()),
                            ConcurrentHashMap::new,
                            Collectors.mapping(FirestationMapping::getAddress, Collectors.toCollection(ConcurrentHashMap::newKeySet))))
            );
            // adresse -> station (first-wins)
            stationByAddress.putAll(
                    fs.stream().collect(Collectors.toMap(
                            m -> norm(m.getAddress()),
                            m -> norm(m.getStation()),
                            (first, second) -> first))
            );
        }

        // -------- Medical records --------
        final List<MedicalRecord> mrs = Optional.ofNullable(dataSet.getMedicalrecords()).orElseGet(List::of);
        if (!mrs.isEmpty()) {
            medicalRecordByPersonKey.putAll(
                    mrs.stream().collect(Collectors.toMap(
                            mr -> key(mr.getFirstName(), mr.getLastName()),
                            mr -> mr,
                            (a, b) -> b)) // last-wins
            );
        }

        log.info("Repo init: persons={}, addresses={}, stations={}, records={}",
                persons.size(), personsByAddress.size(), addressesByStation.size(), medicalRecordByPersonKey.size());
    }

    // -------------------- Requêtes (lecture) --------------------
    @Override
    public Set<String> findAddressesByStation(String stationNumber) {
        if (stationNumber == null) return Set.of();
        Set<String> set = addressesByStation.get(norm(stationNumber));
        return (set == null || set.isEmpty()) ? Set.of() : Set.copyOf(set);
    }

    @Override
    public List<Person> findPersonsByAddress(String address) {
        if (address == null) return List.of();
        List<Person> list = personsByAddress.get(norm(address));
        return (list == null || list.isEmpty()) ? List.of() : List.copyOf(list);
    }

    @Override
    public Optional<String> findStationByAddress(String address) {
        if (address == null) return Optional.empty();
        return Optional.ofNullable(stationByAddress.get(norm(address)));
    }

    @Override
    public Optional<MedicalRecord> findMedicalRecord(String firstName, String lastName) {
        return Optional.ofNullable(medicalRecordByPersonKey.get(key(firstName, lastName)));
    }

    @Override
    public List<Person> findPersonsByLastName(String lastName) {
        if (lastName == null) return List.of();
        List<Person> list = personsByLastName.get(norm(lastName));
        return (list == null || list.isEmpty()) ? List.of() : List.copyOf(list);
    }

    @Override
    public Set<String> findEmailsByCity(String city) {
        if (city == null) return Set.of();
        Set<String> set = emailsByCity.get(norm(city));
        return (set == null || set.isEmpty()) ? Set.of() : Set.copyOf(set);
    }

    @Override
    public List<Person> findAllPersons() {
        return List.copyOf(persons);
    }

    @Override
    public Optional<Person> findPerson(String firstName, String lastName) {
        return Optional.ofNullable(personsByKey.get(key(firstName, lastName)));
    }

    // -------------------- Écritures (CRUD) --------------------
    // Person
    @Override
    public void savePerson(Person person) {
        // ÉCRITURE COHÉRENTE :
        // 1) si une version existe, on la retire de TOUS les index (deindexPerson),
        // 2) on (ré)indexe la nouvelle version dans TOUS les index (indexPerson).
        findPerson(person.getFirstName(), person.getLastName()).ifPresent(this::deindexPerson);
        indexPerson(person);
    }

    @Override
    public void deletePerson(String firstName, String lastName) {
        // ÉCRITURE COHÉRENTE (DELETE): on désindexe proprement partout si présent (idempotent).
        findPerson(firstName, lastName).ifPresent(this::deindexPerson);
    }

    // MedicalRecord
    @Override
    public void saveMedicalRecord(MedicalRecord mr) {
        // ÉCRITURE COHÉRENTE: clé logique "first|last" normalisée → remplacement complet.
        medicalRecordByPersonKey.put(key(mr.getFirstName(), mr.getLastName()), mr);
    }

    @Override
    public void deleteMedicalRecord(String firstName, String lastName) {
        medicalRecordByPersonKey.remove(key(firstName, lastName));
    }

    // Firestation mapping
    @Override
    public void saveMapping(String address, String station) {
        // ÉCRITURE COHÉRENTE DU MAPPING:
        // - met à jour stationByAddress[address],
        // - si la station a changé, enlève l'adresse de l'ancien ensemble addressesByStation[old],
        // - ajoute l'adresse dans le nouvel ensemble addressesByStation[new].
        final String a = norm(address);
        final String s = norm(station);

        // retirer ancienne station si elle change
        String previous = stationByAddress.put(a, s);
        if (previous != null && !previous.equals(s)) {
            addressesByStation.computeIfPresent(previous, (st, set) -> { set.remove(address); return set; });
        }
        // ajouter dans l’index inverse
        addressesByStation.compute(s, (st, set) -> {
            if (set == null) set = ConcurrentHashMap.newKeySet();
            set.add(address); // on conserve la casse d’origine en sortie
            return set;
        });
    }

    @Override
    public void deleteMapping(String address) {
        final String a = norm(address);
        String st = stationByAddress.remove(a);
        if (st != null) {
            addressesByStation.computeIfPresent(st, (key, set) -> { set.remove(address); return set; });
        }
    }
}
