package com.safetynet.alerts.mapper.reporting;

import com.safetynet.alerts.dto.reporting.ResidentMedicalDto;
import com.safetynet.alerts.mapper.CentralMapperConfig;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.lang.Nullable;

@Mapper(
        config = CentralMapperConfig.class,
        imports = com.safetynet.alerts.time.AgeCalculator.class
)
public interface ResidentMapper {
    @Mapping(target="firstName", source="p.firstName")
    @Mapping(target="lastName",  source="p.lastName")
    @Mapping(target="phone",     source="p.phone")
    @Mapping(target="age",
            expression = "java(AgeCalculator.computeAge(rec != null ? rec.getBirthdate() : null))")
    @Mapping(target="medications", source="rec.medications")
    @Mapping(target="allergies",   source="rec.allergies")
    ResidentMedicalDto toResident(Person p, @Nullable MedicalRecord rec);
}
