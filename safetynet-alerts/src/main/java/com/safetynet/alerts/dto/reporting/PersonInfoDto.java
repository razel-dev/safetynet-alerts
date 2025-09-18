package com.safetynet.alerts.dto.reporting;

import java.util.List;

public record PersonInfoDto(
        String firstName,
        String lastName,
        String address,
        int age,
        String email,
        List<String> medications,
        List<String> allergies
) {}