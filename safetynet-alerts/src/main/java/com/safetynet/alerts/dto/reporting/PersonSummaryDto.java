package com.safetynet.alerts.dto.reporting;

/** DTO "résumé" pour l'affichage liste avec l'essentiel */
public record PersonSummaryDto(
        String firstName,
        String lastName,
        String address,
        String phone
) {}