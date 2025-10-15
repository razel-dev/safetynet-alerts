
package com.safetynet.alerts.controller.crud;

import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordCreateDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordResponseDto;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordUpdateDto;
import com.safetynet.alerts.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.util.UriComponentsBuilder;



@RestController
@RequestMapping(path = "/medicalRecord")
@RequiredArgsConstructor
@Validated
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<MedicalRecordResponseDto> create(@Valid @RequestBody MedicalRecordCreateDto dto) {
        MedicalRecordResponseDto out = medicalRecordService.create(dto);

        return ResponseEntity
                .created(
                        UriComponentsBuilder
                                .fromPath("/medicalRecord/{firstName}/{lastName}")
                                .buildAndExpand(out.firstName(), out.lastName())
                                .encode()
                                .toUri()
                )
                .body(out);
    }

    @PutMapping(path = "/{firstName}/{lastName}", consumes = "application/json")
    public ResponseEntity<MedicalRecordResponseDto> update(
            @PathVariable String firstName,
            @PathVariable String lastName,
            @Valid @RequestBody MedicalRecordUpdateDto dto) {
        return ResponseEntity.ok(medicalRecordService.update(firstName, lastName, dto));
    }

    @DeleteMapping("/{firstName}/{lastName}")
    public ResponseEntity<Void> delete(@PathVariable String firstName, @PathVariable String lastName) {
        medicalRecordService.delete(firstName, lastName);
        return ResponseEntity.noContent().build();
    }
}
