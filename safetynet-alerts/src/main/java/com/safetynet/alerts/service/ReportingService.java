package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.reporting.*;

import java.util.*;

public interface ReportingService {
    FirestationCoverageDto getPersonsByStation(String stationNumber);                 // /firestation
    List<ChildAlertDto> getChildAlert(String address);                                // /childAlert
    Set<String> getPhonesByFirestation(String stationNumber);                         // /phoneAlert
    FireAddressDto getFireInfo(String address);                                       // /fire
    Map<String, List<ResidentMedicalDto>> getFloodByStations(Set<String> stations);   // /flood/stations
    List<PersonInfoDto> getPersonInfoByLastName(String lastName);                     // /personInfo
    Set<String> getCommunityEmails(String city);                                      // /communityEmail
}
