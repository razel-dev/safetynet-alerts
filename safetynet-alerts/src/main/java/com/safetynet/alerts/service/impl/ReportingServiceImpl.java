package com.safetynet.alerts.service.impl;

import com.safetynet.alerts.dto.ChildAlertDto;
import com.safetynet.alerts.dto.FireAddressDto;
import com.safetynet.alerts.dto.FirestationCoverageDto;
import com.safetynet.alerts.dto.PersonInfoDto;
import com.safetynet.alerts.dto.PersonSummaryDto;
import com.safetynet.alerts.dto.ResidentMedicalDto;
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

    // ======================= Helpers anti-duplication =========================//

    /** Snapshot du dossier médical d'une personne ou null. */
    private MedicalRecord recordOf(Person p) {
        return repo.findMedicalRecord(p.getFirstName(), p.getLastName()).orElse(null);
    }

    /** Âge à partir d'un dossier (retourne -1 si mr == null). */
    private int ageFromRecord(MedicalRecord mr) {
        return (mr == null) ? -1 : AgeCalculator.computeAge(mr.getBirthdate());
    }

    /** Mappe Person -> PersonSummaryDto. */
    private PersonSummaryDto toSummary(Person p) {
        return new PersonSummaryDto(p.getFirstName(), p.getLastName(), p.getAddress(), p.getPhone());
    }

    /** Données santé dérivées du dossier médical. */
    private record HealthData(int age, List<String> medications, List<String> allergies) {}

    /** Calcule âge + listes meds/allergies pour une Person (gère null). */
    private HealthData healthOf(Person p) {
        MedicalRecord mr = recordOf(p);
        int age = ageFromRecord(mr);
        List<String> meds = (mr == null) ? Collections.emptyList() : mr.getMedications();
        List<String> algs = (mr == null) ? Collections.emptyList() : mr.getAllergies();
        return new HealthData(age, meds, algs);
    }

    /** Mappe Person -> ResidentMedicalDto en une passe (zéro duplication). */
    private ResidentMedicalDto toResidentMedical(Person p) {
        HealthData h = healthOf(p);
        return new ResidentMedicalDto(
                p.getFirstName(),
                p.getLastName(),
                p.getPhone(),
                h.age(),
                h.medications(),
                h.allergies()
        );
    }

    /** Mappe Person -> PersonInfoDto (adresse + email). */
    private PersonInfoDto toPersonInfo(Person p) {
        HealthData h = healthOf(p);
        return new PersonInfoDto(
                p.getFirstName(),
                p.getLastName(),
                p.getAddress(),
                h.age(),
                p.getEmail(),
                h.medications(),
                h.allergies()
        );
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
            summaries.add(toSummary(p));
            int age = ageFromRecord(recordOf(p));
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
                .map(this::toSummary)
                .toList();

        List<ChildAlertDto> children = new ArrayList<>();
        for (Person p : residents) {
            int age = ageFromRecord(recordOf(p));
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

        Set<String> addresses = repo.findAddressesByStation(stationNumber);
        Set<String> phones = addresses.stream()
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
        List<Person> residents = repo.findPersonsByAddress(address);

        List<ResidentMedicalDto> list = residents.stream()
                .map(this::toResidentMedical)
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
                List<ResidentMedicalDto> bucket = byAddress.computeIfAbsent(addr, a -> new ArrayList<>());
                for (Person p : repo.findPersonsByAddress(addr)) {
                    bucket.add(toResidentMedical(p));
                }
            }
        }
        log.info("[service] /flood/stations -> addresses={}", byAddress.size());
        return byAddress;
    }

    @Override
    public List<PersonInfoDto> getPersonInfoByLastName(String lastName) {
        log.debug("[service] /personInfo IN lastName={}", lastName);

        List<Person> people = repo.findPersonsByLastName(lastName);
        List<PersonInfoDto> out = people.stream()
                .map(this::toPersonInfo)
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
