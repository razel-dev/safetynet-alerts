package com.safetynet.alerts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.DataSet;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.SpringBootConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DataLoaderTest {

    // 1) Chemin nominal: charge /data.json, appelle repo.init(...)
    @Test
    void load_ok_calls_repo_init_with_parsed_dataset() {
        // Prépare les dépendances
        ObjectMapper om = new ObjectMapper();
        DataRepository repo = mock(DataRepository.class);

        // Sanity check: la ressource de test doit être présente dans src/test/resources
        try (InputStream is = getClass().getResourceAsStream("/data.json")) {
            assertThat(is).as("La ressource /data.json doit être présente pour le test nominal").isNotNull();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DataLoader loader = new DataLoader(repo, om);
        loader.load();

        // Vérifie que init a été appelé avec le DataSet
        ArgumentCaptor<DataSet> captor = ArgumentCaptor.forClass(DataSet.class);
        verify(repo).init(captor.capture());

        DataSet ds = captor.getValue();
        assertThat(ds).isNotNull();
        assertThat(ds.getPersons()).isNotNull();
        assertThat(ds.getFirestations()).isNotNull();
        assertThat(ds.getMedicalrecords()).isNotNull();
    }

    // 2) Erreur de désérialisation: lève IllegalStateException et n'appelle pas repo.init
    @Test
    void load_ko_wraps_mapper_exception() throws Exception {
        DataRepository repo = mock(DataRepository.class);
        ObjectMapper om = mock(ObjectMapper.class);
        when(om.readValue(any(InputStream.class), eq(DataSet.class)))
                .thenThrow(new IOException("bad json"));

        DataLoader loader = new DataLoader(repo, om);

        assertThrows(IllegalStateException.class, loader::load);
        verifyNoInteractions(repo);
    }


    // Configuration minimale pour le test de conditionnalité
    @SpringBootConfiguration
    @ComponentScan(basePackageClasses = DataLoader.class) // scanne le package contenant DataLoader
    static class App {
        @Configuration
        static class TestBeans {
            @Bean
            ObjectMapper objectMapper() {
                return new ObjectMapper();
            }

            @Bean
            DataRepository dataRepository() {
                return mock(DataRepository.class);
            }
        }
    }
}