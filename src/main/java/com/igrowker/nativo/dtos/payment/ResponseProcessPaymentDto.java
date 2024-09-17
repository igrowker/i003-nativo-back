package com.igrowker.nativo.dtos.payment;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseProcessPaymentDto(
        String id,
        String senderAccount,
        String receiverAccount,
        BigDecimal amount,
        TransactionStatus transactionStatus,
        LocalDateTime transactionDate
) {
}