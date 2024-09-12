package com.igrowker.nativo.dtos;

import java.math.BigDecimal;

public record RequestPaymentDto(
        Long receiver,
        BigDecimal amount,
        String description
) {
}
