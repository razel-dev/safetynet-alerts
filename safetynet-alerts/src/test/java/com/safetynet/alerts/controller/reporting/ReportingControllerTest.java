package com.safetynet.alerts.controller.reporting;

import com.safetynet.alerts.service.ReportingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Remplace le bean par un mock Mockito (Spring Boot 3.2+)
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d’intégration « slice » du contrôleur ReportingController via MockMvc.
 *
 * <p>Caractéristiques:
 * <ul>
 *   <li>@WebMvcTest démarre un contexte Spring MVC « léger » (contrôleurs, conversion, validation…)
 *       sans la couche service/persistance. Pas de démarrage complet de l’application.</li>
 *   <li>{@link ReportingService} est mocké via {@link MockitoBean} afin d'isoler le contrôleur et
 *       piloter les retours sans dépendre des données réelles.</li>
 *   <li>{@link MockMvc} simule des requêtes HTTP et permet de vérifier le contrat HTTP
 *       (routes, paramètres requis, statut, content-type, payload JSON).</li>
 *   <li>Structure Arrange-Act-Assert (AAA) dans chaque test pour plus de lisibilité.</li>
 *   <li>Les 400 Bad Request reposent sur la validation automatique de Spring pour les @RequestParam requis:
 *       si un paramètre est absent, le contrôleur n’est pas invoqué et Spring renvoie 400.</li>
 * </ul>
 * </p>
 */
@WebMvcTest(ReportingController.class)
class ReportingControllerTest {

    /**
     * Client de test HTTP simulé pour piloter le contrôleur.
     * Fournit une API fluide pour construire la requête, exécuter et faire des assertions.
     */
    @Autowired
    private MockMvc mvc;

    /**
     * Service métier mocké, injecté dans le contrôleur testé.
     * Permet de contrôler les retours et de vérifier la délégation (verify(...)).
     */
    @MockitoBean
    private ReportingService reporting;

    @Test
    void firestation() throws Exception {
        // Arrange: aucun (paramètre requis non fourni)
        // NB: @RequestParam stationNumber est requis => Spring renverra 400 avant d'appeler le contrôleur/service.

        // Act
        mvc.perform(get("/firestation"))
           // Assert
           .andExpect(status().isBadRequest());
    }

    @Test
    void childAlert() throws Exception {
        // Arrange: aucun (paramètre requis non fourni)
        // NB: @RequestParam address est requis => 400 attendu.

        // Act
        mvc.perform(get("/childAlert"))
           // Assert
           .andExpect(status().isBadRequest());
    }

    @Test
    void phoneAlert() throws Exception {
        // Arrange
        // On stub la réponse du service. Set utilisé pour dédupliquer et ne pas imposer d’ordre.
        when(reporting.getPhonesByFirestation("2")).thenReturn(Set.of("111-222-3333", "444-555-6666"));

        // Act
        // Le paramètre HTTP s’appelle "firestation" mais est mappé sur stationNumber
        // via @RequestParam("firestation") dans le contrôleur.
        mvc.perform(get("/phoneAlert").param("firestation", "2"))
           // Assert
           .andExpect(status().isOk())
           // Compatible avec application/json (peut inclure un charset, ex: application/json;charset=UTF-8)
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           // Le corps est un tableau JSON
           .andExpect(jsonPath("$").isArray())
           // Vérifie la taille du tableau
           .andExpect(jsonPath("$.length()").value(2))
           // Vérifie la présence des valeurs sans dépendre de l’ordre (prédicat jsonPath)
           .andExpect(jsonPath("$[?(@=='111-222-3333')]").exists())
           .andExpect(jsonPath("$[?(@=='444-555-6666')]").exists());

        // Assert (interaction): le contrôleur a bien délégué au service avec l’argument attendu.
        verify(reporting).getPhonesByFirestation("2");
    }

    @Test
    void fire() throws Exception {
        // Arrange: aucun (paramètre requis non fourni)
        // NB: @RequestParam address est requis => 400 attendu.

        // Act
        mvc.perform(get("/fire"))
           // Assert
           .andExpect(status().isBadRequest());
    }

    @Test
    void flood() throws Exception {
        // Arrange: aucun (paramètre requis non fourni)
        // NB: @RequestParam stations (liste) est requis => 400 attendu.

        // Act
        mvc.perform(get("/flood/stations"))
           // Assert
           .andExpect(status().isBadRequest());
    }

    @Test
    void personInfo() throws Exception {
        // Arrange: aucun (paramètre requis non fourni)
        // NB: @RequestParam lastName est requis => 400 attendu.

        // Act
        mvc.perform(get("/personInfo"))
           // Assert
           .andExpect(status().isBadRequest());
    }

    @Test
    void communityEmail() throws Exception {
        // Arrange
        // On prépare le mock pour renvoyer deux emails pour la ville "Paris".
        when(reporting.getCommunityEmails("Paris")).thenReturn(Set.of("a@ex.com", "b@ex.com"));

        // Act
        mvc.perform(get("/communityEmail").param("city", "Paris"))
           // Assert
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$").isArray())
           .andExpect(jsonPath("$.length()").value(2))
           // On vérifie la présence des deux emails indépendamment de l’ordre.
           .andExpect(jsonPath("$[?(@=='a@ex.com')]").exists())
           .andExpect(jsonPath("$[?(@=='b@ex.com')]").exists());

        // Assert (interaction): vérifie la délégation.
        verify(reporting).getCommunityEmails("Paris");
    }
}
