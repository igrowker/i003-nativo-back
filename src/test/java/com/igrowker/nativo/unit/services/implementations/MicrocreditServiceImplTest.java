package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
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
import org.junit.jupiter.api.BeforeEach;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    private Microcredit microcredit;
    private Contribution contribution;
    private ResponseMicrocreditGetDto responseMicrocreditGetDto;
    private User borrowerUser;
    private User lenderUser;
    private Account borrowerAccount;
    private Account lenderAccount;

    @BeforeEach
    public void setUp() {
        borrowerUser = new User();
        borrowerUser.setEmail("borrower@example.com");
        borrowerUser.setName("John");
        borrowerUser.setSurname("Doe");

        borrowerAccount = new Account();
        borrowerAccount.setId("borrowerAccountId");
        borrowerAccount.setAccountNumber(1234567L);

        lenderUser = new User();
        lenderUser.setEmail("lender@example.com");
        lenderUser.setName("Jane");
        lenderUser.setSurname("Doe");

        lenderAccount = new Account();
        lenderAccount.setId("lenderAccountId");

        microcredit = new Microcredit("microcreditId", borrowerAccount.getId(),
                BigDecimal.valueOf(100000.00), BigDecimal.valueOf(100000.00),
                BigDecimal.valueOf(110000.00), BigDecimal.ZERO, BigDecimal.ZERO,
                "Test title", "Test Description",
                LocalDateTime.of(2024, 10, 17, 18, 20),
                LocalDateTime.of(2024, 9, 17, 18, 20), 1,
                BigDecimal.valueOf(10.00), TransactionStatus.PENDING, List.of());

        responseMicrocreditGetDto = new ResponseMicrocreditGetDto(microcredit.getId(),
                microcredit.getBorrowerAccountId(), microcredit.getAmount(), microcredit.getRemainingAmount(),
                microcredit.getCreatedDate(), microcredit.getExpirationDate(), microcredit.getTitle(),
                microcredit.getDescription(), microcredit.getTransactionStatus(), List.of());

        contribution = new Contribution();
        contribution.setAmount(BigDecimal.valueOf(50000.00));
        contribution.setLenderAccountId(lenderAccount.getId());
        contribution.setTransactionStatus(TransactionStatus.PENDING);
    }

    @Nested
    class CreateMicrocreditsTests {
        @Test
        public void createMicrocredit_ShouldReturnOk() throws Exception {
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto("Test title", "Test Description",
                    BigDecimal.valueOf(100000.00));

            ResponseMicrocreditDto responseMicrocreditDto = new ResponseMicrocreditDto("1234",
                    microcredit.getAmount(), microcredit.getAmountFinal(), microcredit.getRemainingAmount(),
                    microcredit.getCreatedDate(), microcredit.getExpirationDate(), microcredit.getTitle(),
                    microcredit.getDescription(), microcredit.getInstallmentCount(), microcredit.getInterestRate(),
                    microcredit.getTransactionStatus());

            when(microcreditMapper.requestDtoToMicrocredit(any())).thenReturn(microcredit);
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
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
                    eq(borrowerUser.getEmail()),
                    eq(borrowerUser.getName() + " " + borrowerUser.getSurname()),
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
            BigDecimal interest = contribution.getAmount().multiply(microcredit.getInterestRate()).divide(BigDecimal.valueOf(100));
            BigDecimal totalContributionAmountWithInterest = contribution.getAmount().add(interest);

            Validations.UserAccountPair userBorrower = new Validations.UserAccountPair(borrowerUser, borrowerAccount);

            microcredit.setAmount(microcredit.getAmount());
            microcredit.setInterestRate(microcredit.getInterestRate());
            microcredit.setContributions(List.of(contribution));
            microcredit.setTransactionStatus(TransactionStatus.PENDING);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userBorrower);
            when(microcreditRepository.findById(microcredit.getId())).thenReturn(Optional.of(microcredit));
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
                savedMicrocredit.setId(microcredit.getId()); // Aseguramos que tenga un ID no nulo
                return savedMicrocredit; // Lo devolvemos como el microcrédito guardado
            });

            ResponseMicrocreditPaymentDto response = microcreditServiceImpl.payMicrocredit(microcredit.getId());

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(microcredit.getId());
            assertThat(response.totalPaidAmount()).isEqualTo(totalContributionAmountWithInterest);

            verify(microcreditRepository, times(1)).save(microcredit);
            verify(notificationService, times(1)).sendPaymentNotification(
                    eq(lenderUser.getEmail()),
                    eq(lenderUser.getName() + " " + lenderUser.getSurname()),
                    eq(contribution.getAmount()),
                    eq("Devolución cuota microcrédito"),
                    contains("tu contribución al microcrédito con ID: " + microcredit.getId()),
                    contains("Gracias por tu participación")
            );
            verify(notificationService, times(1)).sendPaymentNotification(
                    eq(borrowerUser.getEmail()),
                    eq(borrowerUser.getName() + " " + borrowerUser.getSurname()),
                    eq(totalContributionAmountWithInterest),
                    eq("Descuento cuota del microcrédito"),
                    contains("el descuento por el microcrédito con ID: " + microcredit.getId()),
                    contains("se deducirá automáticamente en tu próximo ingreso")
            );
        }

        @Test
        public void payMicrocredit_ShouldThrowException_WhenInsufficientFunds() throws Exception {
            BigDecimal interest = contribution.getAmount().multiply(microcredit.getInterestRate()).divide(BigDecimal.valueOf(100));
            BigDecimal totalContributionAmountWithInterest = contribution.getAmount().add(interest);

            Validations.UserAccountPair userBorrower = new Validations.UserAccountPair(borrowerUser, borrowerAccount);

            microcredit.setAmount(microcredit.getAmount());
            microcredit.setInterestRate(microcredit.getInterestRate());
            microcredit.setContributions(List.of(contribution));
            microcredit.setTransactionStatus(TransactionStatus.PENDING);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userBorrower);
            when(microcreditRepository.findById(microcredit.getId())).thenReturn(Optional.of(microcredit));
            when(validations.isUserAccountMismatch(borrowerAccount.getId())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(false);

            Exception exception = assertThrows(InsufficientFundsException.class, () -> {
                microcreditServiceImpl.payMicrocredit(microcredit.getId());
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

    @Nested
    class GetAllMicrocreditsByUserTests {
        @Test
        public void getAllMicrocreditsByUser_ShouldReturnOk() throws Exception {
            List<Microcredit> microcreditList = List.of(microcredit);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
            when(microcreditRepository.findAllByBorrowerAccountId(microcredit.getBorrowerAccountId())).thenReturn(microcreditList);
            when(microcreditMapper.responseMicrocreditGet(any(Microcredit.class), anyList())).thenReturn(responseMicrocreditGetDto);

            List<ResponseMicrocreditGetDto> actualResponse = microcreditServiceImpl.getAllMicrocreditsByUser();

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).hasSize(1);
            assertThat(actualResponse.get(0)).usingRecursiveComparison().isEqualTo(responseMicrocreditGetDto);

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(microcreditRepository, times(1)).findAllByBorrowerAccountId(microcredit.getBorrowerAccountId());
            verify(microcreditMapper, times(1)).responseMicrocreditGet(any(Microcredit.class), anyList());
        }

        @Test
        public void getAllMicrocreditsByUser_ShouldReturnNotFound() throws Exception {
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
            when(microcreditRepository.findAllByBorrowerAccountId(borrowerAccount.getId())).thenReturn(List.of());

            assertThatThrownBy(() -> microcreditServiceImpl.getAllMicrocreditsByUser())
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No se encontraron microcréditos.");

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(microcreditRepository, times(1)).findAllByBorrowerAccountId(borrowerAccount.getId());
        }
    }

    @Nested
    class GetAllMicrocreditsByUserByStatusTests {
        @Test
        public void getAllMicrocreditsByUserByStatus_ShouldReturnOk_WhenMicrocreditsFound() throws Exception {
            String transactionStatus = TransactionStatus.PENDING.name();
            List<Microcredit> microcreditList = List.of(microcredit);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
            when(validations.statusConvert(transactionStatus)).thenReturn(TransactionStatus.PENDING);
            when(microcreditRepository.findByTransactionStatusAndBorrowerAccountId(TransactionStatus.PENDING, borrowerAccount.getId())).thenReturn(microcreditList);
            when(microcreditMapper.responseMicrocreditGet(any(Microcredit.class), anyList())).thenReturn(responseMicrocreditGetDto);

            List<ResponseMicrocreditGetDto> actualResponse = microcreditServiceImpl.getAllMicrocreditsByUserByStatus(transactionStatus);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).hasSize(1);
            assertThat(actualResponse.get(0)).usingRecursiveComparison().isEqualTo(responseMicrocreditGetDto);

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(microcreditRepository, times(1)).findByTransactionStatusAndBorrowerAccountId(TransactionStatus.PENDING, borrowerAccount.getId());
            verify(microcreditMapper, times(1)).responseMicrocreditGet(any(Microcredit.class), anyList());
        }

        @Test
        public void getAllMicrocreditsByUserByStatus_ShouldThrowResourceNotFoundException_WhenNoMicrocreditsFound() throws Exception {
            String transactionStatus = TransactionStatus.PENDING.name();

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
            when(validations.statusConvert(transactionStatus)).thenReturn(TransactionStatus.PENDING);
            when(microcreditRepository.findByTransactionStatusAndBorrowerAccountId(TransactionStatus.PENDING, borrowerAccount.getId())).thenReturn(List.of()); // Lista vacía

            assertThatThrownBy(() -> microcreditServiceImpl.getAllMicrocreditsByUserByStatus(transactionStatus))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No se encontraron microcréditos para el usuario con el estado especificado.");

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(microcreditRepository, times(1)).findByTransactionStatusAndBorrowerAccountId(TransactionStatus.PENDING, borrowerAccount.getId());
        }
    }

    @Nested
    class GetMicrocreditsBetweenDatesTests {
        @Test
        public void getMicrocreditsBetweenDates_ShouldReturnOk_WhenMicrocreditsFound() throws Exception {
            String fromDate = "2024-10-01T00:00:00";
            String toDate = "2024-10-10T23:59:59";

            List<LocalDateTime> dateList = List.of(
                    LocalDateTime.of(2024, 10, 1, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59, 59)
            );

            List<Microcredit> microcreditList = List.of(microcredit);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
            when(dateFormatter.getDateFromString(fromDate, toDate)).thenReturn(dateList);
            when(microcreditRepository.findMicrocreditsBetweenDates(borrowerAccount.getId(), dateList.get(0), dateList.get(1))).thenReturn(microcreditList);
            when(microcreditMapper.responseMicrocreditGet(any(Microcredit.class), anyList())).thenReturn(responseMicrocreditGetDto);

            List<ResponseMicrocreditGetDto> actualResponse = microcreditServiceImpl.getMicrocreditsBetweenDates(fromDate, toDate);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).hasSize(1);
            assertThat(actualResponse.get(0)).usingRecursiveComparison().isEqualTo(responseMicrocreditGetDto);

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(dateFormatter, times(1)).getDateFromString(fromDate, toDate);
            verify(microcreditRepository, times(1)).findMicrocreditsBetweenDates(borrowerAccount.getId(), dateList.get(0), dateList.get(1));
            verify(microcreditMapper, times(1)).responseMicrocreditGet(any(Microcredit.class), anyList());
        }

        @Test
        public void getMicrocreditsBetweenDates_ShouldThrowResourceNotFoundException_WhenNoMicrocreditsFound() throws Exception {
            String fromDate = "2024-10-01T00:00:00";
            String toDate = "2024-10-10T23:59:59";

            List<LocalDateTime> dateList = List.of(
                    LocalDateTime.of(2024, 10, 1, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59, 59)
            );

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
            when(dateFormatter.getDateFromString(fromDate, toDate)).thenReturn(dateList);
            when(microcreditRepository.findMicrocreditsBetweenDates(borrowerAccount.getId(), dateList.get(0), dateList.get(1))).thenReturn(List.of()); // Lista vacía

            assertThatThrownBy(() -> microcreditServiceImpl.getMicrocreditsBetweenDates(fromDate, toDate))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No posee microcréditos solicitados");

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(dateFormatter, times(1)).getDateFromString(fromDate, toDate);
            verify(microcreditRepository, times(1)).findMicrocreditsBetweenDates(borrowerAccount.getId(), dateList.get(0), dateList.get(1));
        }
    }

    @Nested
    class GetMicrocreditsByDateAndStatusTests {
        @Test
        public void getMicrocreditsByDateAndStatus_ShouldReturnMicrocredits_WhenFound() throws Exception {
            String date = "2024-10-10T00:00:00";
            String transactionStatus = "PENDING";

            List<Microcredit> microcreditList = List.of(microcredit);
            ResponseContributionDto contributionDto = new ResponseContributionDto(contribution.getId(),
                    lenderAccount.getId(), "LenderFullName", "BorrowerFullName", microcredit.getId(),
                    contribution.getAmount(), contribution.getCreatedDate(), microcredit.getExpirationDate(), contribution.getTransactionStatus());
            List<ResponseContributionDto> contributionsDto = List.of(contributionDto);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
            when(validations.statusConvert(transactionStatus)).thenReturn(TransactionStatus.PENDING);
            when(dateFormatter.getDateFromString(date)).thenReturn(List.of(
                    LocalDateTime.of(2024, 10, 10, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59)
            ));
            when(microcreditRepository.findMicrocreditsByDateAndTransactionStatus(
                    borrowerAccount.getId(),
                    LocalDateTime.of(2024, 10, 10, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59),
                    TransactionStatus.PENDING)).thenReturn(microcreditList);
            when(microcreditMapper.responseMicrocreditGet(any(Microcredit.class), anyList())).thenReturn(responseMicrocreditGetDto);

            List<ResponseMicrocreditGetDto> actualResponse = microcreditServiceImpl.getMicrocreditsByDateAndStatus(date, transactionStatus);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).hasSize(1);
            assertThat(actualResponse.get(0)).usingRecursiveComparison().isEqualTo(responseMicrocreditGetDto);

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(dateFormatter, times(1)).getDateFromString(date);
            verify(microcreditRepository, times(1)).findMicrocreditsByDateAndTransactionStatus(
                    borrowerAccount.getId(),
                    LocalDateTime.of(2024, 10, 10, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59),
                    TransactionStatus.PENDING);
            verify(microcreditMapper, times(1)).responseMicrocreditGet(any(Microcredit.class), anyList());
        }

        @Test
        public void getMicrocreditsByDateAndStatus_ShouldThrowResourceNotFoundException_WhenNoMicrocreditsFound() throws Exception {
            String date = "2024-10-10T00:00:00";
            String transactionStatus = "PENDING";

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(borrowerUser, borrowerAccount));
            when(validations.statusConvert(transactionStatus)).thenReturn(TransactionStatus.PENDING);
            when(dateFormatter.getDateFromString(date)).thenReturn(List.of(
                    LocalDateTime.of(2024, 10, 10, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59)
            ));
            when(microcreditRepository.findMicrocreditsByDateAndTransactionStatus(
                    borrowerAccount.getId(),
                    LocalDateTime.of(2024, 10, 10, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59),
                    TransactionStatus.PENDING)).thenReturn(List.of());

            assertThatThrownBy(() -> microcreditServiceImpl.getMicrocreditsByDateAndStatus(date, transactionStatus))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No posee microcréditos solicitados");

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(dateFormatter, times(1)).getDateFromString(date);
            verify(microcreditRepository, times(1)).findMicrocreditsByDateAndTransactionStatus(
                    borrowerAccount.getId(),
                    LocalDateTime.of(2024, 10, 10, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59),
                    TransactionStatus.PENDING);
        }
    }

    @Nested
    class GetAllMicrocreditsTests {
        @Test
        public void getAll_ShouldReturnMicrocredits_WhenFound() throws Exception {
            List<Microcredit> microcredits = List.of(microcredit);

            when(microcreditRepository.findAll()).thenReturn(microcredits);
            when(microcreditMapper.responseMicrocreditGet(any(Microcredit.class), anyList())).thenReturn(responseMicrocreditGetDto);

            List<ResponseMicrocreditGetDto> actualResponse = microcreditServiceImpl.getAll();

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).hasSize(1);
            assertThat(actualResponse.get(0)).usingRecursiveComparison().isEqualTo(responseMicrocreditGetDto);

            verify(microcreditRepository, times(1)).findAll();
            verify(microcreditMapper, times(1)).responseMicrocreditGet(any(Microcredit.class), anyList());
        }

        @Test
        public void getAll_ShouldThrowResourceNotFoundException_WhenNoMicrocreditsFound() throws Exception {
            when(microcreditRepository.findAll()).thenReturn(List.of());

            assertThatThrownBy(() -> microcreditServiceImpl.getAll())
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No se encontraron microcréditos.");

            verify(microcreditRepository, times(1)).findAll();
            verify(microcreditMapper, never()).responseMicrocreditGet(any(Microcredit.class), anyList());
        }
    }

    @Nested
    class GetOneMicrocreditTests {
        @Test
        public void getOne_ShouldReturnMicrocredit_WhenFound() throws Exception {
            when(microcreditRepository.findById(microcredit.getId())).thenReturn(Optional.of(microcredit));
            when(microcreditMapper.responseMicrocreditGet(eq(microcredit), anyList())).thenReturn(responseMicrocreditGetDto);

            ResponseMicrocreditGetDto actualResponse = microcreditServiceImpl.getOne(microcredit.getId());

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).usingRecursiveComparison().isEqualTo(responseMicrocreditGetDto);

            verify(microcreditRepository, times(1)).findById(microcredit.getId());
            verify(microcreditMapper, times(1)).responseMicrocreditGet(eq(microcredit), anyList());
        }

        @Test
        public void getOne_ShouldThrowResourceNotFoundException_WhenNotFound() throws Exception {
            String microcreditId = "nonExistentId";
            when(microcreditRepository.findById(microcreditId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> microcreditServiceImpl.getOne(microcreditId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Microcrédito no encontrado con id: " + microcreditId);

            verify(microcreditRepository, times(1)).findById(microcreditId);
            verify(microcreditMapper, never()).responseMicrocreditGet(any(Microcredit.class), anyList());
        }
    }

    @Nested
    class GetAllMicrocreditsByTransactionStatusTests {
        @Test
        public void getMicrocreditsByTransactionStatus_ShouldReturnMicrocredits_WhenFound() throws Exception {
            String transactionStatus = "PENDING";
            TransactionStatus enumStatus = TransactionStatus.PENDING;
            List<Microcredit> microcreditList = List.of(microcredit);

            when(validations.statusConvert(transactionStatus)).thenReturn(enumStatus);
            when(microcreditRepository.findByTransactionStatus(enumStatus)).thenReturn(microcreditList);
            when(microcreditMapper.responseMicrocreditGet(any(Microcredit.class), anyList())).thenReturn(responseMicrocreditGetDto);

            List<ResponseMicrocreditGetDto> actualResponse = microcreditServiceImpl.getMicrocreditsByTransactionStatus(transactionStatus);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).hasSize(1);
            assertThat(actualResponse.get(0)).usingRecursiveComparison().isEqualTo(responseMicrocreditGetDto);

            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(microcreditRepository, times(1)).findByTransactionStatus(enumStatus);
            verify(microcreditMapper, times(1)).responseMicrocreditGet(any(Microcredit.class), anyList());
        }

        @Test
        public void getMicrocreditsByTransactionStatus_ShouldThrowResourceNotFoundException_WhenNotFound() throws Exception {
            String transactionStatus = "COMPLETED";
            TransactionStatus enumStatus = TransactionStatus.COMPLETED;

            when(validations.statusConvert(transactionStatus)).thenReturn(enumStatus);
            when(microcreditRepository.findByTransactionStatus(enumStatus)).thenReturn(List.of());

            assertThatThrownBy(() -> microcreditServiceImpl.getMicrocreditsByTransactionStatus(transactionStatus))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No se encontraron microcréditos con el estado especificado.");

            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(microcreditRepository, times(1)).findByTransactionStatus(enumStatus);
        }
    }
}
