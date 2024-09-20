package com.igrowker.nativo.dtos.payment;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseRecordPayment(
        String id,
        String senderAccount,
        String receiverAccount,
        BigDecimal amount,
        String description,
        LocalDateTime transactionDate,
        TransactionStatus transactionStatus
) {
}
