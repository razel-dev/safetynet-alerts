package com.safetynet.alerts.dto.crud.person;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Payload de mise à jour (PUT complet) d'une personne. */
public record PersonUpdateDto(
        @NotBlank String address,
        @NotBlank String city,
        @NotBlank String zip,
        @NotBlank String phone,
        @Email     String email
) {}
