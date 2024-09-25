package com.igrowker.nativo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditPaymentDto;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.services.MicrocreditService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void createMicrocredit_ShouldReturnOk() throws Exception {
        RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto("Test title", "Test Description",
                BigDecimal.valueOf(100000.00), LocalDate.of(2024, 10, 17));

        ResponseMicrocreditDto responseMicrocreditDto = new ResponseMicrocreditDto("1234",
                BigDecimal.valueOf(100000.00), BigDecimal.valueOf(0.00), LocalDate.of(2024, 9, 17),
                requestMicrocreditDto.expirationDate(),
                "Test title", "Test Description", TransactionStatus.PENDENT);

        when(microcreditService.createMicrocredit(requestMicrocreditDto)).thenReturn(responseMicrocreditDto);

        mockMvc.perform(post("/api/microcreditos/solicitar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestMicrocreditDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", Matchers.is(responseMicrocreditDto.id())))
                .andExpect(jsonPath("$.amount", Matchers.is(responseMicrocreditDto.amount().doubleValue())))
                .andExpect(jsonPath("$.remainingAmount", Matchers.is(responseMicrocreditDto.remainingAmount().doubleValue())))
                .andExpect(jsonPath("$.createdDate", Matchers.is(responseMicrocreditDto.createdDate().toString())))
                .andExpect(jsonPath("$.expirationDate", Matchers.is(responseMicrocreditDto.expirationDate().toString())))
                .andExpect(jsonPath("$.title", Matchers.is(responseMicrocreditDto.title())))
                .andExpect(jsonPath("$.description", Matchers.is(responseMicrocreditDto.description())))
                .andExpect(jsonPath("$.transactionStatus", Matchers.is(responseMicrocreditDto.transactionStatus().toString())));
    }

    @Test
    public void getOne_ShouldReturnOk() throws Exception {
        ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDate.now(), LocalDate.now().plusDays(30),
                "Test title", "Test Description", TransactionStatus.PENDENT, List.of());

        when(microcreditService.getOne("1234")).thenReturn(responseMicrocreditGetDto);

        mockMvc.perform(get("/api/microcreditos/1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(responseMicrocreditGetDto.id())))
                .andExpect(jsonPath("$.borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                .andExpect(jsonPath("$.amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                .andExpect(jsonPath("$.remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                .andExpect(jsonPath("$.createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().toString())))
                .andExpect(jsonPath("$.expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().toString())))
                .andExpect(jsonPath("$.title", Matchers.is(responseMicrocreditGetDto.title())))
                .andExpect(jsonPath("$.description", Matchers.is(responseMicrocreditGetDto.description())))
                .andExpect(jsonPath("$.transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                .andExpect(jsonPath("$.contributions", Matchers.hasSize(0)));
    }

    @Test
    public void getAll_ShouldReturnOk() throws Exception {
        ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("microcredits",
                "borrower1",
                BigDecimal.valueOf(10000.00),
                BigDecimal.valueOf(10000.00),
                LocalDate.of(2024, 9, 17),
                LocalDate.of(2024, 10, 17),
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
                .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().toString())))
                .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().toString())))
                .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0))); // Ajusta el tamaño según las contribuciones esperadas
    }

    @Test
    public void getMicrocreditsByTransactionStatus_ShouldReturnOk() throws Exception {
        ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDate.now(), LocalDate.now().plusDays(30),
                "Test title", "Test Description", TransactionStatus.ACCEPTED, List.of());

        when(microcreditService.getMicrocreditsByTransactionStatus("ACCEPTED")).thenReturn(List.of(responseMicrocreditGetDto));

        mockMvc.perform(get("/api/microcreditos/historial-estados/ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].id", Matchers.is(responseMicrocreditGetDto.id())))
                .andExpect(jsonPath("$[0].borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                .andExpect(jsonPath("$[0].amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                .andExpect(jsonPath("$[0].remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().toString())))
                .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().toString())))
                .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0)));
    }

    @Test
    public void createContribution_ShouldReturnOk() throws Exception {
        RequestContributionDto requestContributionDto = new RequestContributionDto("1234",
                BigDecimal.valueOf(10000.00));

        ResponseContributionDto responseContributionDto = new ResponseContributionDto("5678", "lenderAccountId_Test",
                "Tester1", "Tester2", "1234",

                BigDecimal.valueOf(10000.00), LocalDate.now(), LocalDate.now().plusDays(30),
                TransactionStatus.ACCEPTED);

        when(contributionService.createContribution(requestContributionDto)).thenReturn(responseContributionDto);

        mockMvc.perform(post("/api/microcreditos/contribuir")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestContributionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", Matchers.is(responseContributionDto.id())))
                .andExpect(jsonPath("$.lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                .andExpect(jsonPath("$.lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                .andExpect(jsonPath("$.borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                .andExpect(jsonPath("$.microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                .andExpect(jsonPath("$.amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                .andExpect(jsonPath("$.createdDate", Matchers.is(responseContributionDto.createdDate().toString())))
                .andExpect(jsonPath("$.expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().toString())))
                .andExpect(jsonPath("$.transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));
    }

    @Test
    public void getBy_ShouldReturnOk() throws Exception {
        ResponseMicrocreditGetDto responseMicrocreditGetDto = new ResponseMicrocreditGetDto("1234", "5678",
                BigDecimal.valueOf(10000.00), BigDecimal.valueOf(100.00), LocalDate.now(), LocalDate.now().plusDays(30),
                "Test title", "Test Description", TransactionStatus.COMPLETED, List.of());

        when(microcreditService.getBy("COMPLETED")).thenReturn(List.of(responseMicrocreditGetDto));

        mockMvc.perform(get("/api/microcreditos/estado/COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].id", Matchers.is(responseMicrocreditGetDto.id())))
                .andExpect(jsonPath("$[0].borrowerAccountId", Matchers.is(responseMicrocreditGetDto.borrowerAccountId())))
                .andExpect(jsonPath("$[0].amount", Matchers.is(responseMicrocreditGetDto.amount().doubleValue())))
                .andExpect(jsonPath("$[0].remainingAmount", Matchers.is(responseMicrocreditGetDto.remainingAmount().doubleValue())))
                .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseMicrocreditGetDto.createdDate().toString())))
                .andExpect(jsonPath("$[0].expirationDate", Matchers.is(responseMicrocreditGetDto.expirationDate().toString())))
                .andExpect(jsonPath("$[0].title", Matchers.is(responseMicrocreditGetDto.title())))
                .andExpect(jsonPath("$[0].description", Matchers.is(responseMicrocreditGetDto.description())))
                .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseMicrocreditGetDto.transactionStatus().toString())))
                .andExpect(jsonPath("$[0].contributions", Matchers.hasSize(0)));
    }

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
}



/*
// Andando! habría que limpiar de try/catch los métodos y usar sólo los de GlobalException.
    @Test
    public void createMicrocredit_ShouldNotReturnOk() throws Exception {
        RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto("Test title", "Test Description",
                BigDecimal.valueOf(100000.00), LocalDate.of(2024, 10, 17));

        when(microcreditService.createMicrocredit(any())).thenThrow(new ValidationException("Usuario no encontrado"));

        mockMvc.perform(post("/api/microcreditos/solicitar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestMicrocreditDto)))
                .andExpect(status().isBadRequest());
    }

// Andando! Revisar la excepción y mensaje que se quiera devolver... pero debería ser lo único (?
@Test
    public void getOne_ShouldNotReturnOk() throws Exception {
        when(microcreditService.getOne(any())).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));
        mockMvc.perform(get("/api/microcreditos/1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", Matchers.is("Usuario no encontrado")));
* */