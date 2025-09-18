package com.safetynet.alerts.dto.reporting;

import java.util.List;

public record FirestationCoverageDto(
        List<PersonSummaryDto> persons,
        int adults,
        int children
) {}