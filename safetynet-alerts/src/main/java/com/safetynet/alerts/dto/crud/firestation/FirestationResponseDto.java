package com.safetynet.alerts.dto.crud.firestation;

/** Réponse standard pour un mapping adresse ↔ station. */
public record FirestationResponseDto(
        String address,
        String station
) {}
