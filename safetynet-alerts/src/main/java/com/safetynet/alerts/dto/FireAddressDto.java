package com.safetynet.alerts.dto;

import java.util.List;

public record FireAddressDto(
        String stationNumber,
        List<ResidentMedicalDto> residents
) {}