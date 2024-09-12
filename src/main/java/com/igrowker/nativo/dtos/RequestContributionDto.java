package com.igrowker.nativo.dtos;

import java.math.BigDecimal;

public record RequestContributionDto(
        Long microcreditId,
        Long taxpayer,
        BigDecimal amount
) {
}
