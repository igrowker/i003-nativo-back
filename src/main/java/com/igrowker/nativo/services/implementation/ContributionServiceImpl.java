package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionGetDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.mappers.ContributionMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.validations.Validations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;
    private final MicrocreditRepository microcreditRepository;
    private final ContributionMapper contributionMapper;
    private final Validations validations;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResponseContributionDto createContribution(RequestContributionDto requestContributionDto) {
        Validations.UserAccountPair userLender = validations.getAuthenticatedUserAndAccount();
        String userLenderId = userLender.account.getId();

        Microcredit microcredit = microcreditRepository.findById(requestContributionDto.microcreditId())
                .orElseThrow(() -> new ResourceNotFoundException("Microcrédito no encontrado"));

        if (!validations.isUserAccountMismatch(microcredit.getBorrowerAccountId())) {
            throw new IllegalArgumentException("El usuario contribuyente no puede ser el mismo que el solicitante del microcrédito.");
        }

        //Cambio de estado del contribuyente si hubo error FAILED

        Contribution contribution = contributionMapper.requestDtoToContribution(requestContributionDto);

        BigDecimal remainingAmount;

        if (microcredit.getRemainingAmount().compareTo(microcredit.getAmount()) == 0) {
            remainingAmount = microcredit.getAmount().subtract(contribution.getAmount());
        } else {
            remainingAmount = microcredit.getRemainingAmount().subtract(contribution.getAmount());
        }

        if (!contributionOk(contribution.getAmount(), microcredit.getRemainingAmount())) {
            throw new ValidationException("El monto a contribuir no puede ser mayor al solicitado: $ " +
                    microcredit.getAmount());
        }

        microcredit.setRemainingAmount(remainingAmount);

        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            microcredit.setTransactionStatus(TransactionStatus.ACCEPTED);
        }

        contribution.setLenderAccountId(userLender.account.getId());
        contribution.setMicrocredit(microcredit);
        contribution = contributionRepository.save(contribution);

        //Pasa la contribución al saldo del solicitante y resta la misma al contribuyente
        GeneralTransactions generalTransactions = new GeneralTransactions(accountRepository);

        generalTransactions.updateBalances(userLenderId, contribution.getLenderAccountId(), contribution.getAmount());

        microcreditRepository.save(microcredit);

        String lenderFullname = fullname(contribution.getLenderAccountId());
        String borrowerFullname = fullname(microcredit.getBorrowerAccountId());
        String microcreditId = microcredit.getId();
        LocalDate expirationDate = microcredit.getExpirationDate();

        // Realizar la tranferencia entre cuentas validateTransactionUserFunds()

        return contributionMapper.responseContributionDto(contribution, lenderFullname, borrowerFullname,
                microcreditId, expirationDate);
    }

    // Listado de contribuciones por contribuyente.

    @Override
    public List<ResponseContributionGetDto> getAll() {
        List<Contribution> contributions = contributionRepository.findAll();

        return contributions.stream()
                .map(contribution -> {
                    Microcredit microcredit = microcreditRepository.findById(contribution.getMicrocredit().getId())
                            .orElseThrow(() -> new RuntimeException("Microcrédito no encontrado"));

                    String lenderFullname = fullname(contribution.getLenderAccountId());
                    String borrowerFullname = fullname(microcredit.getBorrowerAccountId());
                    String microcreditId = microcredit.getId();
                    LocalDate expirationDate = microcredit.getExpirationDate();

                    return contributionMapper.responseContributionGetDto(contribution, lenderFullname, borrowerFullname,
                            microcreditId, expirationDate);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseContributionGetDto> getContributionsByTransactionStatus(String transactionStatus) {
        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);
        List<Contribution> contributions = contributionRepository.findByTransactionStatus(enumStatus);

        return contributions.stream()
                .map(contribution -> {
                    Microcredit microcredit = microcreditRepository.findById(contribution.getMicrocredit().getId())
                            .orElseThrow(() -> new RuntimeException("Microcrédito no encontrado"));

                    String lenderFullname = fullname(contribution.getLenderAccountId());
                    String borrowerFullname = fullname(microcredit.getBorrowerAccountId());
                    String microcreditId = microcredit.getId();
                    LocalDate expirationDate = microcredit.getExpirationDate();

                    return contributionMapper.responseContributionGetDto(contribution, lenderFullname, borrowerFullname,
                            microcreditId, expirationDate);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ResponseContributionGetDto getOneContribution(String id) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contribution no encontrado con id: " + id));

        Microcredit microcredit = microcreditRepository.findById(contribution.getMicrocredit().getId())
                .orElseThrow(() -> new RuntimeException("Microcrédito no encontrado con id: " + contribution.getMicrocredit().getId()));

        String lenderFullname = fullname(contribution.getLenderAccountId());
        String borrowerFullname = fullname(microcredit.getBorrowerAccountId());
        String microcreditId = microcredit.getId();
        LocalDate expirationDate = microcredit.getExpirationDate();

        return contributionMapper.responseContributionGetDto(contribution, lenderFullname, borrowerFullname,
                microcreditId, expirationDate);
    }

    //Valida monto contribución
    public boolean contributionOk(BigDecimal contributionAmount, BigDecimal microcreditRemainingAmount) {
        return contributionAmount.compareTo(BigDecimal.ZERO) > 0 && contributionAmount.compareTo(microcreditRemainingAmount) <= 0;
    }

    private String fullname(String accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() ->
                new ResourceNotFoundException("Cuenta no encontrada"));

        User user = userRepository.findById(account.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException("Usuario no encontrado"));

        return user.getSurname().toUpperCase() + ", " + user.getName();
    }

      /*
    ACCEPTED -- CUANDO SE CREE LA CONTRIBUCIÓN
    DENIED -- FONDOS INSUFICIENTES
    FAILED -- ALGUN PROBLEMA DE SISTEMA
    COMPLETED -- SE DEVUELVE EL DINERO CONTRIBUIDO
      */
}
