package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.*;
import com.igrowker.nativo.dtos.payment.ResponseRecordPayment;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.*;
import com.igrowker.nativo.mappers.MicrocreditMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.MicrocreditService;
import com.igrowker.nativo.utils.DateFormatter;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.utils.NotificationService;
import com.igrowker.nativo.validations.Validations;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final NotificationService notificationService;
    private final DateFormatter dateFormatter;
    private final Validations getValidations;


    @Override
    public ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) throws MessagingException {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();

        Microcredit microcredit = microcreditMapper.requestDtoToMicrocredit(requestMicrocreditDto);

        checkForActiveOrRestrictedMicrocredit(userBorrower.account.getId());

        BigDecimal limite = new BigDecimal(500000);

        if (microcredit.getAmount().compareTo(limite) > 0) {
            throw new ValidationException("El monto del microcrédito tiene que ser igual o menor a: $ " + limite);
        }

        if (microcredit.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("El monto del microcrédito debe ser mayor a $ 0.00");
        }

        BigDecimal amountFinal = calculateAmountFinal(microcredit);

        //CHEQUEAR QUE SE CREA

        microcredit.setAmountFinal(amountFinal);
        microcredit.setBorrowerAccountId(userBorrower.account.getId());
        microcredit = microcreditRepository.save(microcredit);

        notificationService.sendPaymentNotification(
                userBorrower.user.getEmail(),
                userBorrower.user.getName() + " " + userBorrower.user.getSurname(),
                microcredit.getAmount(),
                "Microcrédito Creado",
                "Te informamos que tu microcrédito con ID: " + microcredit.getId() + " ha sido creado exitosamente.",
                "Gracias por participar en nuestro programa de microcréditos."
        );

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
                .orElseThrow(() -> new ResourceNotFoundException("Microcrédito no encontrado con id: " + id));
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
    public ResponseMicrocreditPaymentDto payMicrocredit(String microcreditId) throws MessagingException {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();

        Microcredit microcredit = microcreditRepository.findById(microcreditId)
                .orElseThrow(() -> new ResourceNotFoundException("Microcrédito no encontrado para el usuario"));

        //Chequear que funcione
        if (validations.isUserAccountMismatch(userBorrower.account.getId())) {
            throw new InvalidUserCredentialsException("El usuario no tiene permiso para pagar este microcrédito.");
        }

        if (microcredit.getTransactionStatus() == TransactionStatus.COMPLETED) {
            throw new DeniedTransactionException("No se puede pagar un microcrédito que ya está " +
                    microcredit.getTransactionStatus().toString().toLowerCase() + ".");
        }

        if (microcredit.getTransactionStatus() == TransactionStatus.PENDING && microcredit.getContributions().isEmpty()) {
            throw new DeniedTransactionException("No se puede pagar un microcrédito sin contribuciones.");
        }

        BigDecimal totalAmountToPay = totalAmountToPay(microcredit);

        if (userBorrower.account.getAmount().compareTo(totalAmountToPay) < 0) {
            throw new InsufficientFundsException("Fondos insuficientes");
        }

        List<Contribution> contributions = microcredit.getContributions();

        GeneralTransactions generalTransactions = new GeneralTransactions(accountRepository);

        BigDecimal totalPaidAmount = BigDecimal.ZERO;

        for (Contribution contribution : contributions) {
            BigDecimal interest = (contribution.getAmount().multiply(microcredit.getInterestRate())).divide(BigDecimal.valueOf(100));
            BigDecimal totalContributionAmountWithInterest = contribution.getAmount().add(interest);

            generalTransactions.updateBalances(microcredit.getBorrowerAccountId(), contribution.getLenderAccountId(),
                    totalContributionAmountWithInterest);

            contribution.setTransactionStatus(TransactionStatus.COMPLETED);
            contributionRepository.save(contribution);

            totalPaidAmount = totalPaidAmount.add(totalContributionAmountWithInterest);

            Account lenderAccount = accountRepository.findById(contribution.getLenderAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta de contribuyente no encontrada."));
            User lenderUser = userRepository.findById(lenderAccount.getUserId())
                    .orElseThrow(() -> new InvalidUserCredentialsException("Usuario de contribuyente no encontrado."));

            notificationService.sendPaymentNotification(lenderUser.getEmail(), (lenderUser.getName() + " " + lenderUser.getSurname()),
                    contribution.getAmount(), "Devolución cuota microcrédito",
                    "Te informamos que se ha procesado la devolución de tu contribución al microcrédito con ID: " + microcredit.getId(),
                    "Gracias por tu participación en nuestro programa de microcréditos. Esperamos seguir contando con tu confianza.");
        }

        notificationService.sendPaymentNotification(userBorrower.user.getEmail(), (userBorrower.user.getName() + " " + userBorrower.user.getSurname()),
                totalPaidAmount, "Descuento cuota del microcrédito",
                "Te informamos que se ha procesado el descuento por el microcrédito con ID: " + microcredit.getId(),
                "Si no tienes saldo suficiente en la cuenta en este momento, el monto pendiente se deducirá automáticamente en tu próximo ingreso.");

        microcredit.setTransactionStatus(TransactionStatus.COMPLETED);

        Microcredit savedMicrocredit = microcreditRepository.save(microcredit);

        return new ResponseMicrocreditPaymentDto(savedMicrocredit.getId(), totalPaidAmount);
    }

    private void checkForActiveOrRestrictedMicrocredit(String borrowerAccountId) {
        Optional<Microcredit> acceptedMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.ACCEPTED);
        if (acceptedMicrocredit.isPresent()) {
            throw new ResourceAlreadyExistsException("Ya tiene un microcrédito activo.");
        }
        Optional<Microcredit> pendingMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.PENDING);
        if (pendingMicrocredit.isPresent()) {
            throw new ResourceAlreadyExistsException("Presenta un microcrédito pendiente.");
        }
        Optional<Microcredit> expiredMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.EXPIRED);
        if (expiredMicrocredit.isPresent()) {
            throw new ExpiredTransactionException("Presenta un microcrédito vencido.");
        }
        Optional<Microcredit> deniedMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.DENIED);
        if (deniedMicrocredit.isPresent()) {
            throw new DeniedTransactionException("No puede solicitar un nuevo microcrédito. Debe regularizar el estado de su cuenta.");
        }
    }

    private List<ResponseMicrocreditGetDto> getResponseMicrocreditGetDtos(List<Microcredit> microcredits) {
        return microcredits.stream().map(this::getResponseMicrocreditGetDto).collect(Collectors.toList());
    }

    private ResponseMicrocreditGetDto getResponseMicrocreditGetDto(Microcredit microcredit) {
        List<ResponseContributionDto> contributionsDto = microcredit.getContributions().stream()
                .map(contribution -> {

                    String lenderFullname = validations.fullname(contribution.getLenderAccountId());
                    String borrowerFullname = validations.fullname(microcredit.getBorrowerAccountId());

                    //MAPPER!!     List<ResponseRecordPayment> paymentListToResponseRecordList(List<Payment> paymentList);
                    return new ResponseContributionDto(
                            contribution.getId(),
                            contribution.getLenderAccountId(),
                            lenderFullname,
                            borrowerFullname,
                            microcredit.getId(),
                            contribution.getAmount(),
                            contribution.getCreatedDate(),
                            microcredit.getExpirationDate(),
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

    @Override
    public List<ResponseMicrocreditGetDto> getMicrocreditsBetweenDates(String fromDate, String toDate) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(fromDate, formatter);
        LocalDate endDate = LocalDate.parse(toDate, formatter);

        List<Microcredit> microcreditList = microcreditRepository.findMicrocreditsBetweenDates(
                accountAndUser.account.getId(), startDate, endDate);

        return microcreditMapper.microcreditListToResponseRecordList(microcreditList);
    }

    public BigDecimal totalAmountToPay(Microcredit microcredit) {
        BigDecimal totalAmountToPay = microcredit.getContributions().stream()
                .map(contribution -> {
                    BigDecimal interest = (contribution.getAmount().multiply(microcredit.getInterestRate())).divide(BigDecimal.valueOf(100));
                    return contribution.getAmount().add(interest);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAmountToPay;
    }


    private BigDecimal calculateAmountFinal(Microcredit microcredit) {
        BigDecimal interest = microcredit.getInterestRate().multiply(microcredit.getAmount()).divide(BigDecimal.valueOf(100));
        BigDecimal amountFinal = microcredit.getAmount().add(interest);

        return amountFinal;
    };


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