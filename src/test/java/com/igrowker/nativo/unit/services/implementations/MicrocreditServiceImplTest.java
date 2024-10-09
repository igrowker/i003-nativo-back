package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.ContributionMapper;
import com.igrowker.nativo.mappers.MicrocreditMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.services.implementation.ContributionServiceImpl;
import com.igrowker.nativo.services.implementation.MicrocreditServiceImpl;
import com.igrowker.nativo.utils.DateFormatter;
import com.igrowker.nativo.utils.NotificationService;
import com.igrowker.nativo.validations.Validations;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MicrocreditServiceImplTest {

    @Mock
    private MicrocreditRepository microcreditRepository;
    @Mock
    private ContributionRepository contributionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private MicrocreditMapper microcreditMapper;
    @Mock
    private ContributionMapper contributionMapper;
    @Mock
    private Validations validations;
    @Mock
    private DateFormatter dateFormatter;
    @InjectMocks
    private MicrocreditServiceImpl microcreditServiceImpl;
    @InjectMocks
    private ContributionServiceImpl contributionServiceImpl;
    @Mock
    private NotificationService notificationService;

    @Nested
    class CreateMicrocreditsTests {
        @Test
        public void createMicrocredit_ShouldReturnOk() throws Exception {
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto("Test title", "Test Description",
                    BigDecimal.valueOf(100000.00));

            Microcredit microcredit = new Microcredit("microcreditId", "borrowerAccountId", requestMicrocreditDto.amount(),
                    requestMicrocreditDto.amount(), BigDecimal.valueOf(110000.00), BigDecimal.ZERO, BigDecimal.ZERO,
                    requestMicrocreditDto.title(), requestMicrocreditDto.description(),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    LocalDateTime.of(2024, 9, 17, 18, 20), 1,
                    BigDecimal.valueOf(10.00), TransactionStatus.PENDING, List.of());

            ResponseMicrocreditDto responseMicrocreditDto = new ResponseMicrocreditDto("1234",
                    microcredit.getAmount(), microcredit.getAmountFinal(), microcredit.getRemainingAmount(),
                    microcredit.getCreatedDate(), microcredit.getExpirationDate(), microcredit.getTitle(),
                    microcredit.getDescription(), microcredit.getInstallmentCount(), microcredit.getInterestRate(),
                    microcredit.getTransactionStatus());

            Account account = new Account();
            account.setAccountNumber(1234567L);

            User user = new User();
            user.setEmail("test@example.com");
            user.setName("John");
            user.setSurname("Doe");

            when(microcreditMapper.requestDtoToMicrocredit(any())).thenReturn(microcredit);
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(user, account));
            when(microcreditRepository.save(any())).thenReturn(microcredit);
            when(microcreditMapper.responseDtoToMicrocredit(microcredit)).thenReturn(responseMicrocreditDto);

            ResponseMicrocreditDto response = microcreditServiceImpl.createMicrocredit(requestMicrocreditDto);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(responseMicrocreditDto.id());
            assertThat(response.amount()).isEqualTo(responseMicrocreditDto.amount());
            assertThat(response.amountFinal()).isEqualTo(responseMicrocreditDto.amountFinal());
            assertThat(response.remainingAmount()).isEqualTo(responseMicrocreditDto.remainingAmount());
            assertThat(response.createdDate()).isEqualTo(responseMicrocreditDto.createdDate());
            assertThat(response.expirationDate()).isEqualTo(responseMicrocreditDto.expirationDate());
            assertThat(response.title()).isEqualTo(responseMicrocreditDto.title());
            assertThat(response.description()).isEqualTo(responseMicrocreditDto.description());
            assertThat(response.installmentCount()).isEqualTo(responseMicrocreditDto.installmentCount());
            assertThat(response.interestRate()).isEqualTo(responseMicrocreditDto.interestRate());
            assertThat(response.transactionStatus()).isEqualTo(responseMicrocreditDto.transactionStatus());

            verify(microcreditRepository, times(1)).save(any());
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(microcreditMapper, times(1)).requestDtoToMicrocredit(any());
            verify(microcreditMapper, times(1)).responseDtoToMicrocredit(microcredit);

            verify(notificationService, times(1)).sendPaymentNotification(
                    eq(user.getEmail()),
                    eq(user.getName() + " " + user.getSurname()),
                    eq(microcredit.getAmount()),
                    eq("Microcrédito Creado"),
                    contains("tu microcrédito con ID: " + microcredit.getId() + " ha sido creado exitosamente."),
                    contains("Gracias por participar en nuestro programa de microcréditos.")
            );
        }

        @Test
        public void createMicrocredit_ShouldReturnNotOk() throws Exception {
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto("Test title", "Test Description",
                    BigDecimal.valueOf(600000.00));

            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("La cuenta indicada no coincide con el usuario logueado en la aplicación"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                microcreditServiceImpl.createMicrocredit(requestMicrocreditDto);
            });

            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }
}
