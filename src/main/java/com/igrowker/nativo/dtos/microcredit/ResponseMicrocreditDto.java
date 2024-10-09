package com.igrowker.nativo.dtos.microcredit;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseMicrocreditDto(
        String id,
        BigDecimal amount,
        BigDecimal amountFinal,
        BigDecimal remainingAmount,
        LocalDateTime createdDate,
        LocalDateTime expirationDate,
        String title,
        String description,
        Integer installmentCount,
        BigDecimal interestRate,
        TransactionStatus transactionStatus
) {
}
