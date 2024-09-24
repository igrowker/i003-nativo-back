package com.igrowker.nativo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.services.MicrocreditService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(value = MicrocreditController.class)
@WithMockUser
class MicrocreditControllerTest {

    @MockBean
    private MicrocreditService microcreditService;

    @MockBean
    private ContributionService contributionService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    @Test
    void createMicrocredit() {
    }

    @Test
    void getOne() {
    }

    @Test
    void getAll() throws Exception {
        var responseMicrocreditGetDto = new ResponseMicrocreditGetDto("microcredits",
                "borrower1",
                BigDecimal.valueOf(10000.00),
                BigDecimal.valueOf(10000.00),
                LocalDate.of(2024,9,17),
                LocalDate.of(2024,10,17),
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
    void getMicrocreditsByTransactionStatus() {
    }

    @Test
    void createContribution() {
    }

    @Test
    void getBy() {
    }

    @Test
    void payMicrocredit() {
    }
}