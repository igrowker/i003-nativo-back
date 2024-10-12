package com.igrowker.nativo.utils;

import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.*;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.MicrocreditService;
import com.igrowker.nativo.validations.Validations;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MicrocreditScheduler {

    private final MicrocreditRepository microcreditRepository;
    private final ContributionRepository contributionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final MicrocreditService microcreditService;
    private final GeneralTransactions generalTransactions;
    private final Validations validations;
    private final NotificationService notificationService;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Argentina/Buenos_Aires")
    public void checkAndExpireMicrocredits() throws MessagingException {
        LocalDateTime today = LocalDateTime.now();

        List<Microcredit> expiredMicrocredits = microcreditRepository
                .findByExpirationDateBeforeAndTransactionStatusNotIn(
                        today, List.of(TransactionStatus.EXPIRED, TransactionStatus.COMPLETED));

        for (Microcredit microcredit : expiredMicrocredits) {
            try {
                List<Contribution> contributions = microcredit.getContributions();

                Account borrowerAccount = accountRepository.findById(microcredit.getBorrowerAccountId())
                        .orElseThrow(() -> new InvalidUserCredentialsException("Cuenta del prestatario no encontrada."));

                User borrowerUser = userRepository.findById(borrowerAccount.getUserId())
                        .orElseThrow(() -> new InvalidUserCredentialsException("Usuario del prestatario no encontrado."));

                if (contributions.isEmpty()) {
                    microcredit.setTransactionStatus(TransactionStatus.COMPLETED);

                    notificationService.sendPaymentNotification(
                            borrowerUser.getEmail(),
                            borrowerUser.getName() + " " + borrowerUser.getSurname(),
                            BigDecimal.ZERO,
                            "Finalización del Microcrédito",
                            "Te informamos que tu microcrédito con ID: " + microcredit.getId() + " ha finalizado" +
                                    " debido a que no recibió ninguna contribución.",
                            "Gracias por participar en nuestro sistema de microcréditos."
                    );
                } else {
                    microcredit.setTransactionStatus(TransactionStatus.EXPIRED);

                    BigDecimal totalAmount = contributions.stream()
                            .map(Contribution::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    notificationService.sendPaymentNotification(
                            borrowerUser.getEmail(),
                            borrowerUser.getName() + " " + borrowerUser.getSurname(),
                            totalAmount,
                            "Microcrédito Vencido",
                            "Te informamos que tu microcrédito con ID: " + microcredit.getId() + " ha vencido porque no se realizó el pago en la fecha de vencimiento.",
                            "Te recomendamos que revises tu cuenta para regularizar la situación."
                    );
                }

                microcreditRepository.save(microcredit);
            } catch (ResourceNotFoundException | MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "America/Argentina/Buenos_Aires")
    public void processPayAutomaticMicrocredits() {
        LocalDateTime today = LocalDateTime.now();
        processMicrocreditPayments(today, TransactionStatus.PENDING);
        processMicrocreditPayments(today, TransactionStatus.ACCEPTED);
    }

    private void processMicrocreditPayments(LocalDateTime today, TransactionStatus status) {
        List<Microcredit> microcredits = microcreditRepository.findByTransactionStatus(status);

        for (Microcredit microcredit : microcredits) {
            if (microcredit.getExpirationDate().isEqual(today)) {
                try {
                    payMicrocreditAndContributors(microcredit);
                } catch (ResourceNotFoundException | MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void payMicrocreditAndContributors(Microcredit microcredit) throws MessagingException {
        List<Contribution> contributions = microcredit.getContributions();

        if (contributions.isEmpty()) return;

        Account borrowerAccount = accountRepository.findById(microcredit.getBorrowerAccountId())
                .orElseThrow(() -> new InvalidAccountException("Cuenta del prestatario no encontrada."));

        User borrowerUser = userRepository.findById(borrowerAccount.getUserId())
                .orElseThrow(() -> new InvalidAccountException("Cuenta del solicitante no encontrada."));

        BigDecimal totalAmount = microcreditService.totalAmountToPay(microcredit);

        if (microcredit.getTransactionStatus() != TransactionStatus.EXPIRED) {
            if (!validations.validateUserFundsForJob(borrowerAccount, totalAmount)) {
                notificationService.sendPaymentNotification(
                        borrowerUser.getEmail(),
                        borrowerUser.getName() + " " + borrowerUser.getSurname(),
                        totalAmount,
                        "Saldo insuficiente para el microcrédito",
                        "No tienes saldo suficiente para pagar el microcrédito con ID: " + microcredit.getId(),
                        "Te recomendamos que deposites fondos en tu cuenta para procesar el pago en el futuro."
                );
                return;
            }
        }

        try {
            List<Contribution> contributionCopy = new ArrayList<>(contributions);

            for (Contribution contribution : contributionCopy) {
                processContribution(contribution, microcredit);

                Account lenderAccount = accountRepository.findById(contribution.getLenderAccountId())
                        .orElseThrow(() -> new InvalidAccountException("Cuenta de prestamista no encontrada."));
                User lenderUser = userRepository.findById(lenderAccount.getUserId())
                        .orElseThrow(() -> new InvalidAccountException("Cuenta de prestamista no encontrada."));

                notificationService.sendPaymentNotification(
                        lenderUser.getEmail(),
                        lenderUser.getName() + " " + lenderUser.getSurname(),
                        totalAmount,
                        "Devolución cuota microcrédito",
                        "Te informamos que se ha procesado la devolución de tu contribución al microcrédito con ID: " + microcredit.getId(),
                        "Gracias por tu participación en nuestro programa de microcréditos. Esperamos seguir contando con tu confianza."
                );
            }

            notificationService.sendPaymentNotification(
                    borrowerUser.getEmail(),
                    borrowerUser.getName() + " " + borrowerUser.getSurname(),
                    totalAmount,
                    "Descuento cuota del microcrédito",
                    "Te informamos que se ha procesado el descuento por el microcrédito con ID: " + microcredit.getId(),
                    "Si no tienes saldo suficiente en la cuenta en este momento, el monto pendiente se deducirá automáticamente en tu próximo ingreso."
            );

            microcredit.setTransactionStatus(TransactionStatus.COMPLETED);
            microcreditRepository.save(microcredit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processContribution(Contribution contribution, Microcredit microcredit) {

        try {
            BigDecimal interest = (contribution.getAmount().multiply(microcredit.getInterestRate())).divide(BigDecimal.valueOf(100));
            BigDecimal totalAmount = contribution.getAmount().add(interest);

            if (microcredit.getTransactionStatus() == TransactionStatus.EXPIRED) {
                generalTransactions.updateBalancesForExpiredMicrocredit(microcredit.getBorrowerAccountId(),
                        contribution.getLenderAccountId(), totalAmount, microcredit);

                BigDecimal newPendingAmount = microcredit.getPendingAmount().subtract(totalAmount);
                microcredit.setPendingAmount(newPendingAmount);
                microcreditRepository.save(microcredit);
            } else {
                generalTransactions.updateBalances(microcredit.getBorrowerAccountId(), contribution.getLenderAccountId(), totalAmount);
            }

            contribution.setTransactionStatus(TransactionStatus.COMPLETED);
            contributionRepository.save(contribution);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "America/Argentina/Buenos_Aires")
    public void processExpiredMicrocreditPayments() {
        LocalDateTime today = LocalDateTime.now();

        List<Microcredit> microcredits = microcreditRepository.findByTransactionStatus(TransactionStatus.EXPIRED);

        for (Microcredit microcredit : microcredits) {
            if (microcredit.getExpirationDate().isBefore(today) || microcredit.getExpirationDate().isEqual(today)) {
                try {
                    microcreditService.updateMicrocreditAmounts(microcredit);

                    BigDecimal currentBalance = accountRepository.getBalanceByUserId(microcredit.getBorrowerAccountId());

                    if (currentBalance.compareTo(microcredit.getPendingAmount()) < 0) {
                        microcredit.setFrozenAmount(microcredit.getFrozenAmount().add(currentBalance));
                        accountRepository.deductBalance(microcredit.getBorrowerAccountId(), currentBalance);
                    }

                    if (microcredit.getFrozenAmount().compareTo(microcredit.getPendingAmount()) >= 0) {
                        payMicrocreditAndContributors(microcredit);
                    }

                    microcreditRepository.save(microcredit);

                } catch (ResourceNotFoundException | MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
