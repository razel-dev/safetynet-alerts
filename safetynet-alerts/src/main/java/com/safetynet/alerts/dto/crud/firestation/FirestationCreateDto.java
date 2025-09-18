package com.safetynet.alerts.dto.crud.firestation;

import jakarta.validation.constraints.NotBlank;

/** Création d'un mapping adresse ↔ station. */
public record FirestationCreateDto(
        @NotBlank String address,
        @NotBlank String station
) {}
