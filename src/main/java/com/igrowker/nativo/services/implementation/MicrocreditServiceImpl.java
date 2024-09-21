package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.contribution.ResponseContributionGetDto;
import com.igrowker.nativo.dtos.microcredit.*;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.mappers.MicrocreditMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.MicrocreditService;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.validations.Validations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MicrocreditServiceImpl implements MicrocreditService {
    private final MicrocreditRepository microcreditRepository;
    private final MicrocreditMapper microcreditMapper;
    private final Validations validations;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ContributionRepository contributionRepository;

    @Override
    public ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();

        Microcredit microcredit = microcreditMapper.requestDtoToMicrocredit(requestMicrocreditDto);

        //Validación para evaluar si tiene un microcredito en estado pendiente, vencido o denegado
        isMicrocreditExpiredOrPendentOrDenied(userBorrower.account.getId());

        BigDecimal limite = new BigDecimal("500000");

        if (microcredit.getAmount().compareTo(limite) > 0) {
            throw new ValidationException("El monto del microcrédito tiene que ser igual o menor a: $ " + limite);
        }

        microcredit.setBorrowerAccountId(userBorrower.account.getId());
        microcredit = microcreditRepository.save(microcredit);

        //Agregar fecha de vencimiento del microcredito
        return microcreditMapper.responseDtoToMicrocredit(microcredit);
    }

    @Override
    @Transactional
    public List<ResponseMicrocreditGetDto> getAll() {
        List<Microcredit> microcredits = microcreditRepository.findAll();

        return getResponseMicrocreditGetDtos(microcredits);
    }

    @Override
    @Transactional
    public List<ResponseMicrocreditGetDto> getMicrocreditsByTransactionStatus(String transactionStatus) {
        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);

        List<Microcredit> microcredits = microcreditRepository.findByTransactionStatus(enumStatus);

        return getResponseMicrocreditGetDtos(microcredits);
    }

    @Override
    @Transactional
    public ResponseMicrocreditGetDto getOne(String id) {
        Microcredit microcredit = microcreditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Microcrédito no encontrado con id: " + id));

        return getResponseMicrocreditGetDto(microcredit);
    }

    @Override
    public List<ResponseMicrocreditGetDto> getBy(String transactionStatus) {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();

        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);

        List<Microcredit> microcredits = microcreditRepository.findByTransactionStatusAndBorrowerAccountId(
                enumStatus, userBorrower.account.getId());

        if (microcredits.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron microcréditos para el usuario con el estado especificado.");
        }

        return microcredits.stream()
                .map(this::getResponseMicrocreditGetDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResponseMicrocreditPaymentDto payMicrocredit(String microcreditId) {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();

        Microcredit microcredit = microcreditRepository.findById(microcreditId)
                .orElseThrow(() -> new ResourceNotFoundException("Microcrédito no encontrado para el usuario"));

        if (!microcredit.getBorrowerAccountId().equals(userBorrower.account.getId())) {
            throw new IllegalArgumentException("El usuario no tiene permiso para pagar este microcrédito.");
        }

        BigDecimal totalAmountToPay = microcredit.getContributions().stream()
                .map(Contribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (userBorrower.account.getAmount().compareTo(totalAmountToPay) < 0) {
            throw new ValidationException("Fondos insuficientes");
        }

        if (microcredit.getTransactionStatus() == TransactionStatus.COMPLETED) {
            throw new IllegalArgumentException("El microcrédito ya ha sido pagado.");
        }

        List<Contribution> contributions = microcredit.getContributions();
        GeneralTransactions generalTransactions = new GeneralTransactions(accountRepository);

        for (Contribution contribution : contributions) {
            generalTransactions.updateBalances(microcredit.getBorrowerAccountId(), contribution.getLenderAccountId(),
                    contribution.getAmount());

            contribution.setTransactionStatus(TransactionStatus.COMPLETED);
            contributionRepository.save(contribution);
        }

        microcredit.setTransactionStatus(TransactionStatus.COMPLETED);
        microcreditRepository.save(microcredit);

        return microcreditMapper.responseMicrocreditPaymentDto(microcredit);
    }

    private void isMicrocreditExpiredOrPendentOrDenied(String borrowerAccountId) {
        Optional<Microcredit> pendingMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.PENDENT);
        if (pendingMicrocredit.isPresent()) {
            throw new ValidationException("Presenta un microcrédito pendiente.");
        }

        Optional<Microcredit> expiredMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.EXPIRED);
        if (expiredMicrocredit.isPresent()) {
            throw new ValidationException("Presenta un microcrédito vencido.");
        }

        Optional<Microcredit> deniedMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.DENIED);
        if (deniedMicrocredit.isPresent()) {
            throw new ValidationException("No puede solicitar un nuevo microcrédito. Debe regularizar el estado de su cuenta.");
        }
    }

    private List<ResponseMicrocreditGetDto> getResponseMicrocreditGetDtos(List<Microcredit> microcredits) {
        return microcredits.stream().map(this::getResponseMicrocreditGetDto).collect(Collectors.toList());
    }

    private ResponseMicrocreditGetDto getResponseMicrocreditGetDto(Microcredit microcredit) {
        List<ResponseContributionGetDto> contributionsDto = microcredit.getContributions().stream()
                .map(contribution -> {

                    Validations validations1 = new Validations(accountRepository, userRepository);

                    String lenderFullname = validations.fullname(contribution.getLenderAccountId());
                    String borrowerFullname = validations.fullname(microcredit.getBorrowerAccountId());

                    return new ResponseContributionGetDto(
                            contribution.getId(),
                            contribution.getLenderAccountId(),
                            lenderFullname,
                            borrowerFullname,
                            microcredit.getId(),
                            contribution.getAmount(),
                            contribution.getCreatedDate(),
                            contribution.getTransactionStatus()
                    );
                }).collect(Collectors.toList());

        return new ResponseMicrocreditGetDto(
                microcredit.getId(),
                microcredit.getBorrowerAccountId(),
                microcredit.getAmount(),
                microcredit.getRemainingAmount(),
                microcredit.getCreatedDate(),
                microcredit.getExpirationDate(),
                microcredit.getTitle(),
                microcredit.getDescription(),
                microcredit.getTransactionStatus(),
                contributionsDto
        );
    }

    /*
    Listar todos los microcreditos, comparar la fecha de vencimiento con la actual.
    ACCEPTED -- SE COMPLETA EL MONTO TOTAL
    DENIED -- NO CUMPLE CON VENCIMIENTO O PENDIENTES,
    FAILED -- ALGUN PROBLEMA DE SISTEMA
    PENDENT -- AL CREARSE EL MICROCREDITO
    EXPIRED -- SUPERA LA FECHA DE VENCIMIENTO Y NO TIENE FONDOS ENTRA EN DEUDA.
    COMPLETED -- AL CUMPLIRSE CON EL PAGO DEL MICRO EN TERMINO
     */
}