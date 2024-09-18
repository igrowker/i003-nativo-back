package com.igrowker.nativo.dtos.microcredit;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditDto(
        String id,
        BigDecimal amount,
        BigDecimal remainingAmount,
        LocalDate createdDate,
        LocalDate expirationDate,
        String title,
        String description,
        TransactionStatus transactionStatus
) {
}
