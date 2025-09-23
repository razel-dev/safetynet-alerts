package com.safetynet.alerts.service.impl;

import com.safetynet.alerts.dto.reporting.ChildAlertDto;
import com.safetynet.alerts.dto.reporting.FireAddressDto;
import com.safetynet.alerts.dto.reporting.FirestationCoverageDto;
import com.safetynet.alerts.dto.reporting.PersonInfoDto;
import com.safetynet.alerts.dto.reporting.PersonSummaryDto;
import com.safetynet.alerts.dto.reporting.ResidentMedicalDto;
import com.safetynet.alerts.mapper.reporting.PersonInfoMapper;
import com.safetynet.alerts.mapper.reporting.ResidentMapper;
import com.safetynet.alerts.mapper.reporting.SummaryMapper;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ReportingServiceImplTest {

    private DataRepository repo;
    private SummaryMapper summaryMapper;
    private ResidentMapper residentMapper;
    private PersonInfoMapper personInfoMapper;
    private ReportingServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(DataRepository.class);
        summaryMapper = mock(SummaryMapper.class);
        residentMapper  = mock(ResidentMapper.class);
        personInfoMapper = mock(PersonInfoMapper.class);
        service = new ReportingServiceImpl(repo, summaryMapper, residentMapper, personInfoMapper);
    }



    @Test
    void getPersonsByStation() {
        when(repo.findAddressesByStation("2")).thenReturn(Set.of("A1"));
        Person pAdult = mock(Person.class);
        when(pAdult.getFirstName()).thenReturn("John");
        when(pAdult.getLastName()).thenReturn("Doe");
        Person pChild = mock(Person.class);
        when(pChild.getFirstName()).thenReturn("Jane");
        when(pChild.getLastName()).thenReturn("Doe");
        when(repo.findPersonsByAddress("A1")).thenReturn(List.of(pAdult, pChild));

        MedicalRecord rAdult = mock(MedicalRecord.class);
        when(rAdult.getBirthdate()).thenReturn("01/01/1980"); // adulte
        MedicalRecord rChild = mock(MedicalRecord.class);
        when(rChild.getBirthdate()).thenReturn("01/01/2015"); // enfant
        when(repo.findMedicalRecord("John", "Doe")).thenReturn(Optional.of(rAdult));
        when(repo.findMedicalRecord("Jane", "Doe")).thenReturn(Optional.of(rChild));

        PersonSummaryDto sAdult = new PersonSummaryDto("John", "Doe", "A1", "111");
        PersonSummaryDto sChild = new PersonSummaryDto("Jane", "Doe", "A1", "222");
        when(summaryMapper.toSummary(pAdult)).thenReturn(sAdult);
        when(summaryMapper.toSummary(pChild)).thenReturn(sChild);

        FirestationCoverageDto out = service.getPersonsByStation("2");

        assertNotNull(out);
        assertEquals(1, out.children());
        assertEquals(1, out.adults());
        assertEquals(List.of(sAdult, sChild), out.persons());
    }

    @Test
    void getChildAlert() {
        String address = "1509 Culver St";
        Person pChild = mock(Person.class);
        when(pChild.getFirstName()).thenReturn("Tenley");
        when(pChild.getLastName()).thenReturn("Boyd");
        Person pAdult = mock(Person.class);
        when(pAdult.getFirstName()).thenReturn("John");
        when(pAdult.getLastName()).thenReturn("Boyd");
        when(repo.findPersonsByAddress(address)).thenReturn(List.of(pChild, pAdult));

        MedicalRecord rChild = mock(MedicalRecord.class);
        when(rChild.getBirthdate()).thenReturn("01/01/2015"); // enfant
        MedicalRecord rAdult = mock(MedicalRecord.class);
        when(rAdult.getBirthdate()).thenReturn("01/01/1980"); // adulte
        when(repo.findMedicalRecord("Tenley", "Boyd")).thenReturn(Optional.of(rChild));
        when(repo.findMedicalRecord("John", "Boyd")).thenReturn(Optional.of(rAdult));

        PersonSummaryDto sChild = new PersonSummaryDto("Tenley", "Boyd", address, "111");
        PersonSummaryDto sAdult = new PersonSummaryDto("John", "Boyd", address, "222");
        when(summaryMapper.toSummary(pChild)).thenReturn(sChild);
        when(summaryMapper.toSummary(pAdult)).thenReturn(sAdult);

        List<ChildAlertDto> out = service.getChildAlert(address);

        assertEquals(1, out.size());
        ChildAlertDto dto = out.getFirst();
        assertEquals("Tenley", dto.firstName());
        assertEquals("Boyd", dto.lastName());
        assertTrue(dto.age() >= 0 && dto.age() <= 18);
        assertEquals(List.of(sAdult),dto.householdMembers()); // les autres membres du foyer
    }

    @Test
    void getPhonesByFirestation() {
        when(repo.findAddressesByStation("3")).thenReturn(Set.of("A1", "A2"));

        Person p1 = mock(Person.class); when(p1.getPhone()).thenReturn("841-000-0001");
        Person p2 = mock(Person.class); when(p2.getPhone()).thenReturn("841-000-0002");
        Person p3 = mock(Person.class); when(p3.getPhone()).thenReturn("841-000-0001"); // doublon
        Person p4 = mock(Person.class); when(p4.getPhone()).thenReturn(null);            // null filtré

        when(repo.findPersonsByAddress("A1")).thenReturn(List.of(p1, p2));
        when(repo.findPersonsByAddress("A2")).thenReturn(List.of(p3, p4));

        Set<String> out = service.getPhonesByFirestation("3");

        assertEquals(List.of("841-000-0001", "841-000-0002"), out.stream().toList());
    }

    @Test
    void getFireInfo() {
        String address = "834 Binoc Ave";
        when(repo.findStationByAddress(address)).thenReturn(Optional.of("3"));

        Person p1 = mock(Person.class);
        Person p2 = mock(Person.class);
        when(repo.findPersonsByAddress(address)).thenReturn(List.of(p1, p2));

        MedicalRecord r1 = mock(MedicalRecord.class);
        MedicalRecord r2 = mock(MedicalRecord.class);
        when(repo.findMedicalRecord(anyString(), anyString())).thenReturn(Optional.empty()); // recordOf appelle repo, mais le mapper recevra null ici

        ResidentMedicalDto rm1 = mock(ResidentMedicalDto.class);
        ResidentMedicalDto rm2 = mock(ResidentMedicalDto.class);
        when(residentMapper.toResident(eq(p1), isNull())).thenReturn(rm1);
        when(residentMapper.toResident(eq(p2), isNull())).thenReturn(rm2);

        FireAddressDto out = service.getFireInfo(address);

        assertEquals("3", out.stationNumber());
        assertEquals(List.of(rm1, rm2), out.residents());
    }

    @Test
    void getFloodByStations() {
        when(repo.findAddressesByStation("1")).thenReturn(Set.of("A1"));
        when(repo.findAddressesByStation("2")).thenReturn(Set.of("A1", "A2")); // A1 présent sur deux stations

        Person pA1_1 = mock(Person.class);
        Person pA1_2 = mock(Person.class);
        Person pA2_1 = mock(Person.class);

        when(repo.findPersonsByAddress("A1")).thenReturn(List.of(pA1_1, pA1_2));
        when(repo.findPersonsByAddress("A2")).thenReturn(List.of(pA2_1));

        when(residentMapper.toResident(eq(pA1_1), any())).thenReturn(mock(ResidentMedicalDto.class));
        when(residentMapper.toResident(eq(pA1_2), any())).thenReturn(mock(ResidentMedicalDto.class));
        when(residentMapper.toResident(eq(pA2_1), any())).thenReturn(mock(ResidentMedicalDto.class));

        Map<String, List<ResidentMedicalDto>> out = service.getFloodByStations(Set.of("1", "2"));

        assertTrue(out.containsKey("A1"));
        assertTrue(out.containsKey("A2"));
        assertEquals(2, out.get("A1").size());
        assertEquals(1, out.get("A2").size());
    }

    @Test
    void getPersonInfoByLastName() {
        Person p1 = mock(Person.class);
        Person p2 = mock(Person.class);
        when(repo.findPersonsByLastName("Boyd")).thenReturn(List.of(p1, p2));

        PersonInfoDto i1 = mock(PersonInfoDto.class);
        PersonInfoDto i2 = mock(PersonInfoDto.class);
        when(personInfoMapper.toInfo(eq(p1), any())).thenReturn(i1);
        when(personInfoMapper.toInfo(eq(p2), any())).thenReturn(i2);

        List<PersonInfoDto> out = service.getPersonInfoByLastName("Boyd");

        assertEquals(List.of(i1, i2), out);
        verify(personInfoMapper, times(2)).toInfo(any(), any());
    }

    @Test
    void getCommunityEmails() {
        when(repo.findEmailsByCity("Culver"))
                .thenReturn(new LinkedHashSet<>(List.of("a@mail.com", "b@mail.com")));

        Set<String> out = service.getCommunityEmails("Culver");

        assertEquals(Set.of("a@mail.com", "b@mail.com"), out);
        verify(repo).findEmailsByCity("Culver");
    }
}