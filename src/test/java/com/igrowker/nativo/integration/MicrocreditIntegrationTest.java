package com.igrowker.nativo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.utils.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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

    @MockBean
    private MicrocreditRepository microcreditRepository;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AccountRepository accountRepository;

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @WithMockUser(username = "test1@gmail.com", roles = "USER")
    public void createMicrocredit_ShouldReturnOk() throws Exception {
        // Mock Data: Request DTO
        RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto(
                "Test title",
                "Test Description",
                BigDecimal.valueOf(100000.00)
        );

        // Mock User
        User borrowerUserMock = mock(User.class);
        when(borrowerUserMock.getEmail()).thenReturn("test1@gmail.com");
        when(borrowerUserMock.getName()).thenReturn("Pedro");
        when(borrowerUserMock.getSurname()).thenReturn("Barbano");
        when(borrowerUserMock.getPassword()).thenReturn("securePassword123");
        when(borrowerUserMock.getPhone()).thenReturn("1234567890");
        when(borrowerUserMock.getBirthday()).thenReturn(LocalDate.of(1990, 1, 1));
        when(borrowerUserMock.getId()).thenReturn("123456789");

        // Mock Account
        Account borrowerAccountMock = mock(Account.class);
        when(borrowerAccountMock.getId()).thenReturn("123456789");
        when(borrowerAccountMock.getAmount()).thenReturn(BigDecimal.valueOf(500000.00));
        when(borrowerAccountMock.getAccountNumber()).thenReturn(123456789L);
        when(borrowerAccountMock.getReservedAmount()).thenReturn(BigDecimal.valueOf(0.00));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(borrowerUserMock));

        when(accountRepository.findByUserId(borrowerUserMock.getId())).thenReturn(Optional.of(borrowerAccountMock));

        Microcredit microcreditMock = new Microcredit();
        microcreditMock.setId("testID123");
        microcreditMock.setBorrowerAccountId("123456789");
        microcreditMock.setAmount(BigDecimal.valueOf(100000.00));
        microcreditMock.setRemainingAmount(BigDecimal.valueOf(100000.00));
        microcreditMock.setTitle("Test title");
        microcreditMock.setDescription("Test Description");
        microcreditMock.setExpirationDate(LocalDate.now().plusDays(30));
        microcreditMock.setCreatedDate(LocalDate.now());
        microcreditMock.setTransactionStatus(TransactionStatus.PENDING);

        when(microcreditRepository.save(any(Microcredit.class))).thenReturn(microcreditMock);

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

        verify(notificationService, times(1)).sendPaymentNotification(
                eq("test1@gmail.com"),
                eq("Pedro Barbano"),
                eq(BigDecimal.valueOf(100000.00)),
                eq("Microcrédito Creado"),
                contains("ha sido creado exitosamente."),
                contains("Gracias por participar en nuestro programa de microcréditos.")
        );
    }
}
