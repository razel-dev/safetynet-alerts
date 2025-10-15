package com.safetynet.alerts.service.impl;

import com.safetynet.alerts.dto.crud.person.PersonCreateDto;
import com.safetynet.alerts.dto.crud.person.PersonResponseDto;
import com.safetynet.alerts.dto.crud.person.PersonUpdateDto;
import com.safetynet.alerts.exception.ConflictExeption;
import com.safetynet.alerts.exception.NotFoundExeption;
import com.safetynet.alerts.mapper.crud.person.PersonMapper;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.repository.DataRepository;
import com.safetynet.alerts.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor

public class PersonServiceImpl implements PersonService {

    private final DataRepository repo;
    private final PersonMapper personMapper;

    @Override
    public PersonResponseDto create(PersonCreateDto dto) {
        log.debug("[service] Person.create IN dto={}", dto);
        repo.findPerson(dto.firstName(), dto.lastName()).ifPresent(p -> {
            throw new ConflictExeption("Person already exists: " + dto.firstName() + " " + dto.lastName());
        });

        Person entity = personMapper.toEntity(dto);
        repo.savePerson(entity);

        var out = personMapper.toResponse(entity);
        log.info("[service] Person.create OUT id={} {}", dto.firstName(), dto.lastName());
        return out;
    }

    @Override
    public PersonResponseDto update(String firstName, String lastName, PersonUpdateDto dto) {
        log.debug("[service] Person.update IN id={}-{} dto={}", firstName, lastName, dto);
        Person entity = repo.findPerson(firstName, lastName)
                .orElseThrow(() -> new NotFoundExeption("Person not found: " + firstName + " " + lastName));

        personMapper.update(entity, dto);   // identité ignorée par le mapper
        repo.savePerson(entity);

        var out = personMapper.toResponse(entity);
        log.info("[service] Person.update OUT id={}-{}", firstName, lastName);
        return out;
    }

    @Override
    public void delete(String firstName, String lastName) {
        log.debug("[service] Person.delete IN id={}-{}", firstName, lastName);
        if (repo.findPerson(firstName, lastName).isEmpty()) {
            throw new NotFoundExeption("Person not found: " + firstName + " " + lastName);
        }
        repo.deletePerson(firstName, lastName);
        log.info("[service] Person.delete OUT id={}-{}", firstName, lastName);
    }
}
