package com.safetynet.alerts.dto;

public record PersonSummaryDto(
        String firstName,
        String lastName,
        String address,
        String phone
) {}