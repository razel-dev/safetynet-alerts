package com.safetynet.alerts.dto.crud.medicalrecord;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** Mise à jour (PUT complet) d'un dossier médical. */
public record MedicalRecordUpdateDto(
        @NotBlank String birthdate,   // "MM/dd/yyyy"
        List<String> medications,
        List<String> allergies
) {}
