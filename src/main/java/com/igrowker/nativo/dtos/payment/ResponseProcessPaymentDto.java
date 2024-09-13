package com.igrowker.nativo.dtos.payment;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;

public record ResponseProcessPaymentDto(
        Long id,
        Long senderAccount,
        Long receiverAccount,
        BigDecimal amount,
        TransactionStatus transactionStatus,
        String message
) {
}