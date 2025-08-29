package com.safetynet.alerts.repository;

import com.safetynet.alerts.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDataRepository implements DataRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryDataRepository.class);

    private final Map<String, List<Person>> addressToPersons = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> stationToAddresses = new ConcurrentHashMap<>();
    private final Map<String, String> addressToStation = new ConcurrentHashMap<>();
    private final Map<String, MedicalRecord> personKeyToRecord = new ConcurrentHashMap<>();
    private final List<Person> persons = new ArrayList<>();

    private static String key(String first, String last) {
        return (first + "|" + last).toLowerCase();
    }

    @Override
    public void init(DataSet dataSet) {
        persons.clear();
        addressToPersons.clear();
        stationToAddresses.clear();
        addressToStation.clear();
        personKeyToRecord.clear();

        // persons
        persons.addAll(dataSet.getPersons());
        dataSet.getPersons().forEach(p ->
                addressToPersons.computeIfAbsent(p.getAddress(), k -> new ArrayList<>()).add(p));

        // firestations (address <-> station)
        dataSet.getFirestations().forEach(fm -> {
            stationToAddresses.computeIfAbsent(fm.getStation(), k -> new HashSet<>()).add(fm.getAddress());
            addressToStation.putIfAbsent(fm.getAddress(), fm.getStation());
        });

        // medical records
        dataSet.getMedicalrecords().forEach(m ->
                personKeyToRecord.put(key(m.getFirstName(), m.getLastName()), m));

        log.info("Indexes built: persons={}, addresses={}, stations={}, medicalRecords={}",
                persons.size(), addressToPersons.size(), stationToAddresses.size(), personKeyToRecord.size());
    }

    @Override
    public List<Person> findPersonsByAddress(String address) {
        return addressToPersons.getOrDefault(address, List.of());
    }

    @Override
    public Set<String> findAddressesByStation(String stationNumber) {
        return stationToAddresses.getOrDefault(stationNumber, Set.of());
    }

    @Override
    public Optional<String> findStationByAddress(String address) {
        return Optional.ofNullable(addressToStation.get(address));
    }

    @Override
    public Optional<MedicalRecord> findMedicalRecord(String firstName, String lastName) {
        return Optional.ofNullable(personKeyToRecord.get(key(firstName, lastName)));
    }

    @Override
    public List<Person> findAllPersons() { return persons; }
}
