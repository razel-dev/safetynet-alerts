package com.safetynet.alerts.repository;

import com.safetynet.alerts.model.DataSet;
import com.safetynet.alerts.model.FirestationMapping;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryDataRepository implements DataRepository {

    // ---------- Index ----------
    private final Map<String, List<Person>>  personsByAddress         = new ConcurrentHashMap<>();
    private final Map<String, Set<String>>   addressesByStation       = new ConcurrentHashMap<>();
    private final Map<String, String>        stationByAddress         = new ConcurrentHashMap<>();
    private final Map<String, MedicalRecord> medicalRecordByPersonKey = new ConcurrentHashMap<>();


    private final Map<String, List<Person>>  personsByLastName        = new ConcurrentHashMap<>();
    private final Map<String, Set<String>>   emailsByCity             = new ConcurrentHashMap<>();

    // Snapshot logique de toutes les personnes
    private final List<Person> persons = new ArrayList<>();

    // -------------------- Helpers --------------------
    private static String norm(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
    private static String key(String first, String last) {
        return norm(first) + "|" + norm(last);
    }

    // -------------------- Init --------------------
    @Override
    public void init(DataSet dataSet) {
        Objects.requireNonNull(dataSet, "dataSet must not be null");

        // Reset complet (idempotence)
        personsByAddress.clear();
        addressesByStation.clear();
        stationByAddress.clear();
        medicalRecordByPersonKey.clear();
        personsByLastName.clear();
        emailsByCity.clear();
        persons.clear();

        // -------- Persons --------
        var ps = dataSet.getPersons();
        if (ps != null && !ps.isEmpty()) {
            // snapshot global
            persons.addAll(ps);

            // adresse -> personnes
            Map<String, List<Person>> byAddress =
                    ps.stream().collect(
                            Collectors.groupingBy(
                                    p -> norm(p.getAddress()),
                                    ConcurrentHashMap::new,
                                    Collectors.toList()
                            )
                    );
            personsByAddress.putAll(byAddress);

            // lastName -> personnes
            Map<String, List<Person>> byLastName =
                    ps.stream().collect(
                            Collectors.groupingBy(
                                    p -> norm(p.getLastName()),
                                    ConcurrentHashMap::new,
                                    Collectors.toList()
                            )
                    );
            personsByLastName.putAll(byLastName);

            // city -> emails
            Map<String, Set<String>> emailsByCityTmp =
                    ps.stream()
                            .filter(p -> p.getEmail() != null && !p.getEmail().isBlank())
                            .collect(
                                    Collectors.groupingBy(
                                            p -> norm(p.getCity()),
                                            ConcurrentHashMap::new,
                                            Collectors.mapping(
                                                    Person::getEmail,
                                                    Collectors.toCollection(ConcurrentHashMap::newKeySet)
                                            )
                                    )
                            );
            emailsByCity.putAll(emailsByCityTmp);
        }

        // -------- Firestations --------
        var fs = dataSet.getFirestations();
        if (fs != null && !fs.isEmpty()) {
            // station(norm) -> adresses (valeurs "raw" pour affichage)
            Map<String, Set<String>> addressesByStationTmp =
                    fs.stream().collect(
                            Collectors.groupingBy(
                                    m -> norm(m.getStation()),
                                    ConcurrentHashMap::new,
                                    Collectors.mapping(
                                            FirestationMapping::getAddress,
                                            Collectors.toCollection(ConcurrentHashMap::newKeySet)
                                    )
                            )
                    );
            addressesByStation.putAll(addressesByStationTmp);

            // adresse(norm) -> station(norm) (règle first-wins)
            Map<String, String> stationByAddressTmp =
                    fs.stream().collect(
                            Collectors.toMap(
                                    m -> norm(m.getAddress()),
                                    m -> norm(m.getStation()),
                                    (first, second) -> first,
                                    ConcurrentHashMap::new
                            )
                    );
            stationByAddress.putAll(stationByAddressTmp);
        }

        // -------- Medical records --------
        var mrs = dataSet.getMedicalrecords();
        if (mrs != null && !mrs.isEmpty()) {
            // (first|last) -> record (last-wins, équivalent d'un put)
            Map<String, MedicalRecord> mrByKey =
                    mrs.stream().collect(
                            Collectors.toMap(
                                    mr -> key(mr.getFirstName(), mr.getLastName()),
                                    mr -> mr,
                                    (a, b) -> b,                   // last-wins
                                    ConcurrentHashMap::new
                            )
                    );
            medicalRecordByPersonKey.putAll(mrByKey);
        }

        log.info("Repo init: persons={}, addresses={}, stations={}, records={}",
                persons.size(), personsByAddress.size(), addressesByStation.size(), medicalRecordByPersonKey.size());
    }

    // -------------------- Requêtes (contrat DataRepository) --------------------
    @Override
    public Set<String> findAddressesByStation(String stationNumber) {
        if (stationNumber == null) return Set.of();
        var set = addressesByStation.get(norm(stationNumber));
        return (set == null || set.isEmpty()) ? Set.of() : Set.copyOf(set);
    }

    @Override
    public List<Person> findPersonsByAddress(String address) {
        if (address == null) return List.of();
        var list = personsByAddress.get(norm(address));
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
        var list = personsByLastName.get(norm(lastName));
        return (list == null || list.isEmpty()) ? List.of() : List.copyOf(list);
    }

    @Override
    public Set<String> findEmailsByCity(String city) {
        if (city == null) return Set.of();
        var set = emailsByCity.get(norm(city));
        return (set == null || set.isEmpty()) ? Set.of() : Set.copyOf(set);
    }

    @Override
    public List<Person> findAllPersons() {
        return List.copyOf(persons);
    }
}
