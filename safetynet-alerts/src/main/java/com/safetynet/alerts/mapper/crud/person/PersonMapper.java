package com.safetynet.alerts.mapper.crud.person;

import com.safetynet.alerts.dto.crud.person.PersonCreateDto;
import com.safetynet.alerts.dto.crud.person.PersonResponseDto;
import com.safetynet.alerts.dto.crud.person.PersonUpdateDto;
import com.safetynet.alerts.mapper.CentralMapperConfig;
import com.safetynet.alerts.model.Person;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface PersonMapper {
    Person toEntity(PersonCreateDto dto);
    @BeanMapping
    void update(@MappingTarget Person target, PersonUpdateDto dto); // PUT complet
    PersonResponseDto toResponse(Person entity);
}
