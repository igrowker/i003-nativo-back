package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.*;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.*;
import com.igrowker.nativo.mappers.ContributionMapper;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MicrocreditServiceImpl implements MicrocreditService {
    private final MicrocreditRepository microcreditRepository;
    private final MicrocreditMapper microcreditMapper;
    private final ContributionMapper contributionMapper;
    private final Validations validations;
    private final GeneralTransactions generalTransactions;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ContributionRepository contributionRepository;
    private final NotificationService notificationService;
    private final BigDecimal microcreditLimit = new BigDecimal(500000);
    private final DateFormatter dateFormatter;

    @Override
    public ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) throws MessagingException {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();

        Microcredit microcredit = microcreditMapper.requestDtoToMicrocredit(requestMicrocreditDto);

        checkForActiveOrRestrictedMicrocredit(userBorrower.account.getId());

        if (isMicrocreditLimitExceeded(microcredit.getAmount())) {
            throw new ValidationException("El monto del microcrédito tiene que ser igual o menor a: $ " + microcreditLimit);
        }

        if (microcredit.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("El monto del microcrédito debe ser mayor a $ 0.00");
        }

        BigDecimal amountFinal = calculateAmountFinal(microcredit);

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
    public ResponseMicrocreditPaymentDto payMicrocredit(String microcreditId) throws MessagingException {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();

        Microcredit microcredit = microcreditRepository.findById(microcreditId)
                .orElseThrow(() -> new ResourceNotFoundException("Microcrédito no encontrado para el usuario"));

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

        if (!validations.validateTransactionUserFunds(totalAmountToPay)) {
            throw new InsufficientFundsException("Fondos insuficientes");
        }

        List<Contribution> contributions = microcredit.getContributions();

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

    @Override
    public List<ResponseMicrocreditGetDto> getAllMicrocreditsByUser() {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();

        String borrowerAccountId = accountAndUser.account.getId();

        List<Microcredit> microcreditList = microcreditRepository.findAllByBorrowerAccountId(borrowerAccountId);

        if (microcreditList.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron microcréditos.");
        }

        return microcreditList.stream()
                .map(microcredit -> {
                    List<ResponseContributionDto> contributionsDto = mapContributionsToDto(microcredit.getContributions());

                    return microcreditMapper.responseMicrocreditGet(microcredit, contributionsDto);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseMicrocreditGetDto> getAllMicrocreditsByUserByStatus(String transactionStatus) {
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
    public List<ResponseMicrocreditGetDto> getMicrocreditsBetweenDates(String fromDate, String toDate) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();

        List<LocalDateTime> elapsedDate = dateFormatter.getDateFromString(fromDate, toDate);
        LocalDateTime startDate = elapsedDate.get(0);
        LocalDateTime endDate = elapsedDate.get(1);

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("La fecha final no puede ser menor a la inicial.");
        }

        List<Microcredit> microcreditList = microcreditRepository.findMicrocreditsBetweenDates(
                accountAndUser.account.getId(), startDate, endDate);

        if (microcreditList.isEmpty()) {
            throw new ResourceNotFoundException("No posee microcréditos solicitados");
        }

        return microcreditList.stream()
                .map(microcredit -> {
                    List<ResponseContributionDto> contributionsDto = mapContributionsToDto(microcredit.getContributions());

                    return microcreditMapper.responseMicrocreditGet(microcredit, contributionsDto);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseMicrocreditGetDto> getMicrocreditsByDateAndStatus(String date, String transactionStatus) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);

        List<LocalDateTime> elapsedDate = dateFormatter.getDateFromString(date);
        LocalDateTime startDate = elapsedDate.get(0);
        LocalDateTime endDate = elapsedDate.get(1);

        List<Microcredit> microcreditList = microcreditRepository.findMicrocreditsByDateAndTransactionStatus(
                accountAndUser.account.getId(), startDate, endDate, enumStatus);

        if (microcreditList.isEmpty()) {
            throw new ResourceNotFoundException("No posee microcréditos solicitados");
        }

        return microcreditList.stream()
                .map(microcredit -> {
                    List<ResponseContributionDto> contributionsDto = mapContributionsToDto(microcredit.getContributions());

                    return microcreditMapper.responseMicrocreditGet(microcredit, contributionsDto);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ResponseMicrocreditGetDto> getAll() {
        List<Microcredit> microcredits = microcreditRepository.findAll();

        if (microcredits.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron microcréditos.");
        }

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
    @Transactional
    public List<ResponseMicrocreditGetDto> getMicrocreditsByTransactionStatus(String transactionStatus) {
        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);
        List<Microcredit> microcredits = microcreditRepository.findByTransactionStatus(enumStatus);

        if (microcredits.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron microcréditos con el estado especificado.");
        }

        return getResponseMicrocreditGetDtos(microcredits);
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

    public boolean isMicrocreditLimitExceeded(BigDecimal currentAmount) {
        return currentAmount.compareTo(microcreditLimit) > 0;
    }

    private List<ResponseMicrocreditGetDto> getResponseMicrocreditGetDtos(List<Microcredit> microcredits) {
        return microcredits.stream().map(this::getResponseMicrocreditGetDto).collect(Collectors.toList());
    }

    private ResponseMicrocreditGetDto getResponseMicrocreditGetDto(Microcredit microcredit) {
        List<ResponseContributionDto> contributionsDto = microcredit.getContributions().stream()
                .map(contribution -> {

                    String lenderFullname = validations.fullname(contribution.getLenderAccountId());
                    String borrowerFullname = validations.fullname(microcredit.getBorrowerAccountId());

                    return contributionMapper.responseContributionDto(contribution, lenderFullname, borrowerFullname);
                }).collect(Collectors.toList());

        return microcreditMapper.responseMicrocreditGet(microcredit, contributionsDto);
    }

    public BigDecimal totalAmountToPay(Microcredit microcredit) {

        return microcredit.getContributions().stream()
                .map(contribution -> {
                    BigDecimal interest = (contribution.getAmount().multiply(microcredit.getInterestRate())).divide(BigDecimal.valueOf(100));
                    return contribution.getAmount().add(interest);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAmountFinal(Microcredit microcredit) {
        BigDecimal interest = microcredit.getInterestRate().multiply(microcredit.getAmount()).divide(BigDecimal.valueOf(100));

        return microcredit.getAmount().add(interest);
    }

    public void updateMicrocreditAmounts(Microcredit microcredit) {
        BigDecimal totalAmountToPay = totalAmountToPay(microcredit);

        microcredit.setAmountFinal(totalAmountToPay);

        microcredit.setPendingAmount(totalAmountToPay);

        microcreditRepository.save(microcredit);
    }

    private List<ResponseContributionDto> mapContributionsToDto(List<Contribution> contributions) {
        return contributions.stream()
                .map(contribution -> {
                    String lenderFullname = validations.fullname(contribution.getLenderAccountId());
                    String borrowerFullname = validations.fullname(contribution.getMicrocredit().getBorrowerAccountId());

                    return contributionMapper.responseContributionDto(contribution, lenderFullname, borrowerFullname);
                })
                .collect(Collectors.toList());
    }
}
