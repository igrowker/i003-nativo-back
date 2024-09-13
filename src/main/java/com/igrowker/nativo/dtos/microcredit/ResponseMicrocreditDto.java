package com.igrowker.nativo.dtos.microcredit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditDto(
        String id,
        BigDecimal amount,
        LocalDate createdDate,
        LocalDate expirationDate,
        String description
) {
}
