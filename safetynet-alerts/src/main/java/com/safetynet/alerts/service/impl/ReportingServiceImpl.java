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
import com.safetynet.alerts.service.ReportingService;
import com.safetynet.alerts.time.AgeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private final DataRepository repo;

    // >>> Mappers injectés
    private final SummaryMapper summaryMapper;
    private final ResidentMapper residentMapper;
    private final PersonInfoMapper personInfoMapper;

    // ------------------------ Helper non-mapping (accès repo) ------------------------
    /** Snapshot du dossier médical d'une personne ou null. */
    private MedicalRecord recordOf(Person p) {
        return repo.findMedicalRecord(p.getFirstName(), p.getLastName()).orElse(null);
    }

    // ============================ Endpoints GET ===============================

    @Override
    public FirestationCoverageDto getPersonsByStation(String stationNumber) {
        log.debug("[service] /firestation IN station={}", stationNumber);

        Set<String> addresses = repo.findAddressesByStation(stationNumber);
        List<Person> persons = addresses.stream()
                .flatMap(addr -> repo.findPersonsByAddress(addr).stream())
                .toList();

        int adults = 0, children = 0;
        List<PersonSummaryDto> summaries = new ArrayList<>(persons.size());

        for (Person p : persons) {
            summaries.add(summaryMapper.toSummary(p));
            int age = AgeCalculator.computeAge(
                    Optional.ofNullable(recordOf(p)).map(MedicalRecord::getBirthdate).orElse(null));
            if (age >= 0 && age <= 18) children++; else adults++;
        }

        FirestationCoverageDto out = new FirestationCoverageDto(summaries, adults, children);
        log.info("[service] /firestation station={} -> persons={} (adults={}, children={})",
                stationNumber, summaries.size(), adults, children);
        return out;
    }

    @Override
    public List<ChildAlertDto> getChildAlert(String address) {
        log.debug("[service] /childAlert IN address={}", address);

        List<Person> residents = repo.findPersonsByAddress(address);
        if (residents.isEmpty()) return Collections.emptyList();

        List<PersonSummaryDto> household = residents.stream()
                .map(summaryMapper::toSummary)
                .toList();

        List<ChildAlertDto> children = new ArrayList<>();
        for (Person p : residents) {
            int age = AgeCalculator.computeAge(
                    Optional.ofNullable(recordOf(p)).map(MedicalRecord::getBirthdate).orElse(null));
            if (age >= 0 && age <= 18) {
                List<PersonSummaryDto> others = household.stream()
                        .filter(ps -> !(ps.firstName().equals(p.getFirstName())
                                && ps.lastName().equals(p.getLastName())))
                        .collect(Collectors.toList());
                children.add(new ChildAlertDto(p.getFirstName(), p.getLastName(), age, others));
            }
        }
        log.info("[service] /childAlert address={} -> children={}", address, children.size());
        return children;
    }

    @Override
    public Set<String> getPhonesByFirestation(String stationNumber) {
        log.debug("[service] /phoneAlert IN station={}", stationNumber);

        Set<String> phones = repo.findAddressesByStation(stationNumber).stream()
                .flatMap(addr -> repo.findPersonsByAddress(addr).stream())
                .map(Person::getPhone)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)); // dédoublonné + ordre stable

        log.info("[service] /phoneAlert station={} -> phones={}", stationNumber, phones.size());
        return phones;
    }

    @Override
    public FireAddressDto getFireInfo(String address) {
        log.debug("[service] /fire IN address={}", address);

        String station = repo.findStationByAddress(address).orElse("");
        List<ResidentMedicalDto> list = repo.findPersonsByAddress(address).stream()
                .map(p -> residentMapper.toResident(p, recordOf(p)))
                .collect(Collectors.toList());

        FireAddressDto out = new FireAddressDto(station, list);
        log.info("[service] /fire address={} -> station={} residents={}", address, station, list.size());
        return out;
    }

    @Override
    public Map<String, List<ResidentMedicalDto>> getFloodByStations(Set<String> stations) {
        log.debug("[service] /flood/stations IN stations={}", stations);

        Map<String, List<ResidentMedicalDto>> byAddress = new LinkedHashMap<>();
        for (String st : stations) {
            for (String addr : repo.findAddressesByStation(st)) {
                var residents = repo.findPersonsByAddress(addr).stream()
                        .map(p -> residentMapper.toResident(p, recordOf(p)))
                        .toList();
                byAddress.merge(addr, new ArrayList<>(residents), (a, b) -> { a.addAll(b); return a; });
            }
        }
        log.info("[service] /flood/stations -> addresses={}", byAddress.size());
        return byAddress;
    }

    @Override
    public List<PersonInfoDto> getPersonInfoByLastName(String lastName) {
        log.debug("[service] /personInfo IN lastName={}", lastName);

        List<PersonInfoDto> out = repo.findPersonsByLastName(lastName).stream()
                .map(p -> personInfoMapper.toInfo(p, recordOf(p)))
                .collect(Collectors.toList());

        log.info("[service] /personInfo lastName={} -> results={}", lastName, out.size());
        return out;
    }

    @Override
    public Set<String> getCommunityEmails(String city) {
        log.debug("[service] /communityEmail IN city={}", city);
        Set<String> emails = repo.findEmailsByCity(city);
        log.info("[service] /communityEmail city={} -> emails={}", city, emails.size());
        return emails;
    }
}
