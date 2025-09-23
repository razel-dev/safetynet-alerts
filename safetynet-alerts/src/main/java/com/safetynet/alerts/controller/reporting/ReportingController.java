package com.safetynet.alerts.controller.reporting;

import com.safetynet.alerts.dto.reporting.*;
import com.safetynet.alerts.service.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reporting;

    /** /firestation?stationNumber= */
    @GetMapping("/firestation")
    public FirestationCoverageDto firestation(@RequestParam String stationNumber) {
        log.debug("HTTP IN /firestation stationNumber={}", stationNumber);
        return reporting.getPersonsByStation(stationNumber);
    }

    /** /childAlert?address= */
    @GetMapping("/childAlert")
    public List<ChildAlertDto> childAlert(@RequestParam String address) {
        log.debug("HTTP IN /childAlert address={}", address);
        return reporting.getChildAlert(address);
    }

    /** /phoneAlert?firestation= */
    @GetMapping("/phoneAlert")
    public Set<String> phoneAlert(@RequestParam("firestation") String stationNumber) {
        log.debug("HTTP IN /phoneAlert firestation={}", stationNumber);
        return reporting.getPhonesByFirestation(stationNumber);
    }

    /** /fire?address= */
    @GetMapping("/fire")
    public FireAddressDto fire(@RequestParam String address) {
        log.debug("HTTP IN /fire address={}", address);
        return reporting.getFireInfo(address);
    }

    /** /flood/stations?stations=1,2,3 */
    @GetMapping("/flood/stations")
    public Map<String, List<ResidentMedicalDto>> flood(@RequestParam Set<String> stations) {
        log.debug("HTTP IN /flood/stations stations={}", stations);
        return reporting.getFloodByStations(stations);
    }

    /** /personInfo?lastName= */
    @GetMapping("/personInfo")
    public List<PersonInfoDto> personInfo(@RequestParam String lastName) {
        log.debug("HTTP IN /personInfo lastName={}", lastName);
        return reporting.getPersonInfoByLastName(lastName);
    }

    /** /communityEmail?city= */
    @GetMapping("/communityEmail")
    public Set<String> communityEmail(@RequestParam String city) {
        log.debug("HTTP IN /communityEmail city={}", city);
        return reporting.getCommunityEmails(city);
    }
}
