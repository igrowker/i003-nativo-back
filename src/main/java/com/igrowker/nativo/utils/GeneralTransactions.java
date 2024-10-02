package com.igrowker.nativo.utils;

import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class GeneralTransactions {

    private final AccountRepository accountRepository;
    private final MicrocreditRepository microcreditRepository;

    @Transactional
    public void updateBalances(String senderAccountId, String receiverAccountId, BigDecimal transactionAmount) {
        Account senderAccount = accountRepository.findById(senderAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de emisor no encontrada"));

        Account receiverAccount = accountRepository.findById(receiverAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de receptor no encontrada"));

        senderAccount.setAmount(senderAccount.getAmount().subtract(transactionAmount));
        receiverAccount.setAmount(receiverAccount.getAmount().add(transactionAmount));

        Account updatedSenderAccount = accountRepository.save(senderAccount);
        Account updatedReceiverAccount = accountRepository.save(receiverAccount);
    }

    @Transactional
    public void updateBalancesForExpiredMicrocredit(String senderAccountId, String receiverAccountId,
                                                    BigDecimal transactionAmount, Microcredit microcredit) {
        Account senderAccount = accountRepository.findById(senderAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de emisor no encontrada"));

        Account receiverAccount = accountRepository.findById(receiverAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de receptor no encontrada"));

        BigDecimal frozenAmount = microcredit.getFrozenAmount();

        if (frozenAmount.compareTo(transactionAmount) >= 0) {
            microcredit.setFrozenAmount(frozenAmount.subtract(transactionAmount));

            receiverAccount.setAmount(receiverAccount.getAmount().add(transactionAmount));

            microcreditRepository.save(microcredit);
            accountRepository.save(receiverAccount);
        }
    }

}