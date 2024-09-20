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

        // verificar que la cuenta tenga el saldo a transferir
        if (!validations.validateTransactionUserFunds(requestContributionDto.amount())) {
            throw new ValidationException("Fondos insuficientes");
        }
        //Actualizar el monto restante
        updateRemainingAmount(microcredit, requestContributionDto.amount());

        Contribution contribution = contributionMapper.requestDtoToContribution(requestContributionDto);
        contribution.setLenderAccountId(userLender.account.getId());
        contribution.setMicrocredit(microcredit);
        contribution = contributionRepository.save(contribution);

        //Pasa la contribución al saldo del solicitante y resta la misma al contribuyente
        GeneralTransactions generalTransactions = new GeneralTransactions(accountRepository);
        generalTransactions.updateBalances(userLenderId, contribution.getMicrocredit().getBorrowerAccountId(), contribution.getAmount());

        microcreditRepository.save(microcredit);

        Validations validations1 = new Validations(accountRepository, userRepository);

        String lenderFullname = validations.fullname(contribution.getLenderAccountId());
        String borrowerFullname = validations.fullname(microcredit.getBorrowerAccountId());
        String microcreditId = microcredit.getId();
        LocalDate expirationDate = microcredit.getExpirationDate();

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

                    Validations validations1 = new Validations(accountRepository, userRepository);

                    String lenderFullname = validations1.fullname(contribution.getLenderAccountId());
                    String borrowerFullname = validations1.fullname(microcredit.getBorrowerAccountId());
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

                    String lenderFullname = validations.fullname(contribution.getLenderAccountId());
                    String borrowerFullname = validations.fullname(microcredit.getBorrowerAccountId());
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

        Validations validations1 = new Validations(accountRepository, userRepository);

        String lenderFullname = validations1.fullname(contribution.getLenderAccountId());
        String borrowerFullname = validations1.fullname(microcredit.getBorrowerAccountId());
        String microcreditId = microcredit.getId();
        LocalDate expirationDate = microcredit.getExpirationDate();

        return contributionMapper.responseContributionGetDto(contribution, lenderFullname, borrowerFullname,
                microcreditId, expirationDate);
    }

    //Valida monto contribución
    public boolean contributionOk(BigDecimal contributionAmount, BigDecimal microcreditRemainingAmount) {
        return contributionAmount.compareTo(BigDecimal.ZERO) > 0 && contributionAmount.compareTo(microcreditRemainingAmount) <= 0;
    }

    //Chequea el monto restante. Cambia el estado de la transacción
    private void updateRemainingAmount(Microcredit microcredit, BigDecimal contributionAmount) {
        if (microcredit.getTransactionStatus() == TransactionStatus.PENDENT) {
            if (contributionAmount.compareTo(microcredit.getRemainingAmount()) > 0) {
                throw new ValidationException("El monto de la contribución no puede ser mayor que el monto restante del microcrédito.");
            }

            BigDecimal remainingAmount = microcredit.getRemainingAmount().subtract(contributionAmount);
            microcredit.setRemainingAmount(remainingAmount);

            if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
                microcredit.setTransactionStatus(TransactionStatus.ACCEPTED);
            }

            microcreditRepository.saveAndFlush(microcredit);
        } else if (microcredit.getTransactionStatus() == TransactionStatus.ACCEPTED) {
            throw new IllegalStateException("No se puede agregar más dinero a un microcrédito que ya está en estado ACCEPTED.");
        } else {
            throw new IllegalStateException("No se puede contribuir en el microcrédito en estado " + microcredit.getTransactionStatus());
        }
    }
}
      /*
    ACCEPTED -- CUANDO SE CREE LA CONTRIBUCIÓN
    DENIED -- FONDOS INSUFICIENTES
    FAILED -- ALGUN PROBLEMA DE SISTEMA
    COMPLETED -- SE DEVUELVE EL DINERO CONTRIBUIDO
      */

