package com.igrowker.nativo.dtos.microcredit;

import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ResponseMicrocreditGetDto(
        String id,
        String borrowerAccountId,
        BigDecimal amount,
        BigDecimal remainingAmount,
        LocalDateTime createdDate,
        LocalDateTime expirationDate,
        String title,
        String description,
        TransactionStatus transactionStatus,
        List<ResponseContributionDto> contributions
) {
}
