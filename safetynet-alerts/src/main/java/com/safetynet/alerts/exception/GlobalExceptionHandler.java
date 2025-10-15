package com.safetynet.alerts.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Conseiller global Spring MVC pour intercepter et transformer les exceptions
 * en réponses HTTP JSON cohérentes pour toutes les couches contrôleur.
 * Chaque méthode @ExceptionHandler ci-dessous s'occupe d'un type d'exception précis.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger disponible pour tracer les erreurs (peut être utilisé dans les handlers).
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Clés standardisées pour les réponses d'erreur
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ERROR = "error";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_PATH = "path";
    private static final String KEY_ERRORS = "errors";
    private static final String DEFAULT_UNEXPECTED_MESSAGE = "An unexpected error occurred";

    // Construit le corps de réponse d'erreur standardisé
    private Map<String, Object> buildBody(HttpStatus status, String errorLabel, String message, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(KEY_TIMESTAMP, LocalDateTime.now());
        body.put(KEY_STATUS, status.value());
        body.put(KEY_ERROR, errorLabel);
        body.put(KEY_MESSAGE, message);
        body.put(KEY_PATH, extractPath(request));
        return body;
    }

    // Extrait le path depuis la description WebRequest
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    @ExceptionHandler(ConflictExeption.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictExeption ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request));
    }

    @ExceptionHandler(NotFoundExeption.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundExeption ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request));
    }

    /**
     * 404 Not Found pour les cas où une ressource n'existe pas.
     * Typiquement levé côté service/répository via NoSuchElementException.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            NoSuchElementException ex, WebRequest request) {

        Map<String, Object> body = buildBody(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage(),
                request
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * 404 Not Found lorsque aucune route/méthode ne correspond (handler absent).
     * Remarque: Pour que Spring lève NoHandlerFoundException, il faut activer l'option dédiée
     * (par ex. spring.mvc.throw-exception-if-no-handler-found=true selon la configuration).
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {
        Map<String, Object> body = buildBody(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "Resource not found: " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                request
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * 400 Bad Request pour les erreurs de validation (@Valid, Jakarta Validation).
     * Agrège les erreurs champ -> message de violation pour un retour clair côté client.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, Object> body = buildBody(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Validation error",
                request
        );

        // Construction d'une map "champ -> message d'erreur"
        // En cas de collisions (même champ multiple fois), on conserve le premier message.
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        org.springframework.validation.FieldError::getField, // clé: nom du champ
                        error -> error.getDefaultMessage() != null           // valeur: message ou fallback
                                ? error.getDefaultMessage()
                                : "Invalid value",
                        (existing, replacement) -> existing                  // stratégie de merge
                ));

        body.put(KEY_ERRORS, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 500 Internal Server Error pour toute exception non explicitement gérée.
     * Sert de filet de sécurité afin de garantir une réponse JSON uniforme.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        // Journalisation centralisée des erreurs non gérées
        LOGGER.error("Unhandled exception on path {}: {}", extractPath(request), ex.getMessage(), ex);

        Map<String, Object> body = buildBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage() != null ? ex.getMessage() : DEFAULT_UNEXPECTED_MESSAGE,
                request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}