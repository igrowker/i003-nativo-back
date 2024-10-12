package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
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
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContributionServiceImplTest {
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
    private ResponseContributionDto responseContributionDto;
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
        lenderAccount.setAccountNumber(987654321L);

        microcredit = new Microcredit();
        microcredit.setId("microcreditId");
        microcredit.setBorrowerAccountId(borrowerAccount.getId());
        microcredit.setAmount(BigDecimal.valueOf(100000.00));
        microcredit.setRemainingAmount(BigDecimal.valueOf(100000.00));
        microcredit.setAmountFinal(BigDecimal.valueOf(100000.00));
        microcredit.setPendingAmount(BigDecimal.ZERO);
        microcredit.setFrozenAmount(BigDecimal.ZERO);
        microcredit.setTitle("Test title");
        microcredit.setDescription("Test Description");
        microcredit.setCreatedDate(LocalDateTime.of(2024, 10, 17, 18, 20));
        microcredit.setCreatedDate(LocalDateTime.of(2024, 9, 17, 18, 20));
        microcredit.setInstallmentCount(1);
        microcredit.setInterestRate(BigDecimal.valueOf(10.00));
        microcredit.setTransactionStatus(TransactionStatus.PENDING);

        responseMicrocreditGetDto = new ResponseMicrocreditGetDto(microcredit.getId(),
                microcredit.getBorrowerAccountId(), microcredit.getAmount(), microcredit.getRemainingAmount(),
                microcredit.getCreatedDate(), microcredit.getExpirationDate(), microcredit.getTitle(),
                microcredit.getDescription(), microcredit.getTransactionStatus(), List.of());

        contribution = new Contribution();
        contribution.setId("contributionId");
        contribution.setLenderAccountId(lenderAccount.getId());
        contribution.setAmount(BigDecimal.valueOf(50000.00));
        contribution.setCreatedDate(LocalDateTime.of(2024, 10, 17, 18, 20));
        contribution.setTransactionStatus(TransactionStatus.ACCEPTED);
        contribution.setMicrocredit(microcredit);
        microcredit.setContributions(List.of(contribution));

        responseContributionDto = new ResponseContributionDto("1111", lenderAccount.getId(),
                "Jane Doe", "John Doe", microcredit.getId(), contribution.getAmount(),
                contribution.getCreatedDate(), microcredit.getExpirationDate(), contribution.getTransactionStatus());

    }

    @Nested
    class CreateContributionsTests {
        @Test
        public void createContribution_ShouldReturnContributionDto_WhenAllValidationsPass() throws MessagingException {
            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    BigDecimal.valueOf(50000.00));

            Validations.UserAccountPair userAccountPair = new Validations.UserAccountPair(lenderUser, lenderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.isUserAccountMismatch(microcredit.getBorrowerAccountId())).thenReturn(true);
            when(validations.validateTransactionUserFunds(requestContributionDto.amount())).thenReturn(true);
            when(microcreditRepository.findById(microcredit.getId())).thenReturn(Optional.of(microcredit));
            when(contributionMapper.requestDtoToContribution(requestContributionDto)).thenReturn(contribution);
            when(contributionRepository.save(contribution)).thenReturn(contribution);
            when(microcreditRepository.save(any(Microcredit.class))).thenReturn(microcredit);
            when(validations.fullname(lenderAccount.getId())).thenReturn(responseContributionDto.lenderFullname());
            when(validations.fullname(borrowerAccount.getId())).thenReturn(responseContributionDto.borrowerFullname());
            when(contributionMapper.responseContributionDto(contribution, responseContributionDto.lenderFullname(),
                    responseContributionDto.borrowerFullname())).thenReturn(responseContributionDto);

            ResponseContributionDto actualResponse = contributionServiceImpl.createContribution(requestContributionDto);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.lenderFullname()).isEqualTo(responseContributionDto.lenderFullname());
            assertThat(actualResponse.borrowerFullname()).isEqualTo(responseContributionDto.borrowerFullname());
            assertThat(actualResponse.amount()).isEqualTo(responseContributionDto.amount());
            assertThat(actualResponse.createdDate()).isEqualTo(responseContributionDto.createdDate());
            assertThat(actualResponse.transactionStatus()).isEqualTo(responseContributionDto.transactionStatus());

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(microcreditRepository, times(1)).findById(microcredit.getId());
            verify(contributionRepository, times(1)).save(contribution);
            verify(microcreditRepository, times(1)).save(any(Microcredit.class));
            verify(notificationService, times(1)).sendContributionNotificationToBorrower(
                    microcredit, lenderAccount.getId(), contribution.getAmount());
        }

        @Test
        public void createContribution_ShouldThrowInvalidUserCredentialsException_WhenUserIsBorrower() {
            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    BigDecimal.valueOf(50000.00));

            Validations.UserAccountPair userAccountPair = new Validations.UserAccountPair(borrowerUser, borrowerAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(microcreditRepository.findById(microcredit.getId())).thenReturn(Optional.of(microcredit));

            assertThrows(InvalidUserCredentialsException.class, () -> {
                contributionServiceImpl.createContribution(requestContributionDto);
            });

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(microcreditRepository, times(1)).findById(microcredit.getId());
            verify(contributionRepository, never()).save(any());
        }
    }

    @Nested
    class GetAllContributionsByUserTests {
        @Test
        public void getAllContributionsByUser_ShouldReturnResponseContributionDtoList_WhenContributionsExist() {
            Validations.UserAccountPair userAccountPair = new Validations.UserAccountPair(lenderUser, lenderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(contributionRepository.findAllByLenderAccountId(contribution.getLenderAccountId())).thenReturn(List.of(contribution));
            when(validations.fullname(lenderAccount.getId())).thenReturn(responseContributionDto.lenderFullname());
            when(validations.fullname(borrowerAccount.getId())).thenReturn(responseContributionDto.borrowerFullname());
            when(contributionMapper.responseContributionDto(contribution, responseContributionDto.lenderFullname(),
                    responseContributionDto.borrowerFullname())).thenReturn(responseContributionDto);

            List<ResponseContributionDto> result = contributionServiceImpl.getAllContributionsByUser();

            assertThat(result).isNotNull();
            assertThat(result).containsExactly(responseContributionDto);

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(contributionRepository, times(1)).findAllByLenderAccountId(contribution.getLenderAccountId());
            verify(contributionMapper, times(1)).responseContributionDto(any(Contribution.class), any(), any());
        }

        @Test
        public void getAllContributionsByUser_ShouldThrowResourceNotFoundException_WhenNoContributionsExist() {
            Validations.UserAccountPair userAccountPair = new Validations.UserAccountPair(lenderUser, lenderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(contributionRepository.findAllByLenderAccountId(lenderAccount.getId())).thenReturn(Collections.emptyList());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> contributionServiceImpl.getAllContributionsByUser())
                    .withMessage("No se encontraron contribuciones.");

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(contributionRepository, times(1)).findAllByLenderAccountId(lenderAccount.getId());
        }
    }

    @Nested
    class GetAllContributionsByUserByStatusTests {
        @Test
        public void getAllContributionsByUserByStatus_ShouldReturnResponseContributionDtoList_WhenContributionsExist() {
            Validations.UserAccountPair userAccountPair = new Validations.UserAccountPair(lenderUser, lenderAccount);
            TransactionStatus enumStatus = TransactionStatus.PENDING;

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert("PENDING")).thenReturn(enumStatus);
            when(contributionRepository.findByTransactionStatusAndLenderAccountId(enumStatus, lenderAccount.getId()))
                    .thenReturn(List.of(contribution));

            when(contributionMapper.responseContributionDto(any(), any(), any())).thenReturn(responseContributionDto);

            List<ResponseContributionDto> result = contributionServiceImpl.getAllContributionsByUserByStatus("PENDING");

            assertThat(result).isNotNull();
            assertThat(result).containsExactly(responseContributionDto);

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert("PENDING");
            verify(contributionRepository, times(1)).findByTransactionStatusAndLenderAccountId(enumStatus, lenderAccount.getId());
            verify(contributionMapper, times(1)).responseContributionDto(any(Contribution.class), any(), any());
        }

        @Test
        public void getAllContributionsByUserByStatus_ShouldThrowResourceNotFoundException_WhenNoContributionsExist() {
            Validations.UserAccountPair userAccountPair = new Validations.UserAccountPair(lenderUser, lenderAccount);
            TransactionStatus enumStatus = TransactionStatus.PENDING;

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert("PENDING")).thenReturn(enumStatus);
            when(contributionRepository.findByTransactionStatusAndLenderAccountId(enumStatus, lenderAccount.getId())).thenReturn(Collections.emptyList());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> contributionServiceImpl.getAllContributionsByUserByStatus("PENDING"))
                    .withMessage("No se encontraron contribuciones para el usuario en el estado especificado.");

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert("PENDING");
            verify(contributionRepository, times(1)).findByTransactionStatusAndLenderAccountId(enumStatus, lenderAccount.getId());
        }
    }

    @Nested
    class GetContributionsBetweenDatesTests {
        @Test
        public void getContributionsBetweenDates_ShouldThrowResourceNotFoundException_WhenNoContributionsExist() {
            String fromDate = "2024-10-01T00:00:00";
            String toDate = "2024-10-10T23:59:59";

            LocalDateTime startDate = LocalDateTime.of(2024, 10, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 10, 10, 23, 59, 59);

            Validations.UserAccountPair userAccountPair = new Validations.UserAccountPair(lenderUser, lenderAccount);
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(dateFormatter.getDateFromString(fromDate, toDate)).thenReturn(List.of(startDate, endDate));
            when(contributionRepository.findContributionsBetweenDates(lenderAccount.getId(), startDate, endDate))
                    .thenReturn(Collections.emptyList());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> contributionServiceImpl.getContributionsBetweenDates(fromDate, toDate))
                    .withMessage("No se encontraron contribuciones en el rango de fechas proporcionado.");

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(dateFormatter, times(1)).getDateFromString(fromDate, toDate);
            verify(contributionRepository, times(1)).findContributionsBetweenDates(lenderAccount.getId(), startDate, endDate);
        }
    }

    @Nested
    class GetContributionsByDateAndStatusTests {
        @Test
        public void getContributionsByDateAndStatus_ShouldReturnResponseContributionDtoList_WhenContributionsExist() {
            String date = "2024-10-01T00:00:00,2024-10-10T23:59:59";
            String transactionStatus = "ACCEPTED";

            TransactionStatus enumStatus = contribution.getTransactionStatus();
            Validations.UserAccountPair accountAndUser = new Validations.UserAccountPair(lenderUser, lenderAccount);

            List<LocalDateTime> elapsedDate = List.of(
                    LocalDateTime.of(2024, 10, 1, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59, 59)
            );

            List<Contribution> contributions = List.of(contribution);
            List<ResponseContributionDto> responseContributionDtos = List.of(responseContributionDto);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(accountAndUser);
            when(validations.statusConvert(transactionStatus)).thenReturn(enumStatus);
            when(dateFormatter.getDateFromString(date)).thenReturn(elapsedDate);
            when(contributionRepository.findContributionsByDateAndTransactionStatus(
                    accountAndUser.account.getId(), elapsedDate.get(0), elapsedDate.get(1), enumStatus))
                    .thenReturn(contributions);
            when(contributionMapper.responseContributionDto(contribution,
                    validations.fullname(contribution.getLenderAccountId()),
                    validations.fullname(contribution.getMicrocredit().getBorrowerAccountId()))).thenReturn(responseContributionDto);

            List<ResponseContributionDto> result = contributionServiceImpl.getContributionsByDateAndStatus(date, transactionStatus);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(responseContributionDto);

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(dateFormatter, times(1)).getDateFromString(date);
            verify(contributionRepository, times(1)).findContributionsByDateAndTransactionStatus(
                    accountAndUser.account.getId(), elapsedDate.get(0), elapsedDate.get(1), enumStatus);
            verify(contributionMapper, times(1)).responseContributionDto(any(Contribution.class), any(), any());
        }

        @Test
        public void getContributionsByDateAndStatus_ShouldThrowResourceNotFoundException_WhenNoContributionsExist() {
            String date = "2024-10-01T00:00:00,2024-10-10T23:59:59";
            String transactionStatus = "PENDING";

            TransactionStatus enumStatus = TransactionStatus.PENDING;
            Validations.UserAccountPair accountAndUser = new Validations.UserAccountPair(lenderUser, lenderAccount);

            List<LocalDateTime> elapsedDate = List.of(
                    LocalDateTime.of(2024, 10, 1, 0, 0),
                    LocalDateTime.of(2024, 10, 10, 23, 59, 59)
            );

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(accountAndUser);
            when(validations.statusConvert(transactionStatus)).thenReturn(enumStatus);
            when(dateFormatter.getDateFromString(date)).thenReturn(elapsedDate);
            when(contributionRepository.findContributionsByDateAndTransactionStatus(
                    accountAndUser.account.getId(), elapsedDate.get(0), elapsedDate.get(1), enumStatus))
                    .thenReturn(Collections.emptyList());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> contributionServiceImpl.getContributionsByDateAndStatus(date, transactionStatus))
                    .withMessage("No posee contribuciones.");

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(dateFormatter, times(1)).getDateFromString(date);
            verify(contributionRepository, times(1)).findContributionsByDateAndTransactionStatus(
                    accountAndUser.account.getId(), elapsedDate.get(0), elapsedDate.get(1), enumStatus);
        }
    }

    @Nested
    class GetAllContributionsTests {
        @Test
        public void getAll_ShouldReturnResponseContributionDtoList_WhenContributionsExist() {
            List<Contribution> contributions = List.of(contribution);
            List<ResponseContributionDto> responseContributionDtos = List.of(responseContributionDto);

            when(contributionRepository.findAll()).thenReturn(contributions);
            when(contributionMapper.responseContributionDto(contribution,
                    validations.fullname(contribution.getLenderAccountId()),
                    validations.fullname(contribution.getMicrocredit().getBorrowerAccountId()))).thenReturn(responseContributionDto);

            List<ResponseContributionDto> result = contributionServiceImpl.getAll();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(responseContributionDto);

            verify(contributionRepository, times(1)).findAll();
            verify(contributionMapper, times(1)).responseContributionDto(any(Contribution.class), any(), any());
        }

        @Test
        public void getAll_ShouldThrowResourceNotFoundException_WhenNoContributionsExist() {
            when(contributionRepository.findAll()).thenReturn(Collections.emptyList());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> contributionServiceImpl.getAll())
                    .withMessage("No se encontraron contribuciones.");

            verify(contributionRepository, times(1)).findAll();
        }
    }

    @Nested
    class GetOneContributionTests {
        @Test
        public void getOneContribution_ShouldReturnResponseContributionDto_WhenContributionExists() {
            when(contributionRepository.findById(contribution.getId())).thenReturn(Optional.of(contribution));
            when(microcreditRepository.findById(contribution.getMicrocredit().getId())).thenReturn(Optional.of(microcredit));
            when(validations.fullname(contribution.getLenderAccountId())).thenReturn("Jane Doe");
            when(validations.fullname(microcredit.getBorrowerAccountId())).thenReturn("John Doe");
            when(contributionMapper.responseContributionDto(contribution, "Jane Doe", "John Doe")).thenReturn(responseContributionDto);

            ResponseContributionDto result = contributionServiceImpl.getOneContribution(contribution.getId());

            assertThat(result).isEqualTo(responseContributionDto);

            verify(contributionRepository, times(1)).findById(contribution.getId());
            verify(microcreditRepository, times(1)).findById(contribution.getMicrocredit().getId());
            verify(validations, times(1)).fullname(contribution.getLenderAccountId());
            verify(validations, times(1)).fullname(microcredit.getBorrowerAccountId());
            verify(contributionMapper, times(1)).responseContributionDto(contribution, "Jane Doe", "John Doe");
        }

        @Test
        public void getOneContribution_ShouldThrowResourceNotFoundException_WhenContributionDoesNotExist() {
            String id = "12345";
            when(contributionRepository.findById(id)).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> contributionServiceImpl.getOneContribution(id))
                    .withMessage("Contribución no encontrada con id: " + id);

            verify(contributionRepository, times(1)).findById(id);
        }
    }

    @Nested
    class GetContributionsByTransactionStatusTests {
        @Test
        public void getContributionsByTransactionStatus_ShouldReturnResponseContributionDtoList_WhenContributionsExist() {
            String transactionStatus = "ACCEPTED";
            TransactionStatus enumStatus = contribution.getTransactionStatus();
            List<Contribution> contributions = List.of(contribution);
            List<ResponseContributionDto> responseContributionDtos = List.of(responseContributionDto);

            when(validations.statusConvert(transactionStatus)).thenReturn(enumStatus);
            when(contributionRepository.findByTransactionStatus(enumStatus)).thenReturn(contributions);
            when(contributionMapper.responseContributionDto(contribution,
                    validations.fullname(contribution.getLenderAccountId()),
                    validations.fullname(contribution.getMicrocredit().getBorrowerAccountId()))).thenReturn(responseContributionDto);

            List<ResponseContributionDto> result = contributionServiceImpl.getContributionsByTransactionStatus(transactionStatus);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(responseContributionDto);

            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(contributionRepository, times(1)).findByTransactionStatus(enumStatus);
            verify(contributionMapper, times(1)).responseContributionDto(any(Contribution.class), any(), any());
        }

        @Test
        public void getContributionsByTransactionStatus_ShouldThrowResourceNotFoundException_WhenNoContributionsExist() {
            String transactionStatus = "PENDING";
            TransactionStatus enumStatus = TransactionStatus.PENDING;
            when(validations.statusConvert(transactionStatus)).thenReturn(enumStatus);
            when(contributionRepository.findByTransactionStatus(enumStatus)).thenReturn(Collections.emptyList());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> contributionServiceImpl.getContributionsByTransactionStatus(transactionStatus))
                    .withMessage("No se encontraron contribuciones con el estado especificado.");

            verify(validations, times(1)).statusConvert(transactionStatus);
            verify(contributionRepository, times(1)).findByTransactionStatus(enumStatus);
        }
    }
}
