package com.safetynet.alerts.service.impl;

import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordCreateDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordResponseDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordUpdateDto;
import com.safetynet.alerts.exception.ConflictExeption;
import com.safetynet.alerts.exception.NotFoundExeption;
import com.safetynet.alerts.mapper.crud.medicalrecord.MedicalRecordMapper;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalRecordServiceImplTest {

    private DataRepository repository;
    private MedicalRecordMapper mapper;
    private MedicalRecordServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(DataRepository.class);
        mapper = mock(MedicalRecordMapper.class);
        service = new MedicalRecordServiceImpl(repository, mapper);
    }

    @Test
    void create_shouldPersistAndReturnResponse_whenRecordNotExists() {
        var dto = mock(MedicalRecordCreateDto.class);
        when(dto.firstName()).thenReturn("John");
        when(dto.lastName()).thenReturn("Doe");
        when(repository.findMedicalRecord("John", "Doe")).thenReturn(Optional.empty());

        var entity = mock(MedicalRecord.class);
        when(mapper.toEntity(dto)).thenReturn(entity);

        var response = mock(MedicalRecordResponseDto.class);
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.create(dto);

        verify(repository).saveMedicalRecord(entity);
        verify(mapper).toEntity(dto);
        verify(mapper).toResponse(entity);
        assertSame(response, result);
    }

    @Test
    void create_shouldThrowConflict_whenRecordAlreadyExists() {
        var dto = mock(MedicalRecordCreateDto.class);
        when(dto.firstName()).thenReturn("John");
        when(dto.lastName()).thenReturn("Doe");
        when(repository.findMedicalRecord("John", "Doe"))
                .thenReturn(Optional.of(mock(MedicalRecord.class)));

        assertThrows(ConflictExeption.class, () -> service.create(dto));

        verify(repository, never()).saveMedicalRecord(any());
        verify(mapper, never()).toEntity(any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void update_shouldPersistAndReturnResponse_whenRecordExists() {
        var entity = mock(MedicalRecord.class);
        when(repository.findMedicalRecord("John", "Doe")).thenReturn(Optional.of(entity));

        var dto = mock(MedicalRecordUpdateDto.class);

        var response = mock(MedicalRecordResponseDto.class);
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.update("John", "Doe", dto);

        verify(mapper).update(entity, dto);
        verify(repository).saveMedicalRecord(entity);
        verify(mapper).toResponse(entity);
        assertSame(response, result);
    }

    @Test
    void update_shouldThrowNotFound_whenRecordMissing() {
        when(repository.findMedicalRecord("Jane", "Unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundExeption.class,
                () -> service.update("Jane", "Unknown", mock(MedicalRecordUpdateDto.class)));

        verify(repository, never()).saveMedicalRecord(any());
        verify(mapper, never()).update(any(), any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void delete_shouldDelete_whenRecordExists() {
        when(repository.findMedicalRecord("John", "Doe"))
                .thenReturn(Optional.of(mock(MedicalRecord.class)));

        service.delete("John", "Doe");

        verify(repository).deleteMedicalRecord("John", "Doe");
    }

    @Test
    void delete_shouldThrowNotFound_whenRecordMissing() {
        when(repository.findMedicalRecord("Jane", "Unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundExeption.class, () -> service.delete("Jane", "Unknown"));

        verify(repository, never()).deleteMedicalRecord(anyString(), anyString());
    }
}