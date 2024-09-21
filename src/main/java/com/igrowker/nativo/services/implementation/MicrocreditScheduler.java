package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.entities.Contribution;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.utils.GeneralTransactions;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MicrocreditScheduler {

    private final MicrocreditRepository microcreditRepository;
    private final ContributionRepository contributionRepository;
    private final GeneralTransactions generalTransactions;

    // El job se ejecuta cada día a medianoche, se puede ajustar!
    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Argentina/Buenos_Aires")
    public void checkAndExpireMicrocredits() {

        LocalDate today = LocalDate.now();

        List<Microcredit> expiredMicrocredits = microcreditRepository
                .findByExpirationDateBeforeAndTransactionStatusNotIn(
                        today, List.of(TransactionStatus.EXPIRED, TransactionStatus.COMPLETED));

        for (Microcredit microcredit : expiredMicrocredits) {
            microcredit.setTransactionStatus(TransactionStatus.EXPIRED);
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
                } catch (ResourceNotFoundException e) {
                    e.getMessage();
                    //Notificación al usuario
                }
            }
        }
    }

    private void payMicrocreditAndContributors(Microcredit microcredit) {
        List<Contribution> contributions = microcredit.getContributions();

        if (!contributions.isEmpty()) {
            for (Contribution contribution : contributions) {
                generalTransactions.updateBalances(
                        microcredit.getBorrowerAccountId(),
                        contribution.getLenderAccountId(),
                        contribution.getAmount()
                );

                contribution.setTransactionStatus(TransactionStatus.COMPLETED);
                contributionRepository.save(contribution);
            }

            microcredit.setTransactionStatus(TransactionStatus.COMPLETED);
            microcreditRepository.save(microcredit);
        }
    }
}
