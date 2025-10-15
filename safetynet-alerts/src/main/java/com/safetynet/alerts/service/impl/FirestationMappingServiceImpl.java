package com.safetynet.alerts.service.impl;

import com.safetynet.alerts.dto.crud.firestation.FirestationCreateDto;
import com.safetynet.alerts.dto.crud.firestation.FirestationResponseDto;
import com.safetynet.alerts.dto.crud.firestation.FirestationUpdateDto;
import com.safetynet.alerts.exception.ConflictExeption;
import com.safetynet.alerts.exception.NotFoundExeption;
import com.safetynet.alerts.mapper.crud.firestation.FirestationCrudMapper;
import com.safetynet.alerts.repository.DataRepository;
import com.safetynet.alerts.service.FirestationMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class FirestationMappingServiceImpl implements FirestationMappingService {

    private final DataRepository repository;
    private final FirestationCrudMapper mapper;

    @Override
    public FirestationResponseDto create(FirestationCreateDto dto) {
        log.debug("[service] FS.create IN dto={}", dto);
        final String address = dto.address();
        final String station = dto.station();

        ensureAddressNotMapped(address);

        var out = saveAndRespond(address, station, "create");
        return out;
    }

    @Override
    public FirestationResponseDto update(String address, FirestationUpdateDto dto) {
        log.debug("[service] FS.update IN address={} dto={}", address, dto);
        ensureAddressExists(address);

        final String station = dto.station();

        var out = saveAndRespond(address, station, "update");
        return out;
    }
    // ... existing code ...
    @Override
    public void delete(String address) {
        log.debug("[service] FS.delete IN address={}", address);
        ensureAddressExists(address);

        repository.deleteMapping(address);
        log.info("[service] FS.delete OUT address={}", address);
    }

    private void ensureAddressNotMapped(String address) {
        if (repository.findStationByAddress(address).isPresent()) {
            throw new ConflictExeption("Mapping already exists for address: " + address);
        }
    }

    private void ensureAddressExists(String address) {
        if (repository.findStationByAddress(address).isEmpty()) {
            throw new NotFoundExeption("Mapping not found for address: " + address);
        }
    }

    private FirestationResponseDto saveAndRespond(String address, String station, String action) {
        repository.saveMapping(address, station);
        var out = mapper.toResponse(address, station);
        log.info("[service] FS.{} OUT address={} station={}", action, address, station);
        return out;
    }
}