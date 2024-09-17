package com.igrowker.nativo.utils;

import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class GeneralTransactions {

    private final AccountRepository accountRepository;

    @Transactional
    public void updateBalances(String senderAccountId, String receiverAccountId, BigDecimal transactionAmount) {
        Account senderAccount = accountRepository.findById(senderAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        Account receiverAccount = accountRepository.findById(receiverAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        senderAccount.setAmount(senderAccount.getAmount().subtract(transactionAmount));
        receiverAccount.setAmount(receiverAccount.getAmount().add(transactionAmount));

        Account updatedSenderAccount =  accountRepository.save(senderAccount);
        Account updatedReceiverAccount =  accountRepository.save(receiverAccount);
    }
}