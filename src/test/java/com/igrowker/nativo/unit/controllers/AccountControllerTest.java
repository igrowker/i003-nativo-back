package com.igrowker.nativo.unit.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.nativo.controllers.AccountController;
import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.dtos.account.ResponseOtherAccountDto;
import com.igrowker.nativo.dtos.account.ResponseSelfAccountDto;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.AccountService;

@WithMockUser
@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper();

    @Nested
    class CreateAccountTests {
        @Test
        public void addAmountAccountShouldBeOk() throws Exception {
            var addAmountAccountDto = new AddAmountAccountDto("existingId", BigDecimal.valueOf(2.50));

            var responseSelfAccountDto = new ResponseSelfAccountDto("existingId", 1234L,
                    BigDecimal.valueOf(2.50), BigDecimal.ZERO, "existingUserId");

            when(accountService.addAmount(any())).thenReturn(responseSelfAccountDto);

            mockMvc.perform(post("/api/cuenta/agregar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(addAmountAccountDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(responseSelfAccountDto.id())))
                .andExpect(jsonPath("$.accountNumber", Matchers.is(responseSelfAccountDto.accountNumber().intValue())))
                .andExpect(jsonPath("$.amount", Matchers.is(responseSelfAccountDto.amount().doubleValue())))
                .andExpect(jsonPath("$.userId", Matchers.is(responseSelfAccountDto.userId())));
        }   

        @Test
        public void addAmountShouldBeNotFound() throws Exception {
            var addAmountAccountDto = new AddAmountAccountDto("notExistingId", BigDecimal.valueOf(2.50));
        
            when(accountService.addAmount(any())).thenThrow(new ResourceNotFoundException("Account not found"));
        
            mockMvc.perform(post("/api/cuenta/agregar")
                    .with(csrf()) 
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(addAmountAccountDto))
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ReadSelfAccountTests {
        @Test
        public void readSelfAccountShouldBeOk() throws Exception {
            var id = "randomId";
            var responseSelfAccountDto = new ResponseSelfAccountDto("randomId", 1234L,
                    BigDecimal.valueOf(2.50),  BigDecimal.ZERO, "randomId2");
            when(accountService.readSelfAccount(id)).thenReturn(responseSelfAccountDto);

            mockMvc.perform(get("/api/cuenta/consultar-saldo/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(responseSelfAccountDto.id())))
                    .andExpect(jsonPath("$.accountNumber", Matchers.is(responseSelfAccountDto.accountNumber().intValue())))
                    .andExpect(jsonPath("$.amount", Matchers.comparesEqualTo(responseSelfAccountDto.amount().doubleValue())))
                    .andExpect(jsonPath("$.userId", Matchers.is(responseSelfAccountDto.userId())));
        }

        @Test
        public void readSelfAccountShouldNotbeOkBadCredentials() throws Exception {
            var id = "randomId";
            when(accountService.readSelfAccount(id)).thenThrow(
                new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación")
            );

            mockMvc.perform(get("/api/cuenta/consultar-saldo/{id}", id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("La cuenta indicada no coincide con el usuario logueado en la aplicación"));
        }

        @Test
        public void readSelfAccountShouldNotBeOkNotFound() throws Exception {
            var id = "notexisting";
            when(accountService.readSelfAccount(id)).thenThrow(
                new ResourceNotFoundException("Account not found")
            );

            mockMvc.perform(get("/api/cuenta/consultar-saldo/{id}", id)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Account not found"));
        }
    }


    @Nested
    class ReadOtherAccountTests {
        @Test
        public void ReadOtherAccountShouldBeOk() throws Exception {
            var accountId = "randomId";
            var ResponseOtherAccountDto = new ResponseOtherAccountDto("randomId", 1234L, "randomId2");

            when(accountService.readOtherAccount(accountId)).thenReturn(ResponseOtherAccountDto);

            mockMvc.perform(get("/api/cuenta/consultar/{id}", accountId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ResponseOtherAccountDto))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(ResponseOtherAccountDto.id())))
                    .andExpect(jsonPath("$.accountNumber", Matchers.is(ResponseOtherAccountDto.accountNumber().intValue())))
                    .andExpect(jsonPath("$.userId", Matchers.is(ResponseOtherAccountDto.userId())));
        }

        @Test
        public void readOtherAccountShouldNotBeOk() throws Exception {
            String accountId = "nonexistent";
            when(accountService.readOtherAccount(accountId)
            ).thenThrow(
                new ResourceNotFoundException("Account not found")
            );

            mockMvc.perform(get("/api/cuenta/consultar/{id}", accountId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}


