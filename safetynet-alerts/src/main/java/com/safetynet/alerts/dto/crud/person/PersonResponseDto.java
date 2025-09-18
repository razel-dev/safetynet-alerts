package com.safetynet.alerts.dto.crud.person;

/** Réponse standard après création/mise à jour d'une personne. */
public record PersonResponseDto(
        String firstName,
        String lastName,
        String address,
        String city,
        String zip,
        String phone,
        String email
) {}
