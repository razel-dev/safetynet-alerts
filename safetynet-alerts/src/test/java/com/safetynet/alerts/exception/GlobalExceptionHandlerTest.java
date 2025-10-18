package com.safetynet.alerts.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// Remplacer l'ancien import incorrect par celui-ci:
import org.springframework.core.MethodParameter;

/**
 * Tests unitaires du {@link GlobalExceptionHandler}.
 * Objectif:
 * - Vérifier que chaque méthode annotée {@code @ExceptionHandler} retourne:
 *   - le bon code HTTP (status),
 *   - une structure de corps JSON standardisée (status, error, message, path),
 *   - des informations cohérentes avec l'exception reçue.
 * Stratégie:
 * - Appel direct des méthodes du handler (pas de couche web), avec:
 *   - des instances d'exceptions construites pour chaque cas,
 *   - un {@link WebRequest} mocké pour contrôler la valeur du chemin (clé "path").
 * Remarques:
 * - Ces tests ne couvrent pas l'intégration Spring MVC complète, mais garantissent le contrat
 *   de transformation exception -> réponse JSON du conseiller global.
 */
class GlobalExceptionHandlerTest {

    // Instance du handler à tester (tests unitaires directs des méthodes @ExceptionHandler)
    private GlobalExceptionHandler handler;
    // WebRequest mocké pour contrôler la valeur du path (via request.getDescription(false))
    private WebRequest request;

    /**
     * Préparation commune à chaque test.
     * - Instancie le handler
     * - Mocke un WebRequest dont la description fournit un "uri=" pour alimenter la clé "path".
     */
    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = Mockito.mock(WebRequest.class);
        // Simule une requête sur /test/path pour vérifier la clé "path" dans le body d'erreur
        when(request.getDescription(false)).thenReturn("uri=/test/path");
    }

    /**
     * Vérifie le mapping d'une {@link ConflictExeption} vers:
     * - HTTP 409 Conflict
     * - Corps JSON avec "error" = "Conflict" et le message de l'exception.
     */
    @Test
    void handleConflict() {
        // Arrange: exception fonctionnelle 409
        ConflictExeption ex = new ConflictExeption("Already exists");

        // Act
        ResponseEntity<Map<String, Object>> resp = handler.handleConflict(ex, request);

        // Assert: statut, structure standard et message
        assertEquals(409, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertEquals("Conflict", body.get("error"));
        assertEquals("Already exists", body.get("message"));
        assertEquals("/test/path", body.get("path"));
    }

    /**
     * Vérifie le mapping d'une {@link NotFoundExeption} vers:
     * - HTTP 404 Not Found
     * - Corps JSON avec "error" = "Not Found" et le message de l'exception.
     */
    @Test
    void handleNotFound() {
        // Arrange: exception fonctionnelle 404
        NotFoundExeption ex = new NotFoundExeption("Missing resource");

        // Act
        ResponseEntity<Map<String, Object>> resp = handler.handleNotFound(ex, request);

        // Assert
        assertEquals(404, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("Missing resource", body.get("message"));
        assertEquals("/test/path", body.get("path"));
    }

    /**
     * Vérifie le mapping d'une {@link NoSuchElementException} vers:
     * - HTTP 404 Not Found
     * - Corps JSON avec "error" = "Not Found" et le message de l'exception.
     */
    @Test
    void handleNotFoundException() {
        // Arrange: cas générique 404 (p.ex. NoSuchElement côté service)
        NoSuchElementException ex = new NoSuchElementException("No such element");

        // Act
        ResponseEntity<Map<String, Object>> resp = handler.handleNotFoundException(ex, request);

        // Assert
        assertEquals(404, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("No such element", body.get("message"));
        assertEquals("/test/path", body.get("path"));
    }

    /**
     * Vérifie le mapping d'une {@link NoHandlerFoundException} (route inexistante) vers:
     * - HTTP 404 Not Found
     * - Message formaté "Resource not found: VERBE URL".
     */
    @Test
    void handleNoHandlerFound() {
        // Arrange: 404 lorsque la route/méthode n'existe pas
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/no/route", new HttpHeaders());

        // Act
        ResponseEntity<Map<String, Object>> resp = handler.handleNoHandlerFound(ex, request);

        // Assert
        assertEquals(404, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        // Message formaté avec verbe + URL
        assertEquals("Resource not found: GET /no/route", body.get("message"));
        assertEquals("/test/path", body.get("path"));
    }

    /**
     * Méthode factice pour fabriquer un {@link MethodParameter} valide
     * (requis pour construire proprement une {@link MethodArgumentNotValidException}).
     *
     * @param arg paramètre fictif
     */
    @SuppressWarnings("unused")
    private void sampleMethod(String arg) {}

    /**
     * Vérifie le mapping d'une {@link MethodArgumentNotValidException} (erreurs de validation)
     * vers:
     * - HTTP 400 Bad Request
     * - Corps JSON avec "error" = "Validation Failed", "message" = "Validation error"
     * - Clé "errors" contenant la map champ -> message de violation.
     */
    @Test
    void handleValidationException() throws NoSuchMethodException {
        // Arrange:
        // - BindingResult réel avec deux erreurs de champ
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Object(), "dto");
        br.addError(new FieldError("dto", "firstName", "must not be blank"));
        br.addError(new FieldError("dto", "lastName", "size must be between 2 and 50"));

        // - MethodParameter pointant vers sampleMethod(String) (obligatoire pour construire l'exception)
        Method m = getClass().getDeclaredMethod("sampleMethod", String.class);
        MethodParameter mp = new MethodParameter(m, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, br);

        // Act
        ResponseEntity<Map<String, Object>> resp = handler.handleValidationException(ex, request);

        // Assert: statut + structure générique + agrégation "errors"
        assertEquals(400, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Validation Failed", body.get("error"));
        assertEquals("Validation error", body.get("message"));
        assertEquals("/test/path", body.get("path"));

        // Vérifie la map champ -> message
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertNotNull(errors);
        assertEquals("must not be blank", errors.get("firstName"));
        assertEquals("size must be between 2 and 50", errors.get("lastName"));
    }

    /**
     * Vérifie le filet de sécurité global:
     * - Exception non gérée explicitement -> HTTP 500 Internal Server Error
     * - Corps JSON avec "error" = "Internal Server Error" et le message de l'exception.
     */
    @Test
    void handleGlobalException() {
        // Arrange: exception non gérée explicitement -> 500
        Exception ex = new Exception("Unexpected boom");

        // Act
        ResponseEntity<Map<String, Object>> resp = handler.handleGlobalException(ex, request);

        // Assert
        assertEquals(500, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("Unexpected boom", body.get("message"));
        assertEquals("/test/path", body.get("path"));
    }

    /**
     * Vérifie le mapping d'un paramètre de requête manquant:
     * - {@link MissingServletRequestParameterException} -> HTTP 400 Bad Request
     * - Message standard Spring mentionnant le nom et le type du paramètre requis.
     */
    @Test
    void handleMissingParameter() throws Exception {
        // Arrange: paramètre requis absent -> 400
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("city", "String");

        // Act
        ResponseEntity<Map<String, Object>> resp = handler.handleMissingParameter(ex, request);

        // Assert
        assertEquals(400, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        // Le message standard Spring contient le nom et le type du paramètre manquant
        assertTrue(((String) body.get("message")).contains("Required request parameter 'city'"));
        assertEquals("/test/path", body.get("path"));
    }
}