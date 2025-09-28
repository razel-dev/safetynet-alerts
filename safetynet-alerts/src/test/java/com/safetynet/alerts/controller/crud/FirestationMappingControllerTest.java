package com.safetynet.alerts.controller.crud;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.dto.crud.firestation.FirestationResponseDto;
import com.safetynet.alerts.service.FirestationMappingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * Tests d’intégration du contrôleur FirestationMappingController à l’aide de MockMvc dans un contexte Spring Boot.
 * Ces tests vérifient que la couche web interagit correctement avec la couche service et respecte le contrat de l’API.

 * Outils utilisés :
 * - MockMvc : simule les requêtes HTTP et valide les réponses.
 * - ObjectMapper : sérialise/désérialise les payloads de requête et de réponse.
 * - FirestationMappingService : service mocké pour isoler et tester le comportement du contrôleur.

 * Cas de test :
 * - create : vérifie la création d’un mapping adresse ↔ station via l’endpoint HTTP POST ; contrôle le statut,
 *   l’en-tête Location et le payload JSON (address, station).

 * - update : vérifie la mise à jour d’un mapping existant via l’endpoint HTTP PUT ; contrôle le statut,
 *   le payload JSON et la station mise à jour dans la réponse.

 * - deleteTest : vérifie la suppression d’un mapping via l’endpoint HTTP DELETE ; contrôle que le code statut
 *   renvoyé est correct lorsque la suppression réussit.
 */
// Test d’intégration Web avec MockMvc sur le contexte complet
@SpringBootTest
@AutoConfigureMockMvc
class FirestationMappingControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean
    FirestationMappingService service; // si vous voulez toujours isoler la couche service

    @Test
    void create() throws Exception {
        // Arrange — données d’entrée pour la création
        String address = "1509 Culver St";
        String station = "1";

        // Arrange — on stub la couche service : peu importe le DTO reçu (any()),
        // la méthode service.create(...) retournera une réponse contenant l’adresse et la station ci-dessus.
        // Cela permet d’isoler le test au seul comportement du contrôleur (statut, en-têtes, JSON).
        Mockito.when(service.create(any()))
                .thenReturn(new FirestationResponseDto(address, station));

        // Arrange — corps JSON de la requête (simule le payload envoyé par un client)
        String body = objectMapper.writeValueAsString(
                Map.of("address", address, "station", station)
        );

        // Arrange — valeur attendue de l’en-tête Location (construite comme dans le contrôleur, avec encodage)
        String expectedLocation = org.springframework.web.util.UriComponentsBuilder
                .fromPath("/firestation/{address}")
                .buildAndExpand(address)
                .encode()
                .toUriString();

        // Act — on envoie une requête POST vers /firestation avec le JSON ci-dessus
        // Assert — on vérifie :
        //  - 201 Created (bonne sémantique REST pour une création)
        //  - Présence et exactitude de l’en-tête Location
        //  - Type de contenu JSON
        //  - Corps JSON conforme à la DTO renvoyée par le service (address et station)
        mockMvc.perform(post("/firestation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", expectedLocation))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.address").value(address))
                .andExpect(jsonPath("$.station").value(station));
    }
    // ... existing code ...
    @Test
    void update() throws Exception {
        // Arrange — adresse ciblée par l’URL et nouvelle station à affecter
        String address = "1509 Culver St";
        String newStation = "2";

        // Arrange — on stub la mise à jour :
        //  - eq(address) s’assure que le contrôleur passe bien l’adresse de l’URL au service.
        //  - any() pour le DTO de mise à jour (on n’asserte pas ici son contenu exact, seulement le flux).
        // La réponse simulée contient la station mise à jour (newStation).
        Mockito.when(service.update(eq(address), any()))
                .thenReturn(new FirestationResponseDto(address, newStation));

        // Arrange — corps JSON ne contient que la station (l’adresse est fournie dans l’URL)
        String body = objectMapper.writeValueAsString(
                Map.of("station", newStation)
        );

        // Act — requête PUT sur /firestation/{address}
        // Assert — on vérifie :
        //  - 200 OK (mise à jour réussie)
        //  - content-type JSON
        //  - le JSON renvoyé reflète bien l’adresse cible et la nouvelle station
        mockMvc.perform(put("/firestation/{address}", address)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.address").value(address))
                .andExpect(jsonPath("$.station").value(newStation));
    }
    // ... existing code ...
    @Test
    void  deleteTest
            () throws Exception {
        // Arrange — adresse à supprimer
        String address = "1509 Culver St";

        // Arrange — on stub la méthode void delete(...) pour ne rien faire (doNothing),
        // ce qui simule une suppression réussie côté service (aucune exception levée).
        Mockito.doNothing().when(service).delete(address);

        // Act — requête DELETE vers /firestation/{address}
        // Assert — on attend 204 No Content (suppression réussie, pas de corps de réponse)
        mockMvc.perform(delete("/firestation/{address}", address))
                .andExpect(status().isNoContent());
    }
}