package com.safetynet.alerts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.DataSet;
import com.safetynet.alerts.repository.DataRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Composant d’amorçage qui charge le fichier data.json au démarrage,
 * le désérialise vers DataSet, puis initialise les index du dépôt en mémoire.
 *
 * Emplacement attendu : src/main/resources/data.json
 *
 */
@Component
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final DataRepository dataRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Crée un chargeur de données basé sur le dépôt fourni.
     * @param dataRepository dépôt à initialiser avec le contenu du jeu de données
     */
    public DataLoader(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * Charge le JSON et initialise les index du dépôt.
     * @throws IllegalStateException si le fichier est manquant ou illisible
     */
    @PostConstruct
    public void load() {
        log.info("Chargement de classpath:/data.json …");
        try (InputStream is = getClass().getResourceAsStream("/data.json")) {
            if (is == null) {
                throw new IllegalStateException("data.json introuvable sur le classpath (src/main/resources/data.json)");
            }
            DataSet dataSet = objectMapper.readValue(is, DataSet.class);
            dataRepository.init(dataSet);
            log.info("data.json chargé : persons={}, firestations={}, medicalrecords={}",
                    dataSet.getPersons().size(),
                    dataSet.getFirestations().size(),
                    dataSet.getMedicalrecords().size());
        } catch (Exception e) {
            log.error("Échec du chargement de data.json", e);
            throw new IllegalStateException("Impossible de charger data.json", e);
        }
    }
}
