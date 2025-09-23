package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.crud.firestation.FirestationCreateDto;
import com.safetynet.alerts.dto.crud.firestation.FirestationResponseDto;
import com.safetynet.alerts.dto.crud.firestation.FirestationUpdateDto;

public interface FirestationMappingService {
    FirestationResponseDto create(FirestationCreateDto dto);
    FirestationResponseDto update(String address, FirestationUpdateDto dto);
    void delete(String address);
}
