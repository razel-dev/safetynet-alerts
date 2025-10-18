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
import org.springframework.http.MediaType; // <-- Contrat HTTP: déclaration des formats
import org.springframework.web.util.UriComponentsBuilder;



@RestController
@RequestMapping(path = "/person", produces = MediaType.APPLICATION_JSON_VALUE) // <-- Le contrôleur parle JSON en sortie
@RequiredArgsConstructor
@Validated
public class PersonController {

    private final PersonService personService; // <-- DÉLÉGATION: la logique métier est dans le service

    // Contrat HTTP:
    // - consomme JSON
    // - valide l'entrée (@Valid)
    // - renvoie 201 Created + en-tête Location vers la ressource créée
    @PostMapping()
    public ResponseEntity<PersonResponseDto> create(@Valid @RequestBody PersonCreateDto dto) {
        PersonResponseDto out = personService.create(dto); // <-- délégation au service, aucune logique métier ici
        return ResponseEntity
                .created( // <-- Statut 201 + en-tête Location
                        UriComponentsBuilder
                                .fromPath("/person/{firstName}/{lastName}")
                                .buildAndExpand(out.firstName(), out.lastName())
                                .encode()
                                .toUri()
                )
                .body(out); // <-- corps JSON de la ressource créée
    }

    // Contrat HTTP:
    // - variables de chemin pour identifier la ressource
    // - consomme/produit JSON
    // - renvoie 200 OK
    @PutMapping(path = "/{firstName}/{lastName}")
    public ResponseEntity<PersonResponseDto> update(
            @PathVariable String firstName,
            @PathVariable String lastName,
            @Valid @RequestBody PersonUpdateDto dto // <-- validation de la charge utile
    ) {
        return ResponseEntity.ok(personService.update(firstName, lastName, dto)); // <-- délégation + statut 200
    }

    // Contrat HTTP:
    // - suppression par identifiant dans l'URL
    // - renvoie 204 No Content sans corps
    @DeleteMapping("/{firstName}/{lastName}")
    public ResponseEntity<Void> delete(
            @PathVariable String firstName,
            @PathVariable String lastName) {
        personService.delete(firstName, lastName); // <-- délégation
        return ResponseEntity.noContent().build(); // <-- statut 204, pas de body
    }
}
