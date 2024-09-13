package com.igrowker.nativo.dtos.payment;

import java.math.BigDecimal;

public record ResponsePaymentDto(
        String id,
        String receiverAccount,
        BigDecimal amount,
        String description,
        String qr
) {
}
