package com.safetynet.alerts.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test minimal: s'assure que le message passé au constructeur
 * est correctement exposé par getMessage()/getLocalizedMessage().
  */
class NotFoundExeptionTest {

    @Test
    void constructor_shouldExposeMessage() {
        NotFoundExeption ex = new NotFoundExeption("Resource not found");
        assertEquals("Resource not found", ex.getMessage());
        assertEquals("Resource not found", ex.getLocalizedMessage());
    }
}