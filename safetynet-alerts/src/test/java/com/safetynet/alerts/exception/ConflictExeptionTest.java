package com.safetynet.alerts.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test minimaliste: le message fourni au constructeur est exposé par getMessage().
  */
class ConflictExeptionTest {

    @Test
    void constructor_shouldExposeMessage() {
        ConflictExeption ex = new ConflictExeption("Already exists");
        assertEquals("Already exists", ex.getMessage());
        assertEquals("Already exists", ex.getLocalizedMessage());
    }
}