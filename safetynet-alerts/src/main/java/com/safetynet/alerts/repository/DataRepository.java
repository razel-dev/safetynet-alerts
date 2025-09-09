package com.safetynet.alerts.repository;

import com.safetynet.alerts.model.*;
import java.util.*;

public interface DataRepository {

    /** Initialise le dépôt depuis data.json (via DataSet). */
    void init(DataSet dataSet);

    /** Caserne → adresses desservies. */
    Set<String> findAddressesByStation(String stationNumber);

    /** Adresse → habitants (tous âges). */
    List<Person> findPersonsByAddress(String address);

    /** Adresse → n° de caserne (si mappée). */
    Optional<String> findStationByAddress(String address);

    /** (firstName,lastName) → dossier médical. */
    Optional<MedicalRecord> findMedicalRecord(String firstName, String lastName);

    /** Nom de famille → personnes (pour /personInfo). */
    List<Person> findPersonsByLastName(String lastName);

    /** Ville → emails dédupliqués (pour /communityEmail). */
    Set<String> findEmailsByCity(String city);

    /** Toutes personnes (snapshot défensif). */
    List<Person> findAllPersons();
}
