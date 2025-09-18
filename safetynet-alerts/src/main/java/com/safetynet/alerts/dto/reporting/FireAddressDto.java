package com.safetynet.alerts.dto.reporting;

import java.util.List;

public record FireAddressDto(
        String stationNumber,
        List<ResidentMedicalDto> residents
) {}