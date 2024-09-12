package com.igrowker.nativo.dtos.payment;

import java.math.BigDecimal;

public record ResponsePaymentDto(
        Long id,
        Long receiverAccount,
        BigDecimal amount,
        String description,
        String qr
) {
}
