package com.igrowker.nativo.dtos.contribution;

import java.math.BigDecimal;

public record RequestContributionDto(
        Long microcreditId,
        Long taxpayer,
        BigDecimal amount
) {
}
