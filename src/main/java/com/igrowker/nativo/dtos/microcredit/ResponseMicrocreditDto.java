package com.igrowker.nativo.dtos.microcredit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditDto(
        Long id,
        Long requester,
        BigDecimal amount,
        LocalDate createdDate,
        LocalDate expirationDate
) {
}
