package com.safetynet.alerts.mapper.crud.medicalrecord;

import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordCreateDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordResponseDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordUpdateDto;
import com.safetynet.alerts.mapper.CentralMapperConfig;
import com.safetynet.alerts.model.MedicalRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface MedicalRecordMapper {
    MedicalRecord toEntity(MedicalRecordCreateDto dto);
    @BeanMapping
    void update(@MappingTarget MedicalRecord target, MedicalRecordUpdateDto dto); // PUT complet
    MedicalRecordResponseDto toResponse(MedicalRecord entity);
}