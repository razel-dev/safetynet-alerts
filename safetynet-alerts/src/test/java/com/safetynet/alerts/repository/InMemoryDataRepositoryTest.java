package com.safetynet.alerts.repository;

import com.safetynet.alerts.model.DataSet;
import com.safetynet.alerts.model.FirestationMapping;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de l’implémentation en mémoire du dépôt {@link InMemoryDataRepository}.

 * Objectifs principaux validés par ces tests:
 *  Initialisation complète via {@link DataSet} (remplacement de l’état et indexation).
 * - Collections retournées non modifiables (instantanés défensifs).
 * - Cohérence des « index dérivés » après opérations d’écriture (CRUD) sur dossiers médicaux
 *   et mapping adresse → caserne.

 * Style: commentaires Arrange-Act-Assert pour la lisibilité.
 */
class InMemoryDataRepositoryTest {

    private InMemoryDataRepository repo;
    private DataSet ds;

    private Person johnDoe;
    private Person janeDoe;
    private Person aliceSmith;

    @BeforeEach // methode qui s'execute avant chaque test.
    void setUp() {
        // Arrange: on prépare un DataSet minimal mais représentatif
        repo = new InMemoryDataRepository();
        ds = new DataSet();

        // Données Persons (3 personnes avec adresses, villes, emails)
        johnDoe = new Person("John", "Doe", "1509 Culver St", "Culver", "97451", "111-111", "john@acme.org");
        janeDoe = new Person("Jane", "Doe", "29 15th St", "Culver", "97451", "222-222", "jane@acme.org");
        aliceSmith = new Person("Alice", "Smith", "1 Main St", "Spring", "11111", "333-333", "alice@acme.org");

        ds.setPersons(List.of(johnDoe, janeDoe, aliceSmith));

        // Données Firestations (mapping adresse → station)
        FirestationMapping m1 = new FirestationMapping();
        m1.setAddress("1509 Culver St");
        m1.setStation("1");
        FirestationMapping m2 = new FirestationMapping();
        m2.setAddress("29 15th St");
        m2.setStation("1");
        FirestationMapping m3 = new FirestationMapping();
        m3.setAddress("1 Main St");
        m3.setStation("2");
        ds.setFirestations(List.of(m1, m2, m3));

        // Données Medical records (dossiers médicaux existants)
        MedicalRecord mrJohn = new MedicalRecord();
        mrJohn.setFirstName("John");
        mrJohn.setLastName("Doe");
        MedicalRecord mrAlice = new MedicalRecord();
        mrAlice.setFirstName("Alice");
        mrAlice.setLastName("Smith");
        ds.setMedicalrecords(List.of(mrJohn, mrAlice));

        // Act: initialisation du dépôt à partir du DataSet
        repo.init(ds);

        // Assert implicite: les assertions détaillées sont vérifiées dans les méthodes de test dédiées
    }

    /**
     * Vérifie que l’initialisation charge les données et construit des collections non modifiables,
     * et que les recherches de base fonctionnent (adresses par station, station par adresse, etc.).
     */
    @Test
    void init() {
        // Assert: taille globale
        assertEquals(3, repo.findAllPersons().size());

        // Assert: liste défensive (non modifiable)
        List<Person> persons = repo.findAllPersons();
        assertThrows(UnsupportedOperationException.class, () -> persons.add(johnDoe));

        // Assert: lecture indexée par station et adresse (casse respectée mais recherche ensuite tolérante)
        assertEquals(Set.of("1509 Culver St", "29 15th St"), repo.findAddressesByStation("1"));
        assertEquals(Optional.of("2"), repo.findStationByAddress("1 Main St"));

        // Assert: recherche insensible à la casse
        assertEquals(2, repo.findPersonsByLastName("DOE").size());
        assertTrue(repo.findEmailsByCity("culver").containsAll(Set.of("john@acme.org", "jane@acme.org")));
    }

