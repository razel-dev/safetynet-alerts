package com.safetynet.alerts.service.impl;

import com.safetynet.alerts.dto.crud.firestation.FirestationCreateDto;
import com.safetynet.alerts.dto.crud.firestation.FirestationResponseDto;
import com.safetynet.alerts.dto.crud.firestation.FirestationUpdateDto;
import com.safetynet.alerts.exception.ConflictExeption;
import com.safetynet.alerts.exception.NotFoundExeption;
import com.safetynet.alerts.mapper.crud.firestation.FirestationCrudMapper;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirestationMappingServiceImplTest {

    private DataRepository repository;
    private FirestationCrudMapper mapper;
    private FirestationMappingServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(DataRepository.class);
        mapper = mock(FirestationCrudMapper.class);
        service = new FirestationMappingServiceImpl(repository, mapper);
    }

    @Test
    void create_shouldPersistAndReturnResponse_whenAddressNotMapped() {
        var dto = new FirestationCreateDto("10 Downing St", "1");
        when(repository.findStationByAddress("10 Downing St")).thenReturn(Optional.empty());
        when(mapper.toResponse("10 Downing St", "1"))
                .thenReturn(new FirestationResponseDto("10 Downing St", "1"));

        var result = service.create(dto);

        verify(repository).saveMapping("10 Downing St", "1");
        assertEquals("10 Downing St", result.address());
        assertEquals("1", result.station());
    }

    @Test
    void create_shouldThrowConflict_whenAddressAlreadyMapped() {
        var dto = new FirestationCreateDto("10 Downing St", "1");
        when(repository.findStationByAddress("10 Downing St")).thenReturn(Optional.of("1"));

        assertThrows(ConflictExeption.class, () -> service.create(dto));
        verify(repository, never()).saveMapping(any(), any());
    }

    @Test
    void update_shouldPersistAndReturnResponse_whenAddressExists() {
        when(repository.findStationByAddress("10 Downing St")).thenReturn(Optional.of("1"));
        var dto = new FirestationUpdateDto("2");
        when(mapper.toResponse("10 Downing St", "2"))
                .thenReturn(new FirestationResponseDto("10 Downing St", "2"));

        var result = service.update("10 Downing St", dto);

        verify(repository).saveMapping("10 Downing St", "2");
        assertEquals("2", result.station());
    }

    @Test
    void update_shouldThrowNotFound_whenAddressMissing() {
        when(repository.findStationByAddress("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundExeption.class,
                () -> service.update("unknown", new FirestationUpdateDto("9")));
        verify(repository, never()).saveMapping(any(), any());
    }

    @Test
    void delete_shouldDelete_whenAddressExists() {
        when(repository.findStationByAddress("10 Downing St")).thenReturn(Optional.of("1"));

        service.delete("10 Downing St");

        verify(repository).deleteMapping("10 Downing St");
    }

    @Test
    void delete_shouldThrowNotFound_whenAddressMissing() {
        when(repository.findStationByAddress("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundExeption.class, () -> service.delete("unknown"));
        verify(repository, never()).deleteMapping(any());
    }
}