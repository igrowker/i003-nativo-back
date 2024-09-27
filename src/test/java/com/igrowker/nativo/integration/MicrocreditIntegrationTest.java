package com.igrowker.nativo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = {"USER"})
public class MicrocreditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @WithMockUser(username = "pame6_46@hotmail.com", roles = "USER")
    public void createMicrocredit_ShouldReturnOk() throws Exception {
        // Mock Data: Request DTO
        RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto(
                "Test title",
                "Test Description",
                BigDecimal.valueOf(100000.00)
        );

        // Mock Data: Borrower User
        User borrowerUser = new User();
        borrowerUser.setDni(12345678L);
        borrowerUser.setEmail("pame6_46@hotmail.com");
        borrowerUser.setName("Pamela");
        borrowerUser.setSurname("Zampieri");
        borrowerUser.setPassword("securePassword123");
        borrowerUser.setPhone("1234567890");
        borrowerUser.setBirthday(LocalDate.of(1990, 1, 1));
        borrowerUser.setEnabled(true);
        borrowerUser.setAccountNonExpired(true);
        borrowerUser.setAccountNonLocked(true);
        borrowerUser.setCredentialsNonExpired(true);

        borrowerUser = userRepository.save(borrowerUser);

        Account borrowerAccount = new Account();
        borrowerAccount.setAccountNumber(123456789L);
        borrowerAccount.setAmount(BigDecimal.valueOf(500000.00));
        borrowerAccount.setEnabled(true);
        borrowerAccount.setUserId(borrowerUser.getId());
        borrowerAccount.setReservedAmount(BigDecimal.ZERO);

        borrowerAccount = accountRepository.save(borrowerAccount);

        borrowerUser.setAccountId(borrowerAccount.getId());
        userRepository.save(borrowerUser);

        assertTrue(userRepository.findByEmail("pame6_46@hotmail.com").isPresent(),
                "El usuario debería haberse guardado");
        assertTrue(accountRepository.findByUserId(borrowerUser.getId()).isPresent(),
                "La cuenta debería haberse guardado");

        mockMvc.perform(post("/api/microcreditos/solicitar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestMicrocreditDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(requestMicrocreditDto.amount().doubleValue()))
                .andExpect(jsonPath("$.remainingAmount").value(requestMicrocreditDto.amount().doubleValue()))
                .andExpect(jsonPath("$.createdDate").exists())
                .andExpect(jsonPath("$.expirationDate").exists())
                .andExpect(jsonPath("$.title").value(requestMicrocreditDto.title()))
                .andExpect(jsonPath("$.description").value(requestMicrocreditDto.description()))
                .andExpect(jsonPath("$.transactionStatus").value(TransactionStatus.PENDING.toString()));
    }
}