    /**
     * Vérifie la récupération des adresses pour un numéro de caserne donné, avec:
     * - tolérance aux espaces et casse,
     * - collections non modifiables,
     * - ensemble vide si la caserne n’existe pas ou si l’argument est null.
     */
    @Test
    void findAddressesByStation() {
        // Act
        Set<String> s1 = repo.findAddressesByStation("1");

        // Assert
        assertEquals(Set.of("1509 Culver St", "29 15th St"), s1);
        // Espaces superflus tolérés
        assertEquals(s1, repo.findAddressesByStation("  1 "));
        // Casernes inconnues / null → ensemble vide
        assertTrue(repo.findAddressesByStation("99").isEmpty());
        assertTrue(repo.findAddressesByStation(null).isEmpty());
        // Non modifiable
        assertThrows(UnsupportedOperationException.class, () -> s1.add("X"));
    }

    /**
     * Vérifie la recherche des personnes par adresse avec:
     * - insensibilité à la casse de l’adresse,
     * - collection non modifiable,
     * - liste vide pour adresse inconnue ou null.
     */
    @Test
    void findPersonsByAddress() {
        // Act
        List<Person> list = repo.findPersonsByAddress("1509 CULVER ST");

        // Assert
        assertEquals(1, list.size());
        assertEquals("John", list.getFirst().getFirstName());
        assertTrue(repo.findPersonsByAddress("unknown").isEmpty());
        assertTrue(repo.findPersonsByAddress(null).isEmpty());
        // Non modifiable
        assertThrows(UnsupportedOperationException.class, () -> list.add(janeDoe));
    }

    /**
     * Vérifie le mapping station ← adresse:
     * - retour via Optional,
     * - recherche insensible à la casse,
     * - Optional.empty() pour adresse inconnue ou null.
     */
    @Test
    void findStationByAddress() {
        // Assert
        assertEquals(Optional.of("1"), repo.findStationByAddress("29 15th St"));
        assertEquals(Optional.of("2"), repo.findStationByAddress("1 main st"));
        assertTrue(repo.findStationByAddress("unknown").isEmpty());
        assertTrue(repo.findStationByAddress(null).isEmpty());
    }

    /**
     * Vérifie la présence/absence de dossiers médicaux par identité,
     * avec insensibilité à la casse et gestion d’arguments null (vide).
     */
    @Test
    void findMedicalRecord() {
        // Assert
        assertTrue(repo.findMedicalRecord("john", "doe").isPresent());
        assertTrue(repo.findMedicalRecord("Alice", "Smith").isPresent());
        assertTrue(repo.findMedicalRecord("Jane", "Doe").isEmpty());
        // Arguments null → Optional.empty()
        assertTrue(repo.findMedicalRecord(null, "doe").isEmpty());
        assertTrue(repo.findMedicalRecord("john", null).isEmpty());
    }

    /**
     * Vérifie la recherche par nom de famille:
     * - insensibilité à la casse,
     * - collection non modifiable,
     * - liste vide pour nom inconnu ou null.
     */
    @Test
    void findPersonsByLastName() {
        // Act
        List<Person> does = repo.findPersonsByLastName("doe");

        // Assert
        assertEquals(2, does.size());
        assertTrue(does.stream().anyMatch(p -> p.getFirstName().equals("John")));
        assertTrue(does.stream().anyMatch(p -> p.getFirstName().equals("Jane")));
        assertTrue(repo.findPersonsByLastName("unknown").isEmpty());
        assertTrue(repo.findPersonsByLastName(null).isEmpty());
        // Non modifiable
        assertThrows(UnsupportedOperationException.class, () -> does.add(aliceSmith));
    }

    /**
     * Vérifie la récupération des emails par ville:
     * - déduplication (Set),
     * - insensibilité à la casse,
     * - Set non modifiable,
     * - ensemble vide pour ville inconnue ou null.
     */
    @Test
    void findEmailsByCity() {
        // Act
        Set<String> culverEmails = repo.findEmailsByCity("Culver");

        // Assert
        assertEquals(Set.of("john@acme.org", "jane@acme.org"), culverEmails);

        Set<String> springEmails = repo.findEmailsByCity("spring");
        assertEquals(Set.of("alice@acme.org"), springEmails);

        assertTrue(repo.findEmailsByCity("unknown").isEmpty());
        assertTrue(repo.findEmailsByCity(null).isEmpty());

        // Non modifiable
        assertThrows(UnsupportedOperationException.class, () -> culverEmails.add("x@y.z"));
    }

