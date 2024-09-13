package com.igrowker.nativo.dtos.payment;

import java.math.BigDecimal;

public record RequestPaymentDto(
        Long receiverAccount,
        BigDecimal amount,
        String description
) {
}
