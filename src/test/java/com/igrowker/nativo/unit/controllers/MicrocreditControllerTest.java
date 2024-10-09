package com.igrowker.nativo.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.nativo.controllers.MicrocreditController;
import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditPaymentDto;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.services.MicrocreditService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(value = MicrocreditController.class)
@WithMockUser
public class MicrocreditControllerTest {
    @MockBean
    private MicrocreditService microcreditService;

    @MockBean
    private ContributionService contributionService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Nested
    class CreateMicrocreditsTests {
        @Test
        public void createMicrocredit_ShouldReturnOk() throws Exception {
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto("Test title", "Test Description",
                    BigDecimal.valueOf(100000.00));

            ResponseMicrocreditDto responseMicrocreditDto = new ResponseMicrocreditDto("1234",
                    BigDecimal.valueOf(100000.00), BigDecimal.valueOf(1100000.00), BigDecimal.valueOf(0.00),
                    LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Test title", "Test Description", 1, BigDecimal.valueOf(10.0), TransactionStatus.PENDING);

            when(microcreditService.createMicrocredit(requestMicrocreditDto)).thenReturn(responseMicrocreditDto);

            mockMvc.perform(post("/api/microcreditos/solicitar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestMicrocreditDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(responseMicrocreditDto.id())))
                    .andExpect(jsonPath("$.amount", Matchers.is(responseMicrocreditDto.amount().doubleValue())))
                    .andExpect(jsonPath("$.remainingAmount", Matchers.is(responseMicrocreditDto.remainingAmount().doubleValue())))
                    .andExpect(jsonPath("$.createdDate", Matchers.is(responseMicrocreditDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$.expirationDate", Matchers.is(responseMicrocreditDto.expirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$.title", Matchers.is(responseMicrocreditDto.title())))
                    .andExpect(jsonPath("$.description", Matchers.is(responseMicrocreditDto.description())))
                    .andExpect(jsonPath("$.transactionStatus", Matchers.is(responseMicrocreditDto.transactionStatus().toString())));
        }

        @Test
        public void createMicrocredit_ShouldNotReturnOk() throws Exception {
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto("Test amount exception ", "Monto mayor al permitido",
                    BigDecimal.valueOf(100000.00));

            when(microcreditService.createMicrocredit(any())).thenThrow(new ValidationException("El monto del microcrédito tiene que ser igual o menor a: $ 500000"));

            mockMvc.perform(post("/api/microcreditos/solicitar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestMicrocreditDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("El monto del microcrédito tiene que ser igual o menor a: $ 500000")));
        }

        @Test
        public void createMicrocredit_ShouldReturnBadRequest() throws Exception {
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto("Test amount exception ", "Monto mayor al permitido",
                    BigDecimal.valueOf(-100000.00));

            when(microcreditService.createMicrocredit(any())).thenThrow(new ValidationException("El monto del microcrédito debe ser mayor a $ 0.00"));

            mockMvc.perform(post("/api/microcreditos/solicitar")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestMicrocreditDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("El monto del microcrédito debe ser mayor a $ 0.00")));
        }
    }

    @Nested
    class PayMicrocreditsTests {
        @Test
        public void payMicrocredit_ShouldReturnOk() throws Exception {
            ResponseMicrocreditPaymentDto responseMicrocreditPaymentDto = new ResponseMicrocreditPaymentDto("1234",
                    BigDecimal.valueOf(1000.00));

            when(microcreditService.payMicrocredit("1234")).thenReturn(responseMicrocreditPaymentDto);

            mockMvc.perform(post("/api/microcreditos/pagar/1234")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(responseMicrocreditPaymentDto))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(responseMicrocreditPaymentDto.id())))
                    .andExpect(jsonPath("$.totalPaidAmount", Matchers.is(responseMicrocreditPaymentDto.totalPaidAmount().doubleValue())));
        }

        @Test
        public void payMicrocredit_ShouldReturnNotFound() throws Exception {
            ResponseMicrocreditPaymentDto responseMicrocreditPaymentDto = new ResponseMicrocreditPaymentDto("1234",
                    BigDecimal.valueOf(1000.00));

            when(microcreditService.getOne(any())).
                    thenThrow(new ResourceNotFoundException("Microcrédito no encontrado con id: " + responseMicrocreditPaymentDto.id()));

            mockMvc.perform(get("/api/microcreditos/" + responseMicrocreditPaymentDto.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("Microcrédito no encontrado con id: 1234")));

        }

        @Test
        public void payMicrocredit_ShouldReturnBadRequest() throws Exception {
            ResponseMicrocreditPaymentDto responseMicrocreditPaymentDto = new ResponseMicrocreditPaymentDto("1234",
                    BigDecimal.valueOf(1000.00));

            when(microcreditService.getOne(any())).
                    thenThrow(new InsufficientFundsException("Fondos insuficientes"));

            mockMvc.perform(get("/api/microcreditos/" + responseMicrocreditPaymentDto.id()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("Fondos insuficientes")));

        }
    }

    @Nested
    class GetAllMicrocreditsByUserTests {
        @Test
        public void getAllMicrocreditsByUser_ShouldReturnOk() throws Exception {
            ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                    BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Test title", "Test Description", TransactionStatus.COMPLETED, List.of());

            when(microcreditService.getAllMicrocreditsByUser()).thenReturn(List.of(responseMicrocreditGetDto));

            mockMvc.perform(get("/api/microcreditos/usuario-logueado"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseMicrocreditGetDto.id())))
                    .andExpect(jsonPath("$[0].borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0)));
        }

        @Test
        public void getAllMicrocreditsByUser_ShouldReturnNotFound() throws Exception {
            when(microcreditService.getAllMicrocreditsByUser()).thenThrow(new ResourceNotFoundException("No se encontraron microcréditos."));

            mockMvc.perform(get("/api/microcreditos/usuario-logueado"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron microcréditos.")));
        }
    }

    @Nested
    class GetAllMicrocreditsByUserByStatusTests {
        @Test
        public void getAllMicrocreditsByUserByStatus_ShouldReturnOk() throws Exception {
            ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                    BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Test title", "Test Description", TransactionStatus.COMPLETED, List.of());

            when(microcreditService.getAllMicrocreditsByUserByStatus("COMPLETED")).thenReturn(List.of(responseMicrocreditGetDto));

            mockMvc.perform(get("/api/microcreditos/estado/COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseMicrocreditGetDto.id())))
                    .andExpect(jsonPath("$[0].borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0)));
        }

        @Test
        public void getAllMicrocreditsByUserByStatus_ShouldReturnNotFound() throws Exception {
            when(microcreditService.getAllMicrocreditsByUserByStatus("ACCEPTED"))
                    .thenThrow(new ResourceNotFoundException("No se encontraron microcréditos para el usuario con el estado especificado."));

            mockMvc.perform(get("/api/microcreditos/estado/ACCEPTED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron microcréditos para " +
                            "el usuario con el estado especificado.")));
        }
    }

    @Nested
    class GetAllMicrocreditsBetweenDatesTests {
        @Test
        public void getMicrocreditsBetweenDates_ShouldReturnOk() throws Exception {
            String fromDate = "2024-09-01";
            String toDate = "2024-10-01";

            ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                    BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDateTime.of(2024, 9, 20, 12, 05),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Test title", "Test Description", TransactionStatus.COMPLETED, List.of());

            when(microcreditService.getMicrocreditsBetweenDates(fromDate, toDate)).thenReturn(List.of(responseMicrocreditGetDto));

            mockMvc.perform(get("/api/microcreditos/entrefechas?fromDate=" + fromDate + "&toDate=" + toDate))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseMicrocreditGetDto.id())))
                    .andExpect(jsonPath("$[0].borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0)));
        }

        @Test
        public void getMicrocreditsBetweenDates_ShouldReturnBadRequest() throws Exception {
            String fromDate = "2023-12-31";
            String toDate = LocalDate.now().toString();

            when(microcreditService.getMicrocreditsBetweenDates(fromDate, toDate))
                    .thenThrow(new ValidationException("La fecha final no puede ser menor a la inicial."));

            mockMvc.perform(get("/api/microcreditos/entrefechas?fromDate=" + fromDate + "&toDate=" + toDate))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("La fecha final no puede ser menor a la inicial.")));
        }

        @Test
        public void getMicrocreditsBetweenDates_ShouldReturnNotFound() throws Exception {
            String fromDate = LocalDate.now().toString();
            String toDate = "2023-12-31";

            when(microcreditService.getMicrocreditsBetweenDates(fromDate, toDate))
                    .thenThrow(new ResourceNotFoundException("No posee microcréditos solicitados"));

            mockMvc.perform(get("/api/microcreditos/entrefechas?fromDate=" + fromDate + "&toDate=" + toDate))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No posee microcréditos solicitados")));
        }
    }

    @Nested
    class GetMicrocreditsByDateAndStatusTests {
        @Test
        public void getMicrocreditsByDateAndStatus_ShouldReturnOk() throws Exception {
            String date = "2024-09-20";

            ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                    BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDateTime.of(2024, 9, 20, 12, 05),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Test title", "Test Description", TransactionStatus.COMPLETED, List.of());

            when(microcreditService.getMicrocreditsByDateAndStatus(date, "COMPLETED")).thenReturn(List.of(responseMicrocreditGetDto));

            mockMvc.perform(get("/api/microcreditos//buscar-fecha-estado?date=" + date + "&status=" + "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseMicrocreditGetDto.id())))
                    .andExpect(jsonPath("$[0].borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0)));
        }

        @Test
        public void getMicrocreditsByDateAndStatus_ShouldReturnNotFound() throws Exception {
            String date = LocalDate.now().toString();

            when(microcreditService.getMicrocreditsByDateAndStatus(date, "COMPLETED"))
                    .thenThrow(new ResourceNotFoundException("No posee microcréditos solicitados"));

            mockMvc.perform(get("/api/microcreditos//buscar-fecha-estado?date=" + date + "&status=" + "COMPLETED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No posee microcréditos solicitados")));
        }
    }

    @Nested
    class GetAllMicrocreditsTests {
        @Test
        public void getAll_ShouldReturnOk() throws Exception {
            ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("microcredits",
                    "borrower1",
                    BigDecimal.valueOf(10000.00),
                    BigDecimal.valueOf(10000.00),
                    LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Auxilio",
                    "Test exitoso",
                    TransactionStatus.ACCEPTED, List.of());

            when(microcreditService.getAll()).thenReturn(List.of(responseMicrocreditGetDto));

            mockMvc.perform(get("/api/microcreditos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseMicrocreditGetDto.id())))
                    .andExpect(jsonPath("$[0].borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0)));
        }

        @Test
        public void getAll_ShouldReturnNotFound() throws Exception {
            when(microcreditService.getAll()).thenThrow(new ResourceNotFoundException("No se encontraron microcréditos."));

            mockMvc.perform(get("/api/microcreditos"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron microcréditos.")));
        }
    }

    @Nested
    class GetOneMicrocreditTests {
        @Test
        public void getOne_ShouldReturnOk() throws Exception {
            ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                    BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Test getOne ok", "Test Description", TransactionStatus.PENDING, List.of());

            when(microcreditService.getOne("1234")).thenReturn(responseMicrocreditGetDto);

            mockMvc.perform(get("/api/microcreditos/1234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(responseMicrocreditGetDto.id())))
                    .andExpect(jsonPath("$.borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                    .andExpect(jsonPath("$.amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                    .andExpect(jsonPath("$.remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                    .andExpect(jsonPath("$.createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$.expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$.title", Matchers.is(responseMicrocreditGetDto.title())))
                    .andExpect(jsonPath("$.description", Matchers.is(responseMicrocreditGetDto.description())))
                    .andExpect(jsonPath("$.transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$.contributions", Matchers.hasSize(0)));
        }

        @Test
        public void getOne_ShouldReturnNotFound() throws Exception {
            ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                    BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDateTime.now(),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Test getOne NOT ok", "Test Description", TransactionStatus.PENDING, List.of());

            when(microcreditService.getOne(any())).
                    thenThrow(new ResourceNotFoundException("Microcrédito no encontrado con id: " + responseMicrocreditGetDto.id()));

            mockMvc.perform(get("/api/microcreditos/" + responseMicrocreditGetDto.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("Microcrédito no encontrado con id: 1234")));
        }
    }

    @Nested
    class GetAllMicrocreditsByTransactionStatusTests {
        @Test
        public void getMicrocreditsByTransactionStatus_ShouldReturnOk() throws Exception {
            ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                    BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    "Test title", "Test Description", TransactionStatus.ACCEPTED, List.of());

            when(microcreditService.getMicrocreditsByTransactionStatus("ACCEPTED")).thenReturn(List.of(responseMicrocreditGetDto));

            mockMvc.perform(get("/api/microcreditos/historial-estados/ACCEPTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseMicrocreditGetDto.id())))
                    .andExpect(jsonPath("$[0].borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0)));
        }

        @Test
        public void getMicrocreditsByTransactionStatus_ShouldReturnNotFound() throws Exception {
            when(microcreditService.getMicrocreditsByTransactionStatus("ACCEPTED")).thenThrow(new ResourceNotFoundException("No se encontraron microcréditos con el estado especificado."));

            mockMvc.perform(get("/api/microcreditos/historial-estados/ACCEPTED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron microcréditos con el estado especificado.")));
        }
    }

    @Nested
    class CreateContributionsTests {
        @Test
        public void createContribution_ShouldReturnOk() throws Exception {
            RequestContributionDto requestContributionDto = new RequestContributionDto("1234",
                    BigDecimal.valueOf(10000.00));

            ResponseContributionDto responseContributionDto = new ResponseContributionDto("5678", "lenderAccountId_Test",
                    "Tester1", "Tester2", "1234",

                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 9, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.createContribution(requestContributionDto)).thenReturn(responseContributionDto);

            mockMvc.perform(post("/api/microcreditos/contribuir")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestContributionDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(responseContributionDto.id())))
                    .andExpect(jsonPath("$.lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                    .andExpect(jsonPath("$.lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                    .andExpect(jsonPath("$.borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                    .andExpect(jsonPath("$.microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                    .andExpect(jsonPath("$.amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                    .andExpect(jsonPath("$.createdDate", Matchers.is(responseContributionDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$.expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$.transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));
        }

        @Test
        public void createContribution_ShouldReturnBadRequest() throws Exception {
            RequestContributionDto requestContributionDto = new RequestContributionDto("1234",
                    BigDecimal.valueOf(-10000.00));

            when(contributionService.createContribution(requestContributionDto))
                    .thenThrow(new ValidationException("El monto de la contribución debe ser mayor a $ 0.00"));

            mockMvc.perform(post("/api/microcreditos/contribuir")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestContributionDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("El monto de la contribución debe ser mayor a $ 0.00")));
        }

        @Test
        public void createContribution_ShouldReturn_insufficient_fund() throws Exception {
            RequestContributionDto requestContributionDto = new RequestContributionDto("1234",
                    BigDecimal.valueOf(10000.00));

            when(contributionService.createContribution(requestContributionDto))
                    .thenThrow(new ValidationException("Fondos insuficientes"));

            mockMvc.perform(post("/api/microcreditos/contribuir")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(requestContributionDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("Fondos insuficientes")));
        }
    }
}