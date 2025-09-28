package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.reporting.ChildAlertDto;
import com.safetynet.alerts.dto.reporting.FireAddressDto;
import com.safetynet.alerts.dto.reporting.FirestationCoverageDto;
import com.safetynet.alerts.dto.reporting.PersonInfoDto;
import com.safetynet.alerts.dto.reporting.ResidentMedicalDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReportingService {
    FirestationCoverageDto getPersonsByStation(String stationNumber);                 // /firestation
    List<ChildAlertDto> getChildAlert(String address);                                // /childAlert
    Set<String> getPhonesByFirestation(String stationNumber);                         // /phoneAlert
    FireAddressDto getFireInfo(String address);                                       // /fire
    Map<String, List<ResidentMedicalDto>> getFloodByStations(Set<String> stations);   // /flood/stations
    List<PersonInfoDto> getPersonInfoByLastName(String lastName);                     // /personInfo
    Set<String> getCommunityEmails(String city);                                      // /communityEmail
}
