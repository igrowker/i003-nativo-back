package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditPaymentDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.ContributionMapper;
import com.igrowker.nativo.mappers.MicrocreditMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.implementation.ContributionServiceImpl;
import com.igrowker.nativo.services.implementation.MicrocreditServiceImpl;
import com.igrowker.nativo.utils.DateFormatter;
import com.igrowker.nativo.utils.GeneralTransactions;
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
import java.util.Optional;

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
    private UserRepository userRepository;
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
    @Mock
    private GeneralTransactions generalTransactions;

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

    @Nested
    class PayMicrocreditsTests {
        @Test
        public void payMicrocredit_ShouldReturnOk() throws Exception {
            String microcreditId = "microcreditId";
            BigDecimal amount = BigDecimal.valueOf(100000.00);
            BigDecimal interestRate = BigDecimal.valueOf(10.00);
            BigDecimal contributionAmount = BigDecimal.valueOf(50000.00);
            BigDecimal interest = contributionAmount.multiply(interestRate).divide(BigDecimal.valueOf(100));
            BigDecimal totalContributionAmountWithInterest = contributionAmount.add(interest);

            // Usuario autenticado (Prestatario)
            Account borrowerAccount = new Account();
            borrowerAccount.setId("borrowerAccountId");

            User borrowerUser = new User();
            borrowerUser.setEmail("borrower@example.com");
            borrowerUser.setName("John");
            borrowerUser.setSurname("Doe");

            Validations.UserAccountPair userBorrower = new Validations.UserAccountPair(borrowerUser, borrowerAccount);

            Contribution contribution = new Contribution();
            contribution.setAmount(contributionAmount);
            contribution.setLenderAccountId("lenderAccountId");
            contribution.setTransactionStatus(TransactionStatus.PENDING);

            Microcredit microcredit = new Microcredit();
            microcredit.setId(microcreditId);
            microcredit.setBorrowerAccountId(borrowerAccount.getId());
            microcredit.setAmount(amount);
            microcredit.setInterestRate(interestRate);
            microcredit.setContributions(List.of(contribution));
            microcredit.setTransactionStatus(TransactionStatus.PENDING);

            // Usuario prestamista (Contribuyente)
            Account lenderAccount = new Account();
            lenderAccount.setId("lenderAccountId");

            User lenderUser = new User();
            lenderUser.setEmail("lender@example.com");
            lenderUser.setName("Jane");
            lenderUser.setSurname("Doe");

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userBorrower);
            when(microcreditRepository.findById(microcreditId)).thenReturn(Optional.of(microcredit));
            when(validations.isUserAccountMismatch(borrowerAccount.getId())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findById("lenderAccountId")).thenReturn(Optional.of(lenderAccount));
            when(userRepository.findById(lenderAccount.getUserId())).thenReturn(Optional.of(lenderUser));

            doNothing().when(generalTransactions).updateBalances(
                    eq(microcredit.getBorrowerAccountId()),
                    eq(contribution.getLenderAccountId()),
                    eq(totalContributionAmountWithInterest)
            );

            when(microcreditRepository.save(any(Microcredit.class))).thenAnswer(invocation -> {
                Microcredit savedMicrocredit = invocation.getArgument(0); // Obtenemos el microcrédito que se intenta guardar
                savedMicrocredit.setId(microcreditId); // Aseguramos que tenga un ID no nulo
                return savedMicrocredit; // Lo devolvemos como el microcrédito guardado
            });

            ResponseMicrocreditPaymentDto response = microcreditServiceImpl.payMicrocredit(microcreditId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(microcreditId);
            assertThat(response.totalPaidAmount()).isEqualTo(totalContributionAmountWithInterest);

            verify(microcreditRepository, times(1)).save(microcredit);
            verify(notificationService, times(1)).sendPaymentNotification(
                    eq(lenderUser.getEmail()),
                    eq(lenderUser.getName() + " " + lenderUser.getSurname()),
                    eq(contributionAmount),
                    eq("Devolución cuota microcrédito"),
                    contains("tu contribución al microcrédito con ID: " + microcreditId),
                    contains("Gracias por tu participación")
            );
            verify(notificationService, times(1)).sendPaymentNotification(
                    eq(borrowerUser.getEmail()),
                    eq(borrowerUser.getName() + " " + borrowerUser.getSurname()),
                    eq(totalContributionAmountWithInterest),
                    eq("Descuento cuota del microcrédito"),
                    contains("el descuento por el microcrédito con ID: " + microcreditId),
                    contains("se deducirá automáticamente en tu próximo ingreso")
            );
        }

        @Test
        public void payMicrocredit_ShouldThrowException_WhenInsufficientFunds() throws Exception {
            String microcreditId = "microcreditId";
            BigDecimal amount = BigDecimal.valueOf(100000.00);
            BigDecimal interestRate = BigDecimal.valueOf(10.00);
            BigDecimal contributionAmount = BigDecimal.valueOf(50000.00);
            BigDecimal interest = contributionAmount.multiply(interestRate).divide(BigDecimal.valueOf(100));
            BigDecimal totalContributionAmountWithInterest = contributionAmount.add(interest);

            // Usuario autenticado (Prestatario)
            Account borrowerAccount = new Account();
            borrowerAccount.setId("borrowerAccountId");

            User borrowerUser = new User();
            borrowerUser.setEmail("borrower@example.com");
            borrowerUser.setName("John");
            borrowerUser.setSurname("Doe");

            Validations.UserAccountPair userBorrower = new Validations.UserAccountPair(borrowerUser, borrowerAccount);

            Contribution contribution = new Contribution();
            contribution.setAmount(contributionAmount);
            contribution.setLenderAccountId("lenderAccountId");
            contribution.setTransactionStatus(TransactionStatus.PENDING);

            Microcredit microcredit = new Microcredit();
            microcredit.setId(microcreditId);
            microcredit.setBorrowerAccountId(borrowerAccount.getId());
            microcredit.setAmount(amount);
            microcredit.setInterestRate(interestRate);
            microcredit.setContributions(List.of(contribution));
            microcredit.setTransactionStatus(TransactionStatus.PENDING);

            // Usuario prestamista (Contribuyente)
            Account lenderAccount = new Account();
            lenderAccount.setId("lenderAccountId");

            User lenderUser = new User();
            lenderUser.setEmail("lender@example.com");
            lenderUser.setName("Jane");
            lenderUser.setSurname("Doe");

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userBorrower);
            when(microcreditRepository.findById(microcreditId)).thenReturn(Optional.of(microcredit));
            when(validations.isUserAccountMismatch(borrowerAccount.getId())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(false);

            Exception exception = assertThrows(InsufficientFundsException.class, () -> {
                microcreditServiceImpl.payMicrocredit(microcreditId);
            });

            assertThat(exception.getMessage()).isEqualTo("Fondos insuficientes");

            verify(microcreditRepository, never()).save(any());

            verify(notificationService, never()).sendPaymentNotification(
                    anyString(),
                    anyString(),
                    any(),
                    anyString(),
                    anyString(),
                    anyString()
            );

            verify(generalTransactions, never()).updateBalances(
                    anyString(),
                    anyString(),
                    any()
            );
        }
    }
}
