package com.safetynet.alerts.mapper.crud.medicalrecord;

import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordCreateDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordResponseDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordUpdateDto;
import com.safetynet.alerts.mapper.CentralMapperConfig;
import com.safetynet.alerts.model.MedicalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface MedicalRecordMapper {

    MedicalRecord toEntity(MedicalRecordCreateDto dto);

    // PUT complet : l’identité ne change pas
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName",  ignore = true)
    void update(@MappingTarget MedicalRecord target, MedicalRecordUpdateDto dto);

    MedicalRecordResponseDto toResponse(MedicalRecord entity);
}
