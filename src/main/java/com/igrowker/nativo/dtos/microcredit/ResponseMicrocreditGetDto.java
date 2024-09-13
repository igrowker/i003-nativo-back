package com.igrowker.nativo.dtos.microcredit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditGetDto (
        Long id,
        Long requester,
        BigDecimal amount,
        LocalDate createdDate,
        LocalDate expirationDate
) {
}
