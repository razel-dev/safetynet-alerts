package com.safetynet.alerts.controller.crud;

import com.safetynet.alerts.dto.crud.firestation.FirestationCreateDto;
import com.safetynet.alerts.dto.crud.firestation.FirestationResponseDto;
import com.safetynet.alerts.dto.crud.firestation.FirestationUpdateDto;
import com.safetynet.alerts.service.FirestationMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(path = "/firestation", produces = "application/json")
@RequiredArgsConstructor
@Validated
public class FirestationMappingController {

    private final FirestationMappingService service;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<FirestationResponseDto> create(@Valid @RequestBody FirestationCreateDto dto) {
        var out = service.create(dto);
        return ResponseEntity
                .created(URI.create("/firestation/" + out.address()))
                .body(out);
    }

    @PutMapping(path = "/{address}", consumes = "application/json")
    public ResponseEntity<FirestationResponseDto> update(
            @PathVariable String address,
            @Valid @RequestBody FirestationUpdateDto dto) {
        return ResponseEntity.ok(service.update(address, dto));
    }

    @DeleteMapping("/{address}")
    public ResponseEntity<Void> delete(@PathVariable String address) {
        service.delete(address);
        return ResponseEntity.noContent().build();
    }
}
