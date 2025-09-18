package com.safetynet.alerts.dto.crud.medicalrecord;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** Création d'un dossier médical. birthdate = "MM/dd/yyyy". */
public record MedicalRecordCreateDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String birthdate,
        List<String> medications,
        List<String> allergies
) {}
