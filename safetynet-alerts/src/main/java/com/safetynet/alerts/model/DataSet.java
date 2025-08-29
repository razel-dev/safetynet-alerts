package com.safetynet.alerts.model;

import java.util.List;

/**
 * Objet racine permettant de désérialiser le fichier data.json.
 *
 * Le JSON contient trois tableaux au premier niveau :
 * - persons : résidents et informations de contact
 * - firestations : correspondances adresse -> numéro de caserne
 * - medicalrecords : dates de naissance, médicaments et allergies
 *
 * Cette classe ne contient aucune logique métier ; elle sert uniquement de contenant
 * pour l’amorçage des données en mémoire.
 */
public class DataSet {

    private List<Person> persons;
    private List<FirestationMapping> firestations;
    private List<MedicalRecord> medicalrecords;

    /**
     * Retourne la liste des personnes lues depuis le JSON.
     * @return la liste des personnes
     */
    public List<Person> getPersons() { return persons; }

    /**
     * Définit la liste des personnes.
     * @param persons liste de personnes à affecter
     */
    public void setPersons(List<Person> persons) { this.persons = persons; }

    /**
     * Retourne la liste des correspondances adresse -> caserne.
     * @return la liste des mappings de caserne
     */
    public List<FirestationMapping> getFirestations() { return firestations; }

    /**
     * Définit la liste des correspondances adresse -> caserne.
     * @param firestations liste de mappings à affecter
     */
    public void setFirestations(List<FirestationMapping> firestations) { this.firestations = firestations; }

    /**
     * Retourne la liste des dossiers médicaux.
     * @return la liste des dossiers médicaux
     */
    public List<MedicalRecord> getMedicalrecords() { return medicalrecords; }

    /**
     * Définit la liste des dossiers médicaux.
     * @param medicalrecords liste des dossiers médicaux à affecter
     */
    public void setMedicalrecords(List<MedicalRecord> medicalrecords) { this.medicalrecords = medicalrecords; }
}
