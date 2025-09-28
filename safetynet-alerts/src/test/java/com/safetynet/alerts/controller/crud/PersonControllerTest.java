package com.safetynet.alerts.controller.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.dto.crud.person.PersonResponseDto;
import com.safetynet.alerts.service.PersonService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    PersonService personService;

    @Test
    void create() throws Exception {
        String firstName = "John";
        String lastName  = "Doe";
        String address = "1509 Culver St";
        String city = "Culver";
        String zip = "97451";
        String phone = "841-874-6512";
        String email = "john.doe@example.com";

        // Le service renvoie la représentation créée
        Mockito.when(personService.create(any()))
                .thenReturn(new PersonResponseDto(firstName, lastName, address, city, zip, phone, email));

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "address", address,
                        "city", city,
                        "zip", zip,
                        "phone", phone,
                        "email", email
                )
        );

        String expectedLocation = "/person/" + firstName + "/" + lastName;

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", expectedLocation))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName));
    }

    @Test
    void update() throws Exception {
        String firstName = "John";
        String lastName  = "Doe";
        String newAddress = "29 15th St";
        String newCity = "NYC";
        String newZip = "10001";
        String newPhone = "212-555-0000";
        String newEmail = "john.doe+new@example.com";

        Mockito.when(personService.update(eq(firstName), eq(lastName), any()))
                .thenReturn(new PersonResponseDto(firstName, lastName, newAddress, newCity, newZip, newPhone, newEmail));

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "address", newAddress,
                        "city", newCity,
                        "zip", newZip,
                        "phone", newPhone,
                        "email", newEmail
                )
        );

        mockMvc.perform(put("/person/{first}/{last}", firstName, lastName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.address").value(newAddress))
                .andExpect(jsonPath("$.city").value(newCity))
                .andExpect(jsonPath("$.zip").value(newZip))
                .andExpect(jsonPath("$.phone").value(newPhone))
                .andExpect(jsonPath("$.email").value(newEmail));
    }

    @Test
    void deleteTest() throws Exception {
        String firstName = "John";
        String lastName  = "Doe";

        Mockito.doNothing().when(personService).delete(firstName, lastName);

        mockMvc.perform(delete("/person/{first}/{last}", firstName, lastName))
                .andExpect(status().isNoContent());
    }
}