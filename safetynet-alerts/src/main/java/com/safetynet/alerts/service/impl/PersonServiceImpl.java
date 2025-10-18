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

    private final DataRepository repo;// Port de persistance

    private final PersonMapper personMapper;

    @Override
    public PersonResponseDto create(PersonCreateDto dto) {
        log.debug("[service] Person.create IN dto={}", dto);
        // Orchestration règle métier #1 : unicité de l'identité
        // récupère prénom/nom du DTO et empêche la création d’un doublon en levant
        // une exception de conflit si l’identité existe
        ensurePersonNotExists(dto.firstName(), dto.lastName());

        // Mapping compile-time DTO -> Entity (transformation de l'objet DTO (type PersonCreatDTO ici) en un autre objet, l'intité métier Person.
        Person entity = personMapper.toEntity(dto);

        // Persistance : càd que l’objet métier (l’entité) est écrit dans un support durable
        repo.savePerson(entity);
        log.info("[service] Person.create OUT id={} {}", dto.firstName(), dto.lastName());

        // Mapping compile-time Entity -> DTO de réponse. - transforme l’entité métier Person en DTO de réponse PersonResponseDto, prêt à être renvoyé par l’API
        PersonResponseDto out = personMapper.toResponse(entity);
        log.info("[service] Person.create OUT id={} {}", dto.firstName(), dto.lastName());
        return out;
    }

    @Override
    public PersonResponseDto update(String firstName, String lastName, PersonUpdateDto dto) {
        log.debug("[service] Person.update IN id={}-{} dto={}", firstName, lastName, dto);
        // Orchestration règle métier #2 : existence préalable
        Person entity = loadExistingPerson(firstName, lastName);

        // Orchestration règle métier #3 : identité immuable (garantie par le mapper qui ignore first/last)
        personMapper.update(entity, dto);   // identité ignorée par le mapper
        repo.savePerson(entity);
        log.info("[service] Person.update OUT id={}-{}", firstName, lastName);

        PersonResponseDto out = personMapper.toResponse(entity);
        log.info("[service] Person.update OUT id={}-{}", firstName, lastName);
        return out;
    }

    @Override
    public void delete(String firstName, String lastName) {
        log.debug("[service] Person.delete IN id={}-{}", firstName, lastName);
        // Orchestration règle métier #2 (variante) : vérifier l'existence avant suppression
        ensurePersonExists(firstName, lastName);

        repo.deletePerson(firstName, lastName);
        log.info("[service] Person.delete OUT id={}-{}", firstName, lastName);
    }

    // ----- Méthodes privées d'orchestration des règles métier -----

    /**
     * Règle d'unicité : empêche la création si l'identité existe déjà.
     */
    private void ensurePersonNotExists(String firstName, String lastName) {
        repo.findPerson(firstName, lastName).ifPresent(p -> {
            throw new ConflictExeption("Person already exists: " + firstName + " " + lastName);
        });
    }

    /**
     * Règle d'existence : lève 404 si la personne est absente.
     */
    private void ensurePersonExists(String firstName, String lastName) {
        if (repo.findPerson(firstName, lastName).isEmpty()) {
            throw new NotFoundExeption("Person not found: " + firstName + " " + lastName);
        }
    }

    /**
     * Charge l'entité existante ou lève 404 (utile pour update).
     */
    private Person loadExistingPerson(String firstName, String lastName) {
        return repo.findPerson(firstName, lastName)
                .orElseThrow(() -> new NotFoundExeption("Person not found: " + firstName + " " + lastName));
    }
}
