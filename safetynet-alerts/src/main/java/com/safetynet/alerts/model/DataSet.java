package com.safetynet.alerts.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenant des trois listes du JSON : persons, firestations, medicalrecords.
 * Aucune logique ; uniquement des getters/setters.
 */
@Getter
@Setter
public class DataSet {
    private List<Person> persons = new ArrayList<>();
    private List<FirestationMapping> firestations = new ArrayList<>();
    private List<MedicalRecord> medicalrecords = new ArrayList<>();

}
