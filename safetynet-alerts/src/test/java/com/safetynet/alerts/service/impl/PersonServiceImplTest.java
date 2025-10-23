package com.safetynet.alerts.service.impl;

import com.safetynet.alerts.dto.crud.person.PersonCreateDto;
import com.safetynet.alerts.dto.crud.person.PersonResponseDto;
import com.safetynet.alerts.dto.crud.person.PersonUpdateDto;
import com.safetynet.alerts.exception.ConflictExeption;
import com.safetynet.alerts.exception.NotFoundExeption;
import com.safetynet.alerts.mapper.crud.person.PersonMapper;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonServiceImplTest {

    private DataRepository repository;
    private PersonMapper mapper;
    private PersonServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(DataRepository.class); //Ils remplacent les dépendances réelles

        mapper = mock(PersonMapper.class);// Ils remplacent les dépendances réelles

        service = new PersonServiceImpl(repository, mapper); // objet reel testé
    }

    @Test
    void create_shouldPersistAndReturnResponse_whenPersonNotExists() {
        var dto = mock(PersonCreateDto.class);
        when(dto.firstName()).thenReturn("John");
        when(dto.lastName()).thenReturn("Doe");
        when(repository.findPerson("John", "Doe")).thenReturn(Optional.empty());

        var entity = mock(Person.class);
        when(mapper.toEntity(dto)).thenReturn(entity);

        var response = mock(PersonResponseDto.class);
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.create(dto);

        verify(repository).savePerson(entity);
        verify(mapper).toEntity(dto);
        verify(mapper).toResponse(entity);
        assertSame(response, result);
    }

    @Test
    void create_shouldThrowConflict_whenPersonAlreadyExists() {
        var dto = mock(PersonCreateDto.class);
        when(dto.firstName()).thenReturn("John");
        when(dto.lastName()).thenReturn("Doe");
        when(repository.findPerson("John", "Doe")).thenReturn(Optional.of(mock(Person.class)));

        assertThrows(ConflictExeption.class, () -> service.create(dto));

        verify(repository, never()).savePerson(any());
        verify(mapper, never()).toEntity(any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void update_shouldPersistAndReturnResponse_whenPersonExists() {
        var entity = mock(Person.class);
        when(repository.findPerson("John", "Doe")).thenReturn(Optional.of(entity));

        var dto = mock(PersonUpdateDto.class);

        var response = mock(PersonResponseDto.class);
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.update("John", "Doe", dto);

        verify(mapper).update(entity, dto);
        verify(repository).savePerson(entity);
        verify(mapper).toResponse(entity);
        assertSame(response, result);
    }

    @Test
    void update_shouldThrowNotFound_whenPersonMissing() {
        when(repository.findPerson("Jane", "Unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundExeption.class,
                () -> service.update("Jane", "Unknown", mock(PersonUpdateDto.class)));

        verify(repository, never()).savePerson(any());
        verify(mapper, never()).update(any(), any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void delete_shouldDelete_whenPersonExists() {
        when(repository.findPerson("John", "Doe")).thenReturn(Optional.of(mock(Person.class)));

        service.delete("John", "Doe");

        verify(repository).deletePerson("John", "Doe");
    }

    @Test
    void delete_shouldThrowNotFound_whenPersonMissing() {
        when(repository.findPerson("Jane", "Unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundExeption.class, () -> service.delete("Jane", "Unknown"));

        verify(repository, never()).deletePerson(anyString(), anyString());
    }
}