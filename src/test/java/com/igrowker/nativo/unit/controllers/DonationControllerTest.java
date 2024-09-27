package com.igrowker.nativo.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.nativo.controllers.DonationController;
import com.igrowker.nativo.dtos.donation.RequestDonationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationDtoFalse;
import com.igrowker.nativo.dtos.donation.ResponseDonationDtoTrue;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.DonationService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = DonationController.class)
@WithMockUser // Simula que un usuario autenticado realiza operaciones
public class DonationControllerTest {

    @MockBean
    private JwtService jwtService;

    // Se necesita importa las dependencias necesarias en el controlador (Servicios)
    @MockBean
    private DonationService donationService;

    @Autowired
    private MockMvc mockMvc; // Facilita la simulacion de peticiones Http

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    public void createADonationTrue_ShouldReturnOk() throws Exception {

        // Arrange: Preparar las clases de Input y Output
        // se puede hacer fuera del test si son clases compartidas
        var RequestDonationDto = new RequestDonationDto(new BigDecimal("100.0"),"348ad942-10aa-42b8-8173-a763c8d9b7e3","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",true);

        var ResponseDonationDtoTrue = new ResponseDonationDtoTrue("e17efc6c-6d57-4542-8ac1-637251e7662b",
                new BigDecimal("100.0"),"348ad942-10aa-42b8-8173-a763c8d9b7e3",
                "Mario","Grande","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                "Ulises", "Gonzales", LocalDateTime.of(2024, 9, 26, 18, 19, 1),
                "PENDENT");

        // Probar BigDecimal.of o algo por ahi

        when(donationService.createDonationTrue(RequestDonationDto)).thenReturn(ResponseDonationDtoTrue);

        // Act: llamada al método que se quiere probar
        mockMvc.perform(post("/api/donaciones/crear-donacion")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(RequestDonationDto)))

                // Assert: probar por verdadero o falso distintas aserciones
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(ResponseDonationDtoTrue.id())))
                .andExpect(jsonPath("$.amount", Matchers.is(ResponseDonationDtoTrue.amount())))
                .andExpect(jsonPath("$.accountIdDonor", Matchers.is(ResponseDonationDtoTrue.accountIdDonor())))
                .andExpect(jsonPath("$.donorName", Matchers.is(ResponseDonationDtoTrue.donorName())))
                .andExpect(jsonPath("$.donorLastName", Matchers.is(ResponseDonationDtoTrue.donorLastName())))
                .andExpect(jsonPath("$.accountIdBeneficiary", Matchers.is(ResponseDonationDtoTrue.accountIdBeneficiary())))
                .andExpect(jsonPath("$.beneficiaryName", Matchers.is(ResponseDonationDtoTrue.beneficiaryName())))
                .andExpect(jsonPath("$.beneficiaryLastName", Matchers.is(ResponseDonationDtoTrue.beneficiaryLastName())))
                .andExpect(jsonPath("$.createdAt", Matchers.is(ResponseDonationDtoTrue.createdAt())))
                .andExpect(jsonPath("$.status", Matchers.is(ResponseDonationDtoTrue.status())));
    }

    @Test
    public void createADonationFalse_ShouldReturnOk() throws Exception {

        // Arrange: Preparar las clases de Input y Output
        // se puede hacer fuera del test si son clases compartidas
        var RequestDonationDto = new RequestDonationDto(new BigDecimal("100"),"348ad942-10aa-42b8-8173-a763c8d9b7e3","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",false);

        var ResponseDonationDtoFalse = new ResponseDonationDtoFalse("e17efc6c-6d57-4542-8ac1-637251e7662b",
                new BigDecimal("100"),"348ad942-10aa-42b8-8173-a763c8d9b7e3",
                "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                LocalDateTime.of(2024, 9, 26, 18, 19, 1),
                "PENDENT");

        when(donationService.createDonationFalse(RequestDonationDto)).thenReturn(ResponseDonationDtoFalse);

        // Act: llamada al metodo que se quiere probar
        mockMvc.perform(post("/api/donaciones/crear-donacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(RequestDonationDto)))

                // Assert: probar por verdadero o falso distintas aserciones
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(ResponseDonationDtoFalse.id())))
                .andExpect(jsonPath("$.amount", Matchers.is(ResponseDonationDtoFalse.amount())))
                .andExpect(jsonPath("$.accountIdDonor", Matchers.is(ResponseDonationDtoFalse.accountIdDonor())))
                .andExpect(jsonPath("$.accountIdBeneficiary", Matchers.is(ResponseDonationDtoFalse.accountIdBeneficiary())))
                .andExpect(jsonPath("$.createdAt", Matchers.is(ResponseDonationDtoFalse.createdAt())))
                .andExpect(jsonPath("$.status", Matchers.is(ResponseDonationDtoFalse.status())));
    }

}