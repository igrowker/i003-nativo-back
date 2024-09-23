package com.igrowker.nativo.utils;

import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MicrocreditScheduler {

    private final MicrocreditRepository microcreditRepository;
    private final ContributionRepository contributionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final GeneralTransactions generalTransactions;
    private final NotificationService notificationService;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Argentina/Buenos_Aires")
    public void checkAndExpireMicrocredits() {
        LocalDate today = LocalDate.now();

        List<Microcredit> expiredMicrocredits = microcreditRepository
                .findByExpirationDateBeforeAndTransactionStatusNotIn(
                        today, List.of(TransactionStatus.EXPIRED, TransactionStatus.COMPLETED));

        for (Microcredit microcredit : expiredMicrocredits) {
            List<Contribution> contributions = microcredit.getContributions();

            if (contributions.isEmpty()) {
                microcredit.setTransactionStatus(TransactionStatus.COMPLETED);
            } else {
                microcredit.setTransactionStatus(TransactionStatus.EXPIRED);
            }

            microcreditRepository.save(microcredit);
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 17 * * ?", zone = "America/Argentina/Buenos_Aires") //Todos los días a las 17.00 hs
    public void processPayAutomaticMicrocredits() {
        LocalDate today = LocalDate.now();
        processMicrocreditPayments(today, TransactionStatus.PENDENT);
        processMicrocreditPayments(today, TransactionStatus.ACCEPTED);

    }

    private void processMicrocreditPayments(LocalDate today, TransactionStatus status) {
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
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta del prestatario no encontrada."));

        User borrowerUser = userRepository.findById(borrowerAccount.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta del prestatario no encontrada."));

        BigDecimal totalAmount = contributions.stream()
                .map(Contribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Contribution contribution : contributions) {
            processContribution(contribution, microcredit);

            Account lenderAccount = accountRepository.findById(contribution.getLenderAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta de prestamista no encontrada."));
            User lenderUser = userRepository.findById(lenderAccount.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta de prestamista no encontrada."));

            notificationService.sendPaymentNotification(lenderUser.getEmail(), (lenderUser.getName() + " " + lenderUser.getSurname()),
                    contribution.getAmount(), "Devolución cuota microcrédito",
                    "Te informamos que se ha procesado la devolución de tu contribución al microcrédito con ID: " + microcredit.getId(),
                    "Gracias por tu participación en nuestro programa de microcréditos. Esperamos seguir contando con tu confianza.");
        }

        notificationService.sendPaymentNotification(borrowerUser.getEmail(), (borrowerUser.getName() + " " + borrowerUser.getSurname()),
                totalAmount, "Descuento cuota del microcrédito",
                "Te informamos que se ha procesado el descuento por el microcrédito con ID: " + microcredit.getId(),
                "Si no tienes saldo suficiente en la cuenta en este momento, el monto pendiente se deducirá automáticamente en tu próximo ingreso.");

        microcredit.setTransactionStatus(TransactionStatus.COMPLETED);
        microcreditRepository.save(microcredit);
    }

    private void processContribution(Contribution contribution, Microcredit microcredit) {
        generalTransactions.updateBalances(
                microcredit.getBorrowerAccountId(),
                contribution.getLenderAccountId(),
                contribution.getAmount());

        contribution.setTransactionStatus(TransactionStatus.COMPLETED);
        contributionRepository.save(contribution);
    }
}
