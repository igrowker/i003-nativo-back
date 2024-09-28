package com.igrowker.nativo.dtos.microcredit;

import com.igrowker.nativo.entities.TransactionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditDto(
        String id,
        BigDecimal amount,
        BigDecimal amountFinal,
        BigDecimal remainingAmount,
        LocalDate createdDate,
        LocalDate expirationDate,
        String title,
        String description,
        Integer installmentCount,
        BigDecimal interestRate,
        TransactionStatus transactionStatus
) {
}
