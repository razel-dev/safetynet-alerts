package com.safetynet.alerts.dto.crud.firestation;

import jakarta.validation.constraints.NotBlank;

/** Mise à jour (PUT) du mapping (l’adresse est fournie dans l’URL). */
public record FirestationUpdateDto(
        @NotBlank String station
) {}
