package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.*;
import com.igrowker.nativo.mappers.ContributionMapper;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.utils.DateFormatter;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.utils.NotificationService;
import com.igrowker.nativo.validations.Validations;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;
    private final MicrocreditRepository microcreditRepository;
    private final ContributionMapper contributionMapper;
    private final Validations validations;
    private final GeneralTransactions generalTransactions;
    private final NotificationService notificationService;
    private final DateFormatter dateFormatter;

    @Override
    @Transactional
    public ResponseContributionDto createContribution(RequestContributionDto requestContributionDto) throws MessagingException {
        Validations.UserAccountPair userLender = validations.getAuthenticatedUserAndAccount();
        String userLenderId = userLender.account.getId();

        Optional<Microcredit> expiredMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(userLenderId, TransactionStatus.EXPIRED);
        if (expiredMicrocredit.isPresent()) {
            throw new ExpiredTransactionException("No puede contribuir. Presenta un microcrédito vencido. " +
                    "Debe regularizar su deuda.");
        }

        Microcredit microcredit = microcreditRepository.findById(requestContributionDto.microcreditId())
                .orElseThrow(() -> new ResourceNotFoundException("Microcrédito no encontrado"));

        if (microcredit.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("El monto de la contribución debe ser mayor a $ 0.00");
        }

        if (microcredit.getTransactionStatus() == TransactionStatus.ACCEPTED) {
            throw new ResourceAlreadyExistsException("El microcrédito ya tiene la totalidad del monto solicitado.");
        }

        if (microcredit.getTransactionStatus() != TransactionStatus.PENDING) {
            throw new ResourceAlreadyExistsException("No se puede contribuir a un microcrédito con estado " + microcredit.getTransactionStatus());
        }

        if (!validations.isUserAccountMismatch(microcredit.getBorrowerAccountId())) {
            throw new InvalidUserCredentialsException("El usuario contribuyente no puede ser el mismo que el solicitante" +
                    " del microcrédito.");
        }

        if (!validations.validateTransactionUserFunds(requestContributionDto.amount())) {
            throw new ValidationException("Fondos insuficientes");
        }

        updateRemainingAmount(microcredit, requestContributionDto.amount());

        Contribution contribution = contributionMapper.requestDtoToContribution(requestContributionDto);
        contribution.setLenderAccountId(userLender.account.getId());
        contribution.setMicrocredit(microcredit);
        contribution = contributionRepository.save(contribution);

        generalTransactions.updateBalances(userLenderId, contribution.getMicrocredit().getBorrowerAccountId(), contribution.getAmount());

        microcredit = microcreditRepository.save(microcredit);

        String lenderFullname = validations.fullname(contribution.getLenderAccountId());
        String borrowerFullname = validations.fullname(microcredit.getBorrowerAccountId());

        notificationService.sendContributionNotificationToBorrower(microcredit, contribution.getLenderAccountId(), contribution.getAmount());

        return contributionMapper.responseContributionDto(contribution, lenderFullname, borrowerFullname);
    }

    @Override
    public List<ResponseContributionDto> getAllContributionsByUser() {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();

        String lenderAccounId = accountAndUser.account.getId();

        List<Contribution> contributions = contributionRepository.findAllByLenderAccountId(lenderAccounId);

        if (contributions.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron contribuciones.");
        }

        return mapContributionsToDto(contributions);
    }

    @Override
    public List<ResponseContributionDto> getAllContributionsByUserByStatus(String transactionStatus) {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();
        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);

        String lenderAccounId = userBorrower.account.getId();

        List<Contribution> contributions = contributionRepository.findByTransactionStatusAndLenderAccountId(enumStatus,
                lenderAccounId);

        if (contributions.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron contribuciones para el usuario en el estado especificado.");
        }

        return mapContributionsToDto(contributions);
    }

    @Override
    public List<ResponseContributionDto> getContributionsBetweenDates(String fromDate, String toDate) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();

        List<LocalDateTime> elapsedDate = dateFormatter.getDateFromString(fromDate, toDate);
        LocalDateTime startDate = elapsedDate.get(0);
        LocalDateTime endDate = elapsedDate.get(1);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        List<Contribution> contributionList = contributionRepository.findContributionsBetweenDates(
                accountAndUser.account.getId(), startDate, endDate);

        if (contributionList.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron contribuciones en el rango de fechas proporcionado.");
        }

        return mapContributionsToDto(contributionList);
    }

    @Override
    public List<ResponseContributionDto> getContributionsByDateAndStatus(String date, String transactionStatus) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);

        List<LocalDateTime> elapsedDate = dateFormatter.getDateFromString(date);
        LocalDateTime startDate = elapsedDate.get(0);
        LocalDateTime endDate = elapsedDate.get(1);

        List<Contribution> contributionList = contributionRepository.findContributionsByDateAndTransactionStatus(
                accountAndUser.account.getId(), startDate, endDate, enumStatus);

        if (contributionList.isEmpty()) {
            throw new ResourceNotFoundException("No posee contribuciones.");
        }

        return mapContributionsToDto(contributionList);
    }

    @Override
    public List<ResponseContributionDto> getAll() {
        List<Contribution> contributions = contributionRepository.findAll();

        if (contributions.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron contribuciones.");
        }

        return mapContributionsToDto(contributions);
    }

    @Override
    public ResponseContributionDto getOneContribution(String id) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribución no encontrada con id: " + id));

        Microcredit microcredit = microcreditRepository.findById(contribution.getMicrocredit().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Microcrédito no encontrado con id: " + contribution.getMicrocredit().getId()));

        String lenderFullname = validations.fullname(contribution.getLenderAccountId());
        String borrowerFullname = validations.fullname(microcredit.getBorrowerAccountId());

        return contributionMapper.responseContributionDto(contribution, lenderFullname, borrowerFullname);
    }

    @Override
    public List<ResponseContributionDto> getContributionsByTransactionStatus(String transactionStatus) {
        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);
        List<Contribution> contributions = contributionRepository.findByTransactionStatus(enumStatus);

        if (contributions.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron contribuciones con el estado especificado.");
        }

        return mapContributionsToDto(contributions);
    }

    private void updateRemainingAmount(Microcredit microcredit, BigDecimal contributionAmount) {
        if (microcredit.getTransactionStatus() == TransactionStatus.PENDING) {
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
