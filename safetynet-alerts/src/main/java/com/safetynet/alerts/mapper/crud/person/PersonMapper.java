package com.safetynet.alerts.mapper.crud.person;

import com.safetynet.alerts.dto.crud.person.PersonCreateDto;
import com.safetynet.alerts.dto.crud.person.PersonResponseDto;
import com.safetynet.alerts.dto.crud.person.PersonUpdateDto;
import com.safetynet.alerts.mapper.CentralMapperConfig;
import com.safetynet.alerts.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper MapStruct généré à la compilation (compile-time).
 *
 * - Conversions compile-time :
 *   MapStruct génère une implémentation concrète (sans réflexion), sûre et performante.
 *   Toute erreur de mapping est détectée à la compilation (unmappedTargetPolicy = ERROR dans la config centrale).
 *
 * - DTO immuables :
 *   Les DTO sont des records Java (lecture seule, pas de setters), garantissant des entrées stables.
 *
 * - Contrats clairs :
 *   - L'identité métier (firstName, lastName) n'est PAS modifiable via update(...).
 *   - Les valeurs nulles du DTO de mise à jour n'écrasent pas la cible (cf. stratégie nullValuePropertyMappingStrategy = IGNORE dans la config).
 */
@Mapper(config = CentralMapperConfig.class)
public interface PersonMapper {

    /**
     * Crée une nouvelle entité Person à partir d'un DTO de création immuable.
     * Conversion compile-time champ-à-champ (noms alignés).
     */
    Person toEntity(PersonCreateDto dto);

    /**
     * Met à jour une entité existante sans toucher à l'identité (firstName/lastName).
     * Contrat :
     *  - firstName / lastName : ignorés (non modifiables ici).
     *  - Valeurs nulles dans le DTO : ignorées (ne remplacent pas la valeur existante).
     */
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName",  ignore = true)
    void update(@MappingTarget Person target, PersonUpdateDto dto);

    /**
     * Transforme l'entité Person vers un DTO de réponse immuable.
     * Conversion compile-time champ-à-champ.
     */
    PersonResponseDto toResponse(Person entity);
}
