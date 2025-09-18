package com.safetynet.alerts.mapper.reporting;

import com.safetynet.alerts.dto.reporting.PersonSummaryDto;
import com.safetynet.alerts.mapper.CentralMapperConfig;
import com.safetynet.alerts.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface SummaryMapper {
    @Mapping(target="firstName", source="firstName")
    @Mapping(target="lastName",  source="lastName")
    @Mapping(target="address",   source="address")
    @Mapping(target="phone",     source="phone")
    PersonSummaryDto toSummary(Person p);
}
