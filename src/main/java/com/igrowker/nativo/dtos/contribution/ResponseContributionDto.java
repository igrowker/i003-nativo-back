package com.igrowker.nativo.dtos.contribution;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseContributionDto(
        String id,
        String lenderAccountId,
        String lenderFullname,
        String borrowerFullname,
        String microcreditId,
        BigDecimal amount,
        LocalDateTime createdDate,
        LocalDateTime expiredDateMicrocredit,
        TransactionStatus transactionStatus
) {
}
