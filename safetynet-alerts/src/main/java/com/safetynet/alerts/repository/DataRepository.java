package com.safetynet.alerts.repository;

import com.safetynet.alerts.model.*;
import java.util.*;

public interface DataRepository {

    /** Initialise le dépôt depuis data.json (via DataSet). */
    void init(DataSet dataSet);

    /** Caserne et adresses desservies. */
    Set<String> findAddressesByStation(String stationNumber);

    /** Adresse et habitants (tous âges). */
    List<Person> findPersonsByAddress(String address);

    /** Adresse et n° de caserne (si mappée). */
    Optional<String> findStationByAddress(String address);

    /** (firstName,lastName) et dossier médical. */
    Optional<MedicalRecord> findMedicalRecord(String firstName, String lastName);

    /** Toutes personnes. */
    List<Person> findAllPersons();
}
