package com.safetynet.alerts.controller.crud;

import com.safetynet.alerts.dto.crud.person.PersonCreateDto;
import com.safetynet.alerts.dto.crud.person.PersonResponseDto;
import com.safetynet.alerts.dto.crud.person.PersonUpdateDto;
import com.safetynet.alerts.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.util.UriComponentsBuilder;



@RestController
@RequestMapping(path = "/person")
@RequiredArgsConstructor
@Validated
public class PersonController {

    private final PersonService personService;


    @PostMapping(consumes = "application/json")
    public ResponseEntity<PersonResponseDto> create(@Valid @RequestBody PersonCreateDto dto) {
        PersonResponseDto out = personService.create(dto);
        return ResponseEntity
                .created(
                        UriComponentsBuilder
                                .fromPath("/person/{firstName}/{lastName}")
                                .buildAndExpand(out.firstName(), out.lastName())
                                .encode()
                                .toUri()
                )
                .body(out);
    }

    @PutMapping(path = "/{firstName}/{lastName}")
    public ResponseEntity<PersonResponseDto> update(
            @PathVariable String firstName,
            @PathVariable String lastName,

            @Valid @RequestBody PersonUpdateDto dto) {
        return ResponseEntity.ok(personService.update(firstName, lastName, dto));
    }


    @DeleteMapping("/{firstName}/{lastName}")
    public ResponseEntity<Void> delete(
            @PathVariable String firstName,
            @PathVariable String lastName) {
        personService.delete(firstName, lastName);
        return ResponseEntity.noContent().build();
    }
}
