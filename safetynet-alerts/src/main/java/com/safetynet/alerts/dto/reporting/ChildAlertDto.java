package com.safetynet.alerts.dto.reporting;

import java.util.List;

public record ChildAlertDto(
        String firstName,
        String lastName,
        int age,
        List<PersonSummaryDto> householdMembers
) {}