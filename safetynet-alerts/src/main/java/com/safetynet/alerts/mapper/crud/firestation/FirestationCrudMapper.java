package com.safetynet.alerts.mapper.crud.firestation;

import com.safetynet.alerts.dto.crud.firestation.FirestationResponseDto;
import com.safetynet.alerts.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface FirestationCrudMapper {
    default FirestationResponseDto toResponse(String address, String station) {
        return new FirestationResponseDto(address, station);
    }
}
