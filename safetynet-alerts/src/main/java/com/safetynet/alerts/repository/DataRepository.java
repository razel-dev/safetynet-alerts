package com.safetynet.alerts.repository;

import com.safetynet.alerts.model.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DataRepository {
  void init(DataSet dataSet);

  List<Person> findPersonsByAddress(String address);
  Set<String> findAddressesByStation(String stationNumber);
  Optional<String> findStationByAddress(String address);
  Optional<MedicalRecord> findMedicalRecord(String firstName, String lastName);
  List<Person> findAllPersons();
}
