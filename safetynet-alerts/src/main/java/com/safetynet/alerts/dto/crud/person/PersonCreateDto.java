package com.safetynet.alerts.dto.crud.person;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Payload de création d'une personne. */
public record PersonCreateDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String address,
        @NotBlank String city,
        @NotBlank String zip,
        @NotBlank String phone,
        @Email     String email
) {}
