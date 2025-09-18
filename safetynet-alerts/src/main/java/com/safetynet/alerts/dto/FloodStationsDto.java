package com.safetynet.alerts.dto;

import java.util.List;
import java.util.Map;

public record FloodStationsDto(
        Map<String, List<ResidentMedicalDto>> householdsByAddress
) {}