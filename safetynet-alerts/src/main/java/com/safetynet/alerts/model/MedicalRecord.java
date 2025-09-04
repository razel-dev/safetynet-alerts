package com.safetynet.alerts.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.util.ArrayList;
import java.util.List;


/**
 * Dossier médical tel que décrit dans le tableau "medicalrecords" de data.json.
 * Champs :
 *  - firstName : prénom
 *  - lastName  : nom
 *  - birthdate : date de naissance (format MM/dd/yyyy)
 *  - medications : liste des médicaments
 *  - allergies   : liste des allergies
 * Identité métier : (firstName, lastName)
 */

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"firstName","lastName"})

public class MedicalRecord {
    /** Prénom du titulaire du dossier. */
    private String firstName;
    /** Nom du titulaire du dossier. */
    private String lastName;
    /** Date de naissance au format MM/dd/yyyy. */
    private String birthdate;
    /** Liste des médicaments. */
    private List<String> medications = new ArrayList<>();
    /** Liste des allergies. */
    private List<String> allergies = new ArrayList<>();


    public MedicalRecord() {}


}