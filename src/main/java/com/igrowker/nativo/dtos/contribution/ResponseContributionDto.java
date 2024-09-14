package com.igrowker.nativo.dtos.contribution;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseContributionDto(
        String id,
        String microcreditId,
        BigDecimal amount,
        LocalDate createdDate,
        String requester
) {
}
