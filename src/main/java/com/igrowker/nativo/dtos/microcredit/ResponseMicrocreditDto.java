package com.igrowker.nativo.dtos.microcredit;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditDto(
        String id,
        BigDecimal amount,
        LocalDate createdDate,
        LocalDate expirationDate,
        String description,
        TransactionStatus transactionStatus
) {
}
