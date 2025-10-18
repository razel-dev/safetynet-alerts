package com.safetynet.alerts.mapper.crud.person;
import com.safetynet.alerts.dto.crud.person.PersonCreateDto;
import com.safetynet.alerts.dto.crud.person.PersonResponseDto;
import com.safetynet.alerts.dto.crud.person.PersonUpdateDto;
import com.safetynet.alerts.model.Person;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests unitaires du {@code PersonMapper}.
 * Objectif:
 * - Vérifier que l’implémentation MapStruct générée respecte les contrats de mapping
 *   entre {@code Person}, {@code PersonCreateDto}, {@code PersonUpdateDto} et {@code PersonResponseDto}.
 * - Garantir:
 *   - la copie champ-à-champ pour la création (toEntity),
 *   - la mise à jour sans modification de l’identité et sans écrasement par des valeurs nulles (update),
 *   - la projection fidèle de l’entité vers le DTO de réponse (toResponse).
 */

class PersonMapperTest {
    private final PersonMapper mapper = Mappers.getMapper(PersonMapper.class);
    @Test
    void toEntity_shouldMapAllFields() {
        PersonCreateDto dto = new PersonCreateDto(
                "John", "Boyd", "10 Downing St", "London", "SW1A 2AA", "0000", "john@example.com"
        );
        Person entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals("John", entity.getFirstName());
        assertEquals("Boyd", entity.getLastName());
        assertEquals("10 Downing St", entity.getAddress());
        assertEquals("London", entity.getCity());
        assertEquals("SW1A 2AA", entity.getZip());
        assertEquals("0000", entity.getPhone());
        assertEquals("john@example.com", entity.getEmail());
    }
    @Test
    void update_shouldIgnoreIdentity_andIgnoreNulls() {
        // Entité existante
        Person entity = new Person(
                "John", "Boyd", "10 Downing St", "London", "SW1A 2AA", "0000", "john@example.com"
        );
        // DTO de mise à jour: identité NE DOIT PAS être modifiée (le mapper les ignore)
        // et les valeurs nulles NE DOIVENT PAS écraser les valeurs existantes (nullValuePropertyMappingStrategy = IGNORE)
        PersonUpdateDto update = new PersonUpdateDto(
                null, // address reste inchangée
                "Paris", // city doit changer
                null,   // zip reste inchangée
                "1111", // phone doit changer
                null    // email reste inchangée
        );
        mapper.update(entity, update);
        // Identité immuable
        assertEquals("John", entity.getFirstName());
        assertEquals("Boyd", entity.getLastName());
        // Champs mis à jour vs conservés
        assertEquals("10 Downing St", entity.getAddress()); // null ignoré
        assertEquals("Paris", entity.getCity());            // mis à jour
        assertEquals("SW1A 2AA", entity.getZip());          // null ignoré
        assertEquals("1111", entity.getPhone());            // mis à jour
        assertEquals("john@example.com", entity.getEmail()); // null ignoré
    }
    @Test
    void toResponse_shouldMapAllFields() {
        Person entity = new Person(
                "Jane", "Doe", "1 Main St", "NYC", "10001", "1234", "jane@ex.com"
        );
        PersonResponseDto out = mapper.toResponse(entity);
        assertNotNull(out);
        assertEquals("Jane", out.firstName());
        assertEquals("Doe", out.lastName());
        assertEquals("1 Main St", out.address());
        assertEquals("NYC", out.city());
        assertEquals("10001", out.zip());
        assertEquals("1234", out.phone());
        assertEquals("jane@ex.com", out.email());
    }
}