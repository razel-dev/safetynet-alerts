package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordCreateDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordResponseDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordUpdateDto;

public interface MedicalRecordService {
    MedicalRecordResponseDto create(MedicalRecordCreateDto dto);
    MedicalRecordResponseDto update(String firstName, String lastName, MedicalRecordUpdateDto dto);
    void delete(String firstName, String lastName);
}
