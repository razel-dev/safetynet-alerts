package com.safetynet.alerts.service.impl;

import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordCreateDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordResponseDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordUpdateDto;
import com.safetynet.alerts.exception.ConflictExeption;
import com.safetynet.alerts.exception.NotFoundExeption;
import com.safetynet.alerts.mapper.crud.medicalrecord.MedicalRecordMapper;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.repository.DataRepository;
import com.safetynet.alerts.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor

public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final DataRepository repo;
    private final MedicalRecordMapper mrMapper;

    @Override
    public MedicalRecordResponseDto create(MedicalRecordCreateDto dto) {
        log.debug("[service] MR.create IN dto={}", dto);
        repo.findMedicalRecord(dto.firstName(), dto.lastName()).ifPresent(mr -> {
            throw new ConflictExeption("MedicalRecord already exists: " + dto.firstName() + " " + dto.lastName());
        });

        MedicalRecord entity = mrMapper.toEntity(dto);
        repo.saveMedicalRecord(entity);

        var out = mrMapper.toResponse(entity);
        log.info("[service] MR.create OUT id={}-{}", dto.firstName(), dto.lastName());
        return out;
    }

    @Override
    public MedicalRecordResponseDto update(String firstName, String lastName, MedicalRecordUpdateDto dto) {
        log.debug("[service] MR.update IN id={}-{} dto={}", firstName, lastName, dto);
        MedicalRecord entity = repo.findMedicalRecord(firstName, lastName)
                .orElseThrow(() -> new NotFoundExeption("MedicalRecord not found: " + firstName + " " + lastName));

        mrMapper.update(entity, dto);   // identité ignorée par le mapper
        repo.saveMedicalRecord(entity);

        var out = mrMapper.toResponse(entity);
        log.info("[service] MR.update OUT id={}-{}", firstName, lastName);
        return out;
    }

    @Override
    public void delete(String firstName, String lastName) {
        log.debug("[service] MR.delete IN id={}-{}", firstName, lastName);
        if (repo.findMedicalRecord(firstName, lastName).isEmpty()) {
            throw new NotFoundExeption("MedicalRecord not found: " + firstName + " " + lastName);
        }
        repo.deleteMedicalRecord(firstName, lastName);
        log.info("[service] MR.delete OUT id={}-{}", firstName, lastName);
    }
}
