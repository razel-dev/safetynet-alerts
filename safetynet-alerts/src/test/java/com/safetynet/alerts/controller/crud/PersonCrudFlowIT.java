package com.safetynet.alerts.controller.crud;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration validant le flux CRUD complet de l'entité "Person".
 * Cette classe utilise MockMvc pour simuler des requêtes HTTP vers les endpoints REST
 * de l'application et vérifie le comportement de la chaîne créer-lire-mettre à jour-supprimer.
 
 * Scénario exécuté dans l'ordre suivant :
 * 1. Création d'une nouvelle Person via une requête POST.
 * 2. Lecture de la Person créée via une requête GET vers un endpoint de reporting.
 * 3. Mise à jour de la Person créée via une requête PUT.
 * 4. Vérification des valeurs mises à jour via une autre requête GET.
 * 5. Suppression de la Person via une requête DELETE.
 * 6. Vérification finale que la Person n'existe plus via un dernier GET.
 
 * Annotations :
 * - {@code @SpringBootTest} : indique qu'il s'agit d'un test Spring Boot, chargeant le contexte applicatif.
 * - {@code @AutoConfigureMockMvc} : active et configure MockMvc pour simuler la gestion requête/réponse.
 * - {@code @DirtiesContext} : réinitialise le contexte Spring après le test pour éviter les effets de bord.
 
 * Dépendances :
 * - MockMvc : permet d'émettre des requêtes HTTP simulées et de valider les réponses.
 
 * Ce test garantit le bon fonctionnement de l'API REST Person et
 * valide les réponses des endpoints (structure JSON, en-têtes HTTP, statuts).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext // remet le contexte propre après les tests (utile si on modifie l'état en mémoire : données en mémoire, caches, etc.)
class PersonCrudFlowIT {

    @Autowired MockMvc mvc; // Client HTTP simulé fourni par Spring Test pour appeler les endpoints sans serveur réel


    @Test
    void person_crud_flow_ok() throws Exception {
        // --- 1) CREATE ---
        String createJson = """
      {
        "firstName":"Jane","lastName":"Doe",
        "address":"10 Demo St","city":"Culver","zip":"97451",
        "phone":"841-000-0000","email":"jane@demo.com"
      }""";
        // Appel de création :
        // - contentType JSON pour indiquer le format du corps
        // - on vérifie 201 Created + l'en-tête Location pointant sur la ressource créée (convention REST)
        // - on contrôle quelques champs dans la réponse JSON
        mvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/person/Jane/Doe"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.address").value("10 Demo St"));
        // --- 2) READ (via un endpoint de reporting, ex: /personInfo) ---
        mvc.perform(get("/personInfo").param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[?(@.firstName=='Jane')]").exists());

        // --- 3) UPDATE ---
        // Corps JSON de mise à jour. L'identité (Jane/Doe) est passée dans l'URL (variables de chemin),
        // tandis que les nouvelles valeurs sont fournies dans le corps. Ici on fait une mise à jour "complète" de l'objet retourné.
        String updateJson = """
      {
        "firstName":"Jane","lastName":"Doe",
        "address":"11 Updated Ave","city":"Culver","zip":"97451",
        "phone":"841-111-2222","email":"jane.updated@demo.com"
      }""";
        // Appel de mise à jour :
        // - PUT /person/{firstName}/{lastName} avec le JSON mis à jour
        // - on s'attend à 200 OK et on vérifie les champs modifiés dans la réponse
        mvc.perform(put("/person/{firstName}/{lastName}", "Jane", "Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("11 Updated Ave"))
                .andExpect(jsonPath("$.phone").value("841-111-2222"));
        // --- 4) READ (vérif post-update) ---
        mvc.perform(get("/personInfo").param("lastName", "Doe"))
                .andExpect(status().isOk())
                // jsonPath sélectionne l'élément du tableau dont firstName == "Jane",
                // puis projette la propriété address ; on vérifie qu'elle a bien la nouvelle valeur.
                .andExpect(jsonPath("$[?(@.firstName=='Jane')].address").value("11 Updated Ave"));

        // --- 5) DELETE ---
        // Suppression de la ressource identifiée par {firstName}/{lastName}.
        // 204 No Content signifie suppression réussie sans corps de réponse.
        mvc.perform(delete("/person/{firstName}/{lastName}", "Jane", "Doe"))
                .andExpect(status().isNoContent());

        // --- 6) READ (après suppression -> ne doit plus être présent) ---
        mvc.perform(get("/personInfo").param("lastName", "Doe"))
                .andExpect(status().isOk())
                // On s'assure que l'élément "Jane" n'est plus présent dans la collection retournée.
                .andExpect(jsonPath("$[?(@.firstName=='Jane')]").doesNotExist());
    }
}