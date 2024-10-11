package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        microcredit.setContributions(List.of());

        responseMicrocreditGetDto = new ResponseMicrocreditGetDto(microcredit.getId(),
                microcredit.getBorrowerAccountId(), microcredit.getAmount(), microcredit.getRemainingAmount(),
                microcredit.getCreatedDate(), microcredit.getExpirationDate(), microcredit.getTitle(),
                microcredit.getDescription(), microcredit.getTransactionStatus(), List.of());

        contribution = new Contribution();
        contribution.setId("contributionId");
        contribution.setLenderAccountId(lenderAccount.getId());
        contribution.setAmount(BigDecimal.valueOf(50000.00));
        contribution.setCreatedDate(LocalDateTime.of(2024, 10, 17, 18, 20));
        contribution.setTransactionStatus(TransactionStatus.PENDING);
    }

    @Nested
    class CreateContributionsTests {
        @Test
        public void createContribution_ShouldReturnContributionDto_WhenAllValidationsPass() throws MessagingException {
            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    BigDecimal.valueOf(50000.00));

            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", lenderAccount.getId(),
                    "Jane Doe", "John Doe", microcredit.getId(), contribution.getAmount(),
                    contribution.getCreatedDate(), microcredit.getExpirationDate(), contribution.getTransactionStatus());

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
}
