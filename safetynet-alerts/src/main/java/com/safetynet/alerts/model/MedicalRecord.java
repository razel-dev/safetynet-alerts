package com.safetynet.alerts.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dossier médical tel que décrit dans le tableau
 * medicalrecords du fichier data.json
 *  birthday MM/dd/yyyy</code> afin de respecter le format du fichier.
 *
 *
 * firstName — prénom
 * lastName — nom
 * birthdate — date de naissance (format MM/dd/yyyy)
 * medications— liste des médicaments
 * allergies— liste des allergies
  */

public class MedicalRecord {
    /** Prénom du titulaire du dossier. */
    private String firstName;
    /** Nom du titulaire du dossier. */
    private String lastName;


    /** Date de naissance au format MM/dd/yyyy. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private String birthdate;


    /** Liste des médicaments. */
    private List<String> medications = new ArrayList<>();
    /** Liste des allergies. */
    private List<String> allergies = new ArrayList<>();



    public MedicalRecord() {}
    /**
     List<String> medications, List<String> allergies) {
     this.firstName = firstName;
     this.lastName = lastName;
     this.birthdate = birthdate;
     if (medications != null) this.medications = medications;
     if (allergies != null) this.allergies = allergies;
     }

     /** @return le prénom */
    public String getFirstName() { return firstName; }
    /** @param firstName le prénom */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return le nom */
    public String getLastName() { return lastName; }
    /** @param lastName le nom */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return la date de naissance */
    public String getBirthdate() { return birthdate; }
    /** @param birthdate la date de naissance */
    public void String(String birthdate) { this.birthdate = birthdate; }


    /** @return la liste des médicaments */
    public List<String> getMedications() { return medications; }
    /** @param medications la liste des médicaments (remplacée par une liste vide si null) */
    public void setMedications(List<String> medications) {
        this.medications = (medications != null) ? medications : new ArrayList<>();
    }

    /** @return la liste des allergies */
    public List<String> getAllergies() { return allergies; }
    /** @param allergies la liste des allergies (remplacée par une liste vide si null) */
    public void setAllergies(List<String> allergies) {
        this.allergies = (allergies != null) ? allergies : new ArrayList<>();
    }

    /**
     * Identité basée sur le couple <code>firstName</code>/<code>lastName</code>.
     *
     * @param o autre objet à comparer
     * @return {@code true} si égaux, sinon {@code false}
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecord)) return false;

        return Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName);
    }

    /** @return hash basé sur firstName et lastName */
    @Override public int hashCode() { return Objects.hash(firstName, lastName); }

    /** @return représentation textuelle utile aux logs de debug */
    @Override public String toString() {
        return "MedicalRecord{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthdate=" + birthdate +
                ", medications=" + medications +
                ", allergies=" + allergies +
                '}';
    }
}