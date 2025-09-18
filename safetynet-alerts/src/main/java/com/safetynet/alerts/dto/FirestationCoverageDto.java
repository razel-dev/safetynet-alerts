package com.safetynet.alerts.dto;

import java.util.List;

public record FirestationCoverageDto(
        List<PersonSummaryDto> persons,
        int adults,
        int children
) {}