    /**
     * Vérifie que findAllPersons() retourne un « snapshot » immuable de toutes les personnes,
     * contenant bien les 3 personnes initialisées.
     */
    @Test
    void findAllPersons() {
        // Act
        List<Person> all = repo.findAllPersons();

        // Assert
        assertEquals(3, all.size());
        assertTrue(all.contains(johnDoe));
        assertTrue(all.contains(janeDoe));
        assertTrue(all.contains(aliceSmith));
        // Non modifiable
        assertThrows(UnsupportedOperationException.class, all::clear);
    }

    /**
     * Vérifie la recherche d’une personne par identité (prénom/nom),
     * insensible à la casse, et Optional.empty() pour identité inconnue.
     */
    @Test
    void findPerson() {
        // Assert
        assertTrue(repo.findPerson("john", "doe").isPresent());
        assertTrue(repo.findPerson("JOHN", "DOE").isPresent());
        assertEquals("John", repo.findPerson("john", "doe").orElseThrow().getFirstName());
        assertTrue(repo.findPerson("unknown", "person").isEmpty());
    }

    /**
     * Vérifie la création/mise à jour d’un dossier médical:
     * - ajout de Jane Doe (absente au départ),
     * - recherche ensuite présente, insensible à la casse.
     */
    @Test
    void saveMedicalRecord() {
        // Précondition
        assertTrue(repo.findMedicalRecord("john", "doe").isPresent());

        // Act
        MedicalRecord mrJane = new MedicalRecord();
        mrJane.setFirstName("Jane");
        mrJane.setLastName("Doe");
        repo.saveMedicalRecord(mrJane);

        // Assert
        assertTrue(repo.findMedicalRecord("jane", "doe").isPresent());
    }

    /**
     * Vérifie la suppression d’un dossier médical:
     * - Alice Smith présente au départ,
     * - suppression idempotente attendue,
     * - puis Optional.empty().
     */
    @Test
    void deleteMedicalRecord() {
        // Précondition
        assertTrue(repo.findMedicalRecord("alice", "smith").isPresent());

        // Act
        repo.deleteMedicalRecord("Alice", "Smith");

        // Assert
        assertTrue(repo.findMedicalRecord("alice", "smith").isEmpty());
    }

    /**
     * Vérifie la création/mise à jour du mapping adresse → caserne:
     * - modification d’une adresse existante (changement de station),
     * - création d’une nouvelle adresse,
     * - mise à jour cohérente des « index inverses » (adresses par station).
     */
    @Test
    void saveMapping() {
        // Préconditions
        assertEquals(Optional.of("2"), repo.findStationByAddress("1 Main St"));
        assertTrue(repo.findAddressesByStation("2").contains("1 Main St"));

        // Act: mutation du mapping
        repo.saveMapping("1 Main St", "3");

        // Assert: station et ensembles inverses mis à jour
        assertEquals(Optional.of("3"), repo.findStationByAddress("1 Main St"));
        assertFalse(repo.findAddressesByStation("2").contains("1 Main St"));
        assertTrue(repo.findAddressesByStation("3").contains("1 Main St"));

        // Act: création d’un nouveau mapping
        repo.saveMapping("10 Downing St", "4");

        // Assert
        assertEquals(Optional.of("4"), repo.findStationByAddress("10 Downing St"));
        assertTrue(repo.findAddressesByStation("4").contains("10 Downing St"));
    }

    /**
     * Vérifie la suppression du mapping adresse → caserne:
     * - l’adresse n’a plus de station associée,
     * - l’adresse disparaît de l’ensemble des adresses de l’ancienne station.
     */
    @Test
    void deleteMapping() {
        // Précondition
        assertEquals(Optional.of("1"), repo.findStationByAddress("1509 Culver St"));

        // Act
        repo.deleteMapping("1509 Culver St");

        // Assert
        assertTrue(repo.findStationByAddress("1509 Culver St").isEmpty());
        assertFalse(repo.findAddressesByStation("1").contains("1509 Culver St"));
    }
}