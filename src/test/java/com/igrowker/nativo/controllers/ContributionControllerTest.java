package com.igrowker.nativo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.nativo.dtos.contribution.ResponseContributionGetDto;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.ContributionService;
import org.hamcrest.Matchers;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ContributionController.class)
@WithMockUser
public class ContributionControllerTest {
    @MockBean
    private ContributionService contributionService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    public void getOneContribution_ShouldReturnOk() throws Exception {
        ResponseContributionGetDto responseContributionGetDto = new ResponseContributionGetDto("1111", "5678",
                "Test1", "Test2", "1234",
                BigDecimal.valueOf(10000.00), LocalDate.now(), TransactionStatus.ACCEPTED);

        when(contributionService.getOneContribution("1111")).thenReturn(responseContributionGetDto);

        mockMvc.perform(get("/api/contribuciones/1111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(responseContributionGetDto.id())))
                .andExpect(jsonPath("$.lenderAccountId", Matchers.is(responseContributionGetDto.lenderAccountId())))
                .andExpect(jsonPath("$.lenderFullname", Matchers.is(responseContributionGetDto.lenderFullname())))
                .andExpect(jsonPath("$.borrowerFullname", Matchers.is(responseContributionGetDto.borrowerFullname())))
                .andExpect(jsonPath("$.microcreditId", Matchers.is(responseContributionGetDto.microcreditId())))
                .andExpect(jsonPath("$.amount", Matchers.is(responseContributionGetDto.amount().doubleValue())))
                .andExpect(jsonPath("$.createdDate", Matchers.is(responseContributionGetDto.createdDate().toString())))
                .andExpect(jsonPath("$.transactionStatus", Matchers.is(responseContributionGetDto.transactionStatus().toString())));
    }

    @Test
    public void getAll_ShouldReturnOk() throws Exception {
        ResponseContributionGetDto responseContributionGetDto = new ResponseContributionGetDto("1111", "5678",
                "Test1", "Test2", "1234",
                BigDecimal.valueOf(10000.00), LocalDate.now(), TransactionStatus.ACCEPTED);

        when(contributionService.getAll()).thenReturn(List.of(responseContributionGetDto));

        mockMvc.perform(get("/api/contribuciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].id", Matchers.is(responseContributionGetDto.id())))
                .andExpect(jsonPath("$[0].lenderAccountId", Matchers.is(responseContributionGetDto.lenderAccountId())))
                .andExpect(jsonPath("$[0].lenderFullname", Matchers.is(responseContributionGetDto.lenderFullname())))
                .andExpect(jsonPath("$[0].borrowerFullname", Matchers.is(responseContributionGetDto.borrowerFullname())))
                .andExpect(jsonPath("$[0].microcreditId", Matchers.is(responseContributionGetDto.microcreditId())))
                .andExpect(jsonPath("$[0].amount", Matchers.is(responseContributionGetDto.amount().doubleValue())))
                .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseContributionGetDto.createdDate().toString())))
                .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseContributionGetDto.transactionStatus().toString())));
    }

    @Test
    public void getContributionsByTransactionStatus_ShouldReturnOk() throws Exception {
        ResponseContributionGetDto responseContributionGetDto = new ResponseContributionGetDto("1111", "5678",
                "Test1", "Test2", "1234",
                BigDecimal.valueOf(10000.00), LocalDate.now(), TransactionStatus.ACCEPTED);

        when(contributionService.getContributionsByTransactionStatus("ACCEPTED"))
                .thenReturn(List.of(responseContributionGetDto));

        mockMvc.perform(get("/api/contribuciones/historial-estados/ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].id", Matchers.is(responseContributionGetDto.id())))
                .andExpect(jsonPath("$[0].lenderAccountId", Matchers.is(responseContributionGetDto.lenderAccountId())))
                .andExpect(jsonPath("$[0].lenderFullname", Matchers.is(responseContributionGetDto.lenderFullname())))
                .andExpect(jsonPath("$[0].borrowerFullname", Matchers.is(responseContributionGetDto.borrowerFullname())))
                .andExpect(jsonPath("$[0].microcreditId", Matchers.is(responseContributionGetDto.microcreditId())))
                .andExpect(jsonPath("$[0].amount", Matchers.is(responseContributionGetDto.amount().doubleValue())))
                .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseContributionGetDto.createdDate().toString())))
                .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseContributionGetDto.transactionStatus().toString())));
    }
}
