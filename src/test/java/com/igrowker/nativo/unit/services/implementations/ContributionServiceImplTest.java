package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.mappers.ContributionMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.services.implementation.ContributionServiceImpl;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.utils.NotificationService;
import com.igrowker.nativo.validations.Validations;
import org.junit.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContributionServiceImplTest {
    @Mock
    private MicrocreditRepository microcreditRepository;

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private Validations validations;

    @Mock
    private GeneralTransactions generalTransactions;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ContributionMapper contributionMapper;

    @InjectMocks
    private ContributionServiceImpl contributionService;

    private Microcredit microcredit;
    private Contribution contribution;
    private User userBorrower;
    private User userLender;
    private Account accountBorrower;
    private Account accountLender;

    @BeforeEach
    public void setUp() {
        // Configuración del usuario prestatario (Borrower)
        accountBorrower = new Account();
        accountBorrower.setId("1234567L");
        accountBorrower.setAccountNumber(1234567L);

        userBorrower = new User();
        userBorrower.setEmail("tonystark@example.com");
        userBorrower.setName("Tony");
        userBorrower.setSurname("Stark");
        userBorrower.setAccountId(accountBorrower.getId());

        // Configuración del usuario prestamista (Lender)
        accountLender = new Account();
        accountLender.setId("7654321L");
        accountLender.setAccountNumber(7654321L);
        accountLender.setAmount(BigDecimal.valueOf(200000.00));

        userLender = new User();
        userLender.setEmail("peterparker@example.com");
        userLender.setName("Peter");
        userLender.setSurname("Parker");
        userLender.setAccountId(accountLender.getId());

        // Configuración del microcrédito
        microcredit = new Microcredit("microcreditId", accountBorrower.getId(), BigDecimal.valueOf(100000.00),
                BigDecimal.valueOf(100000.00), BigDecimal.valueOf(110000.00), BigDecimal.ZERO, BigDecimal.ZERO,
                "Test title", "Test description", LocalDateTime.of(2024, 10, 17, 18, 20),
                LocalDateTime.of(2024, 9, 17, 18, 20), 1, BigDecimal.valueOf(10.00), TransactionStatus.PENDING, List.of());

        // Asegúrate de que el microcrédito está correctamente asignado al prestatario
        assertNotEquals(accountBorrower.getId(), accountLender.getId());

        // Configuración de la contribución
        contribution = new Contribution();
        contribution.setId("contributionId");
        contribution.setAmount(BigDecimal.valueOf(2000));
        contribution.setMicrocredit(microcredit);
        contribution.setLenderAccountId(accountLender.getId());
    }

    @Test
    public void createContribution_ShouldReturnOk() throws Exception {
        // Datos de entrada del DTO de la contribución
        RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(), BigDecimal.valueOf(2000));

        // Configuración de los mocks
        Validations.UserAccountPair userAccountPair = new Validations.UserAccountPair(userLender, accountLender);
        when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
        when(microcreditRepository.findById(anyString())).thenReturn(Optional.of(microcredit));
        when(contributionMapper.requestDtoToContribution(any(RequestContributionDto.class))).thenReturn(contribution);
        when(contributionRepository.save(any(Contribution.class))).thenReturn(contribution);

        // Configuración del mapeo del resultado a ResponseContributionDto
        ResponseContributionDto responseContributionDto = new ResponseContributionDto(
                contribution.getId(),
                accountLender.getId(),
                userLender.getSurname().toUpperCase() + " " + userLender.getName(),
                userBorrower.getSurname().toUpperCase() + " " + userBorrower.getName(),
                microcredit.getId(),
                contribution.getAmount(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                TransactionStatus.COMPLETED
        );

        // Validación de que los usuarios son distintos
        assertNotEquals(microcredit.getBorrowerAccountId(), userLender.getAccountId());

        // Llamada al método que se está probando
        ResponseContributionDto response = contributionService.createContribution(requestContributionDto);

        // Validaciones
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(responseContributionDto.id());
        assertThat(response.amount()).isEqualTo(responseContributionDto.amount());

        // Verificar que los métodos mockeados fueron llamados correctamente
        verify(contributionRepository, times(1)).save(any(Contribution.class));
        verify(validations, times(1)).getAuthenticatedUserAndAccount();
        verify(contributionMapper, times(1)).requestDtoToContribution(any(RequestContributionDto.class));
        verify(contributionMapper, times(1)).responseContributionDto(
                any(Contribution.class),
                eq(userLender.getSurname().toUpperCase() + " " + userLender.getName()),
                eq(userBorrower.getSurname().toUpperCase() + " " + userBorrower.getName())
        );
    }

}