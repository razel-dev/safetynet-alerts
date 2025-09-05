package com.safetynet.alerts.config;                       // Déclare le package (emplacement logique de la classe)

import com.fasterxml.jackson.databind.ObjectMapper;       // Import Jackson pour convertir JSON ↔ objets Java
import com.safetynet.alerts.model.DataSet;                // Modèle racine qui correspond à la structure de data.json
import com.safetynet.alerts.repository.DataRepository;    // Contrat du dépôt en mémoire (stockage + index)
import jakarta.annotation.PostConstruct;                  // Annotation pour exécuter une méthode après l’injection des dépendances

import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Component;          // Marque la classe comme bean géré par Spring (détection de composants)

import java.io.InputStream;
import java.util.Collection;


/**
 * DataLoader
 * ---------------------------
 * - Au démarrage de l'application, charge le fichier "data.json" le convertit en DataSet, puis initialise le dépôt en mémoire.
 * - Objectif : disposer des données (pas de BDD ici, tout est en mémoire).
 */
@Slf4j
@Component

public class DataLoader {
    private final DataRepository repo;
    private final ObjectMapper om;
    private static final String PATH = "/data.json";

    public DataLoader(DataRepository repo, ObjectMapper om)
    { this.repo = repo; this.om = om; }

    @PostConstruct
    void load() {
        try (InputStream is = getClass().getResourceAsStream(PATH))
        {
            if (is == null) throw new IllegalStateException("data.json introuvable sur le classpath");
            DataSet ds = om.readValue(is, DataSet.class);
            repo.init(ds);
            log.info("Dataset chargé (persons={}, firestations={}, medicalrecords={})",
                    size(ds.getPersons()), size(ds.getFirestations()), size(ds.getMedicalrecords()));
        }

        catch (Exception e)
        {
            log.error("Échec chargement {}", PATH, e);
            throw new IllegalStateException("Impossible de charger " + PATH, e);
        }
    }

    private static int size(Collection<?> c)
    { return c == null ? 0 : c.size(); }
}