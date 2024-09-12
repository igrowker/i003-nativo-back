package com.igrowker.nativo.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseContributionDto(
        Long id,
        Long microcreditId,
        BigDecimal amount,
        LocalDate createdDate
        //Long requester?
) {
}

