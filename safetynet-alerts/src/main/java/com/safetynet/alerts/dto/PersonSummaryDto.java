package com.safetynet.alerts.dto;

/** DTO "résumé" pour l'affichage liste avec l'essentiel */
public record PersonSummaryDto(
        String firstName,
        String lastName,
        String address,
        String phone
) {}