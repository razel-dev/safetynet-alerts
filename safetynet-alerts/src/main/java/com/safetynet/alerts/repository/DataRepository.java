package com.safetynet.alerts.repository;

import com.safetynet.alerts.model.*;

import java.util.*;

/**
 * Contrat d'accès aux données de l'application (persons, medicalrecords, firestations).
 * <p>
 * Responsabilités :
 * <ul>
 *   <li>Initialisation du dépôt à partir du fichier data.json (via {@link DataSet}).</li>
 *   <li>Requêtes de lecture destinées aux endpoints de reporting.</li>
 *   <li>Opérations d'écriture (CRUD) pour les personnes, dossiers médicaux et le mapping adresse→caserne.</li>
 * </ul>
 * Conventions :
 * <ul>
 *   <li>L'identité métier d'une personne et d'un dossier médical est le couple (firstName, lastName).</li>
 *   <li>Les méthodes de lecture renvoient des collections vides plutôt que null. Les absences ponctuelles sont encapsulées dans {@link Optional}.</li>
 *   <li>{@link #findAllPersons()} doit retourner un « snapshot » défensif (copie) pour éviter toute modification externe de l'état interne.</li>
 * </ul>
 */
public interface DataRepository {

    // -------- Init depuis data.json --------

    /**
     * Initialise intégralement le dépôt à partir d'un {@link DataSet} chargé depuis data.json.
     * Cette opération doit remplacer l'état actuel et (ré)indexer les données pour les requêtes de lecture.
     *
     * @param dataSet jeu de données complet (persons, firestations, medicalrecords)
     */
    void init(DataSet dataSet);

    // -------- Lectures (reporting) --------

    /**
     * Retourne l'ensemble des adresses desservies par une caserne donnée.
     *
     * @param stationNumber numéro de caserne (ex. "1")
     * @return ensemble d'adresses desservies, jamais null (éventuellement vide)
     */
    Set<String> findAddressesByStation(String stationNumber);

    /**
     * Retourne la liste des habitants (tous âges) pour une adresse donnée.
     *
     * @param address adresse postale exacte
     * @return liste des personnes à cette adresse, jamais null (éventuellement vide)
     */
    List<Person> findPersonsByAddress(String address);

    /**
     * Retourne, si elle existe, la caserne associée à une adresse donnée.
     *
     * @param address adresse postale
     * @return numéro de caserne dans un {@link Optional}, vide si aucun mapping
     */
    Optional<String> findStationByAddress(String address);

    /**
     * Recherche le dossier médical d'une personne par identité (prénom/nom).
     *
     * @param firstName prénom
     * @param lastName  nom de famille
     * @return dossier médical dans un {@link Optional}, vide si introuvable
     */
    Optional<MedicalRecord> findMedicalRecord(String firstName, String lastName);

    /**
     * Liste les personnes portant un nom de famille donné.
     *
     * @param lastName nom de famille
     * @return liste des personnes correspondantes, jamais null (éventuellement vide)
     */
    List<Person> findPersonsByLastName(String lastName);

    /**
     * Retourne l'ensemble des emails (dédupliqués) pour une ville donnée.
     *
     * @param city ville
     * @return ensemble d'emails, jamais null (éventuellement vide)
     */
    Set<String> findEmailsByCity(String city);

    /**
     * Retourne un instantané défensif de toutes les personnes connues.
     * L'appelant ne doit pas pouvoir modifier l'état interne du dépôt via cette liste.
     *
     * @return copie de la liste des personnes, jamais null (éventuellement vide)
     */
    List<Person> findAllPersons();

    /**
     * Recherche une personne par identité (prénom/nom).
     *
     * @param firstName prénom
     * @param lastName  nom de famille
     * @return personne correspondante dans un {@link Optional}, vide si introuvable
     */
    Optional<Person> findPerson(String firstName, String lastName);

    // -------- Écritures (CRUD) --------

    /**
     * Crée ou met à jour une personne (remplacement complet sur la clé d'identité).
     * L'implémentation doit maintenir à jour tous les index dérivés (par adresse, nom, ville/email, etc.).
     *
     * @param person instance à persister
     */
    void savePerson(Person person);

    /**
     * Supprime une personne par identité (prénom/nom).
     * L'opération est idempotente si la personne n'existe pas.
     *
     * @param firstName prénom
     * @param lastName  nom de famille
     */
    void deletePerson(String firstName, String lastName);

    // MedicalRecord

    /**
     * Crée ou met à jour un dossier médical (remplacement complet sur la clé d'identité).
     *
     * @param mr dossier médical à persister
     */
    void saveMedicalRecord(MedicalRecord mr);

    /**
     * Supprime un dossier médical par identité (prénom/nom).
     * L'opération est idempotente si le dossier n'existe pas.
     *
     * @param firstName prénom
     * @param lastName  nom de famille
     */
    void deleteMedicalRecord(String firstName, String lastName);

    // Firestation mapping

    /**
     * Crée ou met à jour le mapping entre une adresse et un numéro de caserne.
     *
     * @param address adresse postale
     * @param station numéro de caserne (ex. "1")
     */
    void saveMapping(String address, String station);

    /**
     * Supprime le mapping caserne pour une adresse donnée.
     * L'opération est idempotente si aucun mapping n'existe.
     *
     * @param address adresse postale
     */
    void deleteMapping(String address);
}
