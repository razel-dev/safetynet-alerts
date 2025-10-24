package com.safetynet.alerts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.DataSet;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// ... existing code ...
/**
 * Tests unitaires de DataLoader sans démarrer de contexte Spring.
 * Stratégie:
 * - On instancie DataLoader manuellement avec ses dépendances (repo mocké, ObjectMapper réel ou mocké).
 * - On valide le scénario nominal (chargement et désérialisation du JSON → appel repo.init(DataSet)).
 * - On valide le scénario d'erreur (IOException Jackson → IllegalStateException, aucune interaction dépôt).
 */
class DataLoaderTest {

    /** Chemin de la ressource JSON utilisée par DataLoader. */
    private static final String DATA_PATH = "/data.json";

    /**
     * Vérifie que la ressource de test est bien présente sur le classpath.
     * Si absente, le test nominal échouerait pour une mauvaise raison (environnement de test),
     * d’où cette précondition explicite.
     */
    private static void assertTestDataResourcePresent() {
        try (InputStream is = DataLoaderTest.class.getResourceAsStream(DATA_PATH)) {
            assertThat(is)
                .as("La ressource doit être présente sur le classpath de test", DATA_PATH)
                .isNotNull();
        } catch (IOException e) {
            // Pas attendu pour un getResourceAsStream, mais on encapsule par prudence
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Chemin nominal: charge /data.json et appelle repo.init(...) avec le DataSet parsé")
    void load_ok_calls_repo_init_with_parsed_dataset() {
        // Arrange (préparation des dépendances)
        // - ObjectMapper réel pour une vraie désérialisation JSON
        // - DataRepository mocké pour vérifier l’interaction init(...)
        ObjectMapper om = new ObjectMapper();
        DataRepository repo = mock(DataRepository.class);
        assertTestDataResourcePresent(); // précondition: data.json doit exister

        // Act (exécution)
        DataLoader loader = new DataLoader(repo, om);
        loader.load(); // appelle la logique: ouvre /data.json, parse en DataSet, et repo.init(ds)

        // Assert (vérifications)
        // - Capture l’argument passé à init(...) pour valider le DataSet construit
        ArgumentCaptor<DataSet> captor = ArgumentCaptor.forClass(DataSet.class);
        verify(repo, times(1)).init(captor.capture());

        DataSet ds = captor.getValue();
        assertThat(ds).as("Le DataSet parsé ne doit pas être nul").isNotNull();
        assertThat(ds.getPersons()).as("La liste persons ne doit pas être nulle").isNotNull();
        assertThat(ds.getFirestations()).as("La liste firestations ne doit pas être nulle").isNotNull();
        assertThat(ds.getMedicalrecords()).as("La liste medicalrecords ne doit pas être nulle").isNotNull();
    }

    // ... existing code ...

    @Test
    @DisplayName("Erreur de désérialisation: IOException → IllegalStateException et aucune interaction dépôt")
    void load_ko_wraps_mapper_exception() throws Exception {
        // Arrange
        // - repo mocké
        // - ObjectMapper mocké pour simuler une IOException Jackson lors de readValue(...)
        DataRepository repo = mock(DataRepository.class);
        ObjectMapper om = mock(ObjectMapper.class);
        when(om.readValue(any(InputStream.class), eq(DataSet.class)))
            .thenThrow(new IOException("bad json"));

        // Act + Assert
        // - DataLoader doit encapsuler l’IOException en IllegalStateException
        DataLoader loader = new DataLoader(repo, om);
        assertThrows(IllegalStateException.class, loader::load);

        // - En cas d’échec de parsing, aucune interaction ne doit survenir avec le dépôt
        //   (on ne veut surtout pas d’init partiel)
        verifyNoInteractions(repo);
        // Variante plus ciblée si besoin: verify(repo, never()).init(any());
    }
}
