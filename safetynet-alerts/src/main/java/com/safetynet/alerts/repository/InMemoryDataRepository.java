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

    // Pour /personInfo & /communityEmail
    private final Map<String, List<Person>>  personsByLastName        = new ConcurrentHashMap<>();
    private final Map<String, Set<String>>   emailsByCity             = new ConcurrentHashMap<>();

    // Snapshot global de toutes les personnes
    private final List<Person> persons = new ArrayList<>();

    // -------------------- Helpers --------------------
    private static String norm(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
    private static String key(String first, String last) {
        return norm(first) + "|" + norm(last);
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
        persons.clear();

        // -------- Persons --------
        final List<Person> ps = dataSet.getPersons();
        if (ps != null && !ps.isEmpty()) {
            persons.addAll(ps);

            // adresse -> personnes
            Map<String, List<Person>> byAddress =
                    ps.stream().collect(Collectors.groupingBy(
                            p -> norm(p.getAddress()),
                            ConcurrentHashMap::new,
                            Collectors.toList()
                    ));
            personsByAddress.putAll(byAddress);


            // lastName -> personnes
            Map<String, List<Person>> byLastName =
                    ps.stream().collect(Collectors.groupingBy(
                            p -> norm(p.getLastName()),
                            ConcurrentHashMap::new,
                            Collectors.toList()
                    ));
            personsByLastName.putAll(byLastName);

            // city -> emails
            Map<String, Set<String>> emailsByCityTmp =
                    ps.stream()
                            .filter(p -> p.getEmail() != null )
                            .collect(Collectors.groupingBy(
                                    p -> norm(p.getCity()),
                                    ConcurrentHashMap::new,
                                    Collectors.mapping(Person::getEmail, Collectors.toCollection(ConcurrentHashMap::newKeySet))
                            ));

            emailsByCity.putAll(emailsByCityTmp);
        }
        // -------- Firestations --------
        final List<FirestationMapping> fs = dataSet.getFirestations();
        if (!fs.isEmpty()) {
            // station(norm) -> adresses
            Map<String, Set<String>> addressesByStationTmp =
                    fs.stream().collect(
                            Collectors.groupingBy(
                                    m -> norm(m.getStation()),
                                    Collectors.mapping(
                                            FirestationMapping::getAddress,
                                            Collectors.toSet()
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
                                    (first, second) -> first      // first-wins si doublon dans le JSON
                            )
                    );
            stationByAddress.putAll(stationByAddressTmp);
        }

        // -------- Medical records --------
        final List<MedicalRecord> mrs = dataSet.getMedicalrecords();
        if (!mrs.isEmpty()) {
            // (first|last) -> record (last-wins)
            Map<String, MedicalRecord> mrByKey =
                    mrs.stream().collect(
                            Collectors.toMap(
                                    mr -> key(mr.getFirstName(), mr.getLastName()),
                                    mr -> mr,
                                    (a, b) -> b                  // last-wins
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
}
