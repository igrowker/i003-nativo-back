package com.igrowker.nativo.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestMicrocreditDto(
        Long requester,
        BigDecimal amount,
        LocalDate expirationDate
) {
}
