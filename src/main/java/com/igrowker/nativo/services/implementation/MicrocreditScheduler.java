package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MicrocreditScheduler {
    private final MicrocreditRepository microcreditRepository;

    public MicrocreditScheduler(MicrocreditRepository microcreditRepository) {
        this.microcreditRepository = microcreditRepository;
    }

    // El job se ejecuta cada día a medianoche, se puede ajustar!
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkAndExpireMicrocredits() {

        LocalDate today = LocalDate.now();

        List<Microcredit> expiredMicrocredits = microcreditRepository.findByExpirationDateBeforeAndTransactionStatusNot
                (today, TransactionStatus.EXPIRED);

        for (Microcredit microcredit : expiredMicrocredits) {
            microcredit.setTransactionStatus(TransactionStatus.EXPIRED);
            microcreditRepository.save(microcredit);
        }
    }
}
