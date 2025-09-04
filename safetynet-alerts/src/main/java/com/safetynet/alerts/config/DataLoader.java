package com.safetynet.alerts.config;                       // Déclare le package (emplacement logique de la classe)

import com.fasterxml.jackson.databind.ObjectMapper;       // Import Jackson pour convertir JSON ↔ objets Java
import com.safetynet.alerts.model.DataSet;                // Modèle racine qui correspond à la structure de data.json
import com.safetynet.alerts.repository.DataRepository;    // Contrat du dépôt en mémoire (stockage + index)
import jakarta.annotation.PostConstruct;                  // Annotation pour exécuter une méthode après l’injection des dépendances
import org.slf4j.Logger;                                  // Interface de logging (SLF4J)
import org.slf4j.LoggerFactory;                           // Fabrique de Logger (SLF4J)
import org.springframework.context.annotation.Profile;     // Permet d’activer/désactiver ce bean selon le profil Spring
import org.springframework.stereotype.Component;          // Marque la classe comme bean géré par Spring (détection de composants)

import java.io.InputStream;                               // Flux d’entrée (pour lire le fichier JSON du classpath)
import java.util.Collection;                              // Type générique Collection (pour size() utilitaire)

/**
 * DataLoader
 * ---------------------------
 * - Au démarrage de l'application, charge le fichier "data.json" , le convertit en DataSet, puis initialise le dépôt en mémoire.
 * - Objectif : disposer des données (pas de BDD ici, tout est en mémoire).
 */
@Component                                                  // Enregistre automatiquement ce composant dans le contexte Spring
@Profile("!test")                                           // Active ce bean sauf en profil 'test' (évite le chargement auto pendant les tests)
public class DataLoader {                                   // Début de la déclaration de classe

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class); // Logger type-safe pour cette classe

    private final DataRepository repository;                       // Dépendance vers le dépôt en mémoire (injectée par Spring)
    private final ObjectMapper objectMapper = new ObjectMapper(); // Instance Jackson simple (suffit pour ce projet)

    // Injection par constructeur : Spring instancie DataLoader en fournissant un DataRepository conforme
    public DataLoader(DataRepository repository) {          // Constructeur avec injection du dépôt
        this.repository = repository;                       // Affectation au champ privé
    }                                                       // Fin du constructeur

    /**
     * Méthode appelée automatiquement juste après la création du bean.
     * Étapes :
     *  1) Ouvre "data.json" depuis le classpath.
     *  2) Désérialise en DataSet avec Jackson.
     *  3) Initialise le dépôt en mémoire (index, caches...).
     *  4) Log des métriques de base.
     */
    @PostConstruct                                          // Indique à Spring d’exécuter load() après l’injection des dépendances
    void load() {                                           // Méthode d’amorçage (package-private suffit)
        log.info("Chargement de data.json …");              // Log de départ (niveau INFO)

        // getResourceAsStream("/data.json") :
        // - Le "/" signifie "à la racine du classpath".
        // - Maven place src/main/resources dans le classpath au build/exec.
        try (InputStream is = getClass().getResourceAsStream("/data.json")) { // Ouvre le flux vers le fichier packagé
            if (is == null) {                                // Si la ressource n’est pas trouvée…
                // Si le fichier n'est pas trouvé dans le JAR/classpath, on échoue immédiatement
                throw new IllegalStateException("data.json introuvable sur le classpath"); // Erreur bloquante
            }

            // Désérialisation du JSON complet vers notre modèle racine
            DataSet dataSet = objectMapper.readValue(is, DataSet.class); // Jackson lit le JSON et crée un DataSet

            // Initialisation du dépôt : création des structures en mémoire (listes, maps, index...)
            repository.init(dataSet);                        // On confie les données au dépôt pour indexation

            // Petit récap en log (tailles des 3 collections principales)
            log.info("OK - persons={}, firestations={}, medicalrecords={}", // Résumé des éléments chargés
                    size(dataSet.getPersons()),             // Nombre de personnes
                    size(dataSet.getFirestations()),        // Nombre de mappings firestation
                    size(dataSet.getMedicalrecords()));     // Nombre de dossiers médicaux
        } catch (Exception e) {                             // Capture toute exception (I/O, JSON mal formé, etc.)
            // En cas d'erreur (fichier absent, JSON invalide, etc.), on trace et on stoppe le boot proprement
            log.error("Échec du chargement de data.json", e); // Log d’erreur avec stacktrace
            throw new IllegalStateException("Impossible de charger data.json", e); // On remonte une IllegalStateException
        }                                                   // Fin du try-with-resources (ferme InputStream automatiquement)
    }                                                       // Fin de la méthode load()

    // Utilitaire : évite un NullPointerException si une collection est null
    private static int size(Collection<?> c) {              // Méthode utilitaire pour obtenir la taille d’une collection
        return c == null ? 0 : c.size();                    // Retourne 0 si null, sinon la taille réelle
    }                                                       // Fin de size()
}                                                           // Fin de la classe DataLoader
