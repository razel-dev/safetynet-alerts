package com.safetynet.alerts.dto.crud.medicalrecord;

import java.util.List;

/** Réponse standard pour dossier médical. */
public record MedicalRecordResponseDto(
        String firstName,
        String lastName,
        String birthdate,
        List<String> medications,
        List<String> allergies
) {}
