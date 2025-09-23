package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.crud.person.PersonCreateDto;
import com.safetynet.alerts.dto.crud.person.PersonResponseDto;
import com.safetynet.alerts.dto.crud.person.PersonUpdateDto;

public interface PersonService {
    PersonResponseDto create(PersonCreateDto dto);
    PersonResponseDto update(String firstName, String lastName, PersonUpdateDto dto);
    void delete(String firstName, String lastName);
}
