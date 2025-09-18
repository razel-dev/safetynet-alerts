package com.safetynet.alerts.dto.reporting;

import java.util.List;

public record ResidentMedicalDto(
        String firstName,
        String lastName,
        String phone,
        int age,
        List<String> medications,
        List<String> allergies
) {}