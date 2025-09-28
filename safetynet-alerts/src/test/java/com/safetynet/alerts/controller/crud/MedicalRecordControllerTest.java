package com.safetynet.alerts.controller.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.dto.crud.medicalrecord.MedicalRecordResponseDto;
import com.safetynet.alerts.service.MedicalRecordService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MedicalRecordControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    MedicalRecordService medicalRecordService;

    @Test
    void create() throws Exception {
        String firstName = "John";
        String lastName  = "Boyd";
        String birthdate = "03/06/1984";
        List<String> medications = List.of("aznol:350mg", "hydrapermazol:100mg");
        List<String> allergies   = List.of("nillacilan");

        Mockito.when(medicalRecordService.create(any()))
                .thenReturn(new MedicalRecordResponseDto(firstName, lastName, birthdate, medications, allergies));

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "birthdate", birthdate,
                        "medications", medications,
                        "allergies", allergies
                )
        );

        String expectedLocation = org.springframework.web.util.UriComponentsBuilder
                .fromPath("/medicalRecord/{first}/{last}")
                .buildAndExpand(firstName, lastName)
                .encode()
                .toUriString();

        mockMvc.perform(post("/medicalRecord")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", expectedLocation))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.birthdate").value(birthdate))
                .andExpect(jsonPath("$.medications[0]").value(medications.getFirst()))
                .andExpect(jsonPath("$.allergies[0]").value(allergies.getFirst()));
    }

    @Test
    void update() throws Exception {
        String firstName = "John";
        String lastName  = "Boyd";
        String newBirthdate = "04/07/1985";
        List<String> newMeds = List.of("newmed:10mg");
        List<String> newAllergies = List.of("peanut");

        Mockito.when(medicalRecordService.update(eq(firstName), eq(lastName), any()))
                .thenReturn(new MedicalRecordResponseDto(firstName, lastName, newBirthdate, newMeds, newAllergies));

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "birthdate", newBirthdate,
                        "medications", newMeds,
                        "allergies", newAllergies
                )
        );

        mockMvc.perform(put("/medicalRecord/{first}/{last}", firstName, lastName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.birthdate").value(newBirthdate))
                .andExpect(jsonPath("$.medications[0]").value(newMeds.getFirst()))
                .andExpect(jsonPath("$.allergies[0]").value(newAllergies.getFirst()));
    }

    @Test
    void deleteTest() throws Exception {
        String firstName = "John";
        String lastName  = "Boyd";

        Mockito.doNothing().when(medicalRecordService).delete(firstName, lastName);

        mockMvc.perform(delete("/medicalRecord/{first}/{last}", firstName, lastName))
                .andExpect(status().isNoContent());
    }
}