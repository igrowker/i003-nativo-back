package com.igrowker.nativo.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.nativo.controllers.DonationController;
import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponseRecordPayment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.DonationService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/*
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

    @Nested
    class testCreateDonation_ShouldReturnOk {

        @Test
        public void createADonationTrue_ShouldReturnOk() throws Exception {

            // Arrange: Preparar las clases de Input y Output
            // se puede hacer fuera del test si son clases compartidas
            var RequestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),"348ad942-10aa-42b8-8173-a763c8d9b7e3","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",true);

            var ResponseDonationDtoTrue = new ResponseDonationDtoTrue("e17efc6c-6d57-4542-8ac1-637251e7662b",
                    BigDecimal.valueOf(100.0),"348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "Mario","Grande","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    "Ulises", "Gonzales", LocalDateTime.now(),
                    "PENDENT");

            when(donationService.createDonationTrue(RequestDonationDto)).thenReturn(ResponseDonationDtoTrue);

            // Act: llamada al método que se quiere probar
            mockMvc.perform(post("/api/donaciones/crear-donacion")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestDonationDto)))

                    // Assert: probar por verdadero o falso distintas aserciones
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(ResponseDonationDtoTrue.id())))
                    .andExpect(jsonPath("$.amount", Matchers.is(ResponseDonationDtoTrue.amount().doubleValue())))
                    .andExpect(jsonPath("$.accountIdDonor", Matchers.is(ResponseDonationDtoTrue.accountIdDonor())))
                    .andExpect(jsonPath("$.donorName", Matchers.is(ResponseDonationDtoTrue.donorName())))
                    .andExpect(jsonPath("$.donorLastName", Matchers.is(ResponseDonationDtoTrue.donorLastName())))
                    .andExpect(jsonPath("$.accountIdBeneficiary", Matchers.is(ResponseDonationDtoTrue.accountIdBeneficiary())))
                    .andExpect(jsonPath("$.beneficiaryName", Matchers.is(ResponseDonationDtoTrue.beneficiaryName())))
                    .andExpect(jsonPath("$.beneficiaryLastName", Matchers.is(ResponseDonationDtoTrue.beneficiaryLastName())))
                    .andExpect(jsonPath("$.createdAt", Matchers.is(ResponseDonationDtoTrue.createdAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")))))
                    .andExpect(jsonPath("$.status", Matchers.is(ResponseDonationDtoTrue.status())));
        }

        @Test
        public void createADonationFalse_ShouldReturnOk() throws Exception {

            // Arrange: Preparar las clases de Input y Output
            // se puede hacer fuera del test si son clases compartidas
            var RequestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),"348ad942-10aa-42b8-8173-a763c8d9b7e3","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",false);

            var ResponseDonationDtoFalse = new ResponseDonationDtoFalse("e17efc6c-6d57-4542-8ac1-637251e7662b",
                    BigDecimal.valueOf(100.0),"348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    LocalDateTime.now(),
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
                    .andExpect(jsonPath("$.amount", Matchers.is(ResponseDonationDtoFalse.amount().doubleValue())))
                    .andExpect(jsonPath("$.accountIdDonor", Matchers.is(ResponseDonationDtoFalse.accountIdDonor())))
                    .andExpect(jsonPath("$.accountIdBeneficiary", Matchers.is(ResponseDonationDtoFalse.accountIdBeneficiary())))
                    .andExpect(jsonPath("$.createdAt", Matchers.is(ResponseDonationDtoFalse.createdAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")))))
                    .andExpect(jsonPath("$.status", Matchers.is(ResponseDonationDtoFalse.status())));
        }

        @Test
        public void createADonationTrue_should_NOT_be_ok() throws Exception {

            var RequestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),"l","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",true);
            when(donationService.createDonationTrue(RequestDonationDto)).thenThrow(new ResourceNotFoundException("El id de la cuenta donante no existe"));

            mockMvc.perform(post("/api/donaciones/crear-donacion")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestDonationDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("El id de la cuenta donante no existe")));
        }
    }

    @Nested
    class testConfirmationDonation{

        @Test
        public void confirmation_ShouldReturnOk() throws Exception{

            var RequestDonationConfirmationDto = new RequestDonationConfirmationDto("e17efc6c-6d57-4542-8ac1-637251e7662b","348ad942-10aa-42b8-8173-a763c8d9b7e3","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", TransactionStatus.ACCEPTED);

            var ResponseDonationConfirmationDto = new ResponseDonationConfirmationDto("e17efc6c-6d57-4542-8ac1-637251e7662b",
                    BigDecimal.valueOf(100.0),"348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",TransactionStatus.ACCEPTED);

            when(donationService.confirmationDonation(RequestDonationConfirmationDto)).thenReturn(ResponseDonationConfirmationDto);
            // Act: llamada al método que se quiere probar
            mockMvc.perform(post("/api/donaciones/confirmar-donacion")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestDonationConfirmationDto)))

                    // Assert: probar por verdadero o falso distintas aserciones
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(ResponseDonationConfirmationDto.id())))
                    .andExpect(jsonPath("$.accountIdDonor", Matchers.is(ResponseDonationConfirmationDto.accountIdDonor())))
                    .andExpect(jsonPath("$.accountIdBeneficiary", Matchers.is(ResponseDonationConfirmationDto.accountIdBeneficiary())))
                    .andExpect(jsonPath("$.status", Matchers.is(ResponseDonationConfirmationDto.status().name())));
        }

        @Test
        public void confirmation_should_NOT_be_ok() throws Exception {

            var RequestDonationConfirmationDto = new RequestDonationConfirmationDto("e17efc6c-6d57-4542-8ac1-637251e7662","348ad942-10aa-42b8-8173-a763c8d9b7e3","218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", TransactionStatus.ACCEPTED);

            when(donationService.confirmationDonation(RequestDonationConfirmationDto)).thenThrow(new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación"));

            mockMvc.perform(post("/api/donaciones/confirmar-donacion")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestDonationConfirmationDto)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", Matchers.is("La cuenta indicada no coincide con el usuario logueado en la aplicación")));
        }
    }

    @Nested
    class testRecordDonation{

        @Test
        public void recordDonor_ShouldReturnOk() throws Exception{

            String idAccountDonor = "348ad942-10aa-42b8-8173-a763c8d9b7e3";

            // Yo lo filtro por cuenta del donador
            var responseRecordDonation = new ResponseDonationRecord("e17efc6c-6d57-4542-8ac1-637251e7662b",BigDecimal.valueOf(100.0),
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",TransactionStatus.ACCEPTED);

            when(donationService.recordDonationDonor(idAccountDonor)).thenReturn(List.of(responseRecordDonation));

            mockMvc.perform(get("/api/donaciones/historial-donaciones/donador/{idDonorAccount}",idAccountDonor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseRecordDonation.id())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseRecordDonation.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].accountIdDonor", Matchers.is(responseRecordDonation.accountIdDonor())))
                    .andExpect(jsonPath("$[0].accountIdBeneficiary", Matchers.is(responseRecordDonation.accountIdBeneficiary())))
                    .andExpect(jsonPath("$[0].status", Matchers.is(responseRecordDonation.status().name())));
        }

        @Test
        public void recordBeneficiary_ShouldReturnOk() throws Exception{

            String idAccountBeneficiary = "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c";

            // Yo lo filtro por cuenta del donador
            var responseRecordDonation = new ResponseDonationRecord("e17efc6c-6d57-4542-8ac1-637251e7662b",BigDecimal.valueOf(100.0),
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",TransactionStatus.ACCEPTED);

            when(donationService.recordDonationBeneficiary(idAccountBeneficiary)).thenReturn(List.of(responseRecordDonation));

            mockMvc.perform(get("/api/donaciones/historial-donaciones/beneficiario/{idBeneficiaryAccount}",idAccountBeneficiary))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseRecordDonation.id())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseRecordDonation.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].accountIdDonor", Matchers.is(responseRecordDonation.accountIdDonor())))
                    .andExpect(jsonPath("$[0].accountIdBeneficiary", Matchers.is(responseRecordDonation.accountIdBeneficiary())))
                    .andExpect(jsonPath("$[0].status", Matchers.is(responseRecordDonation.status().name())));
        }

        @Test
        public void recordDonor_should_NOT_be_ok() throws Exception {

            String idAccountDonor = "348ad942-10aa-42b8-8173-a763c8d9b7e3";

            when(donationService.recordDonationDonor(idAccountDonor)).thenThrow(new ResourceNotFoundException("No hay donaciones recibidas"));

            mockMvc.perform(get("/api/donaciones/historial-donaciones/donador/{idDonorAccount}",idAccountDonor))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No hay donaciones recibidas")));
        }

        @Test
        public void recordBeneficiary_should_NOT_be_ok() throws Exception {

            String idAccountBeneficiary = "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c";

            when(donationService.recordDonationBeneficiary(idAccountBeneficiary)).thenThrow(new InsufficientFundsException("La cuenta del donador no existe"));

            mockMvc.perform(get("/api/donaciones/historial-donaciones/beneficiario/{idBeneficiaryAccount}",idAccountBeneficiary))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("La cuenta del donador no existe")));
        }


    }

}

 */