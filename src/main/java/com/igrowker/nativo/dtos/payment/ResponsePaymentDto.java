package com.igrowker.nativo.dtos.payment;

import java.math.BigDecimal;

public record ResponsePaymentDto(
        String id,
        String receiverAccount,
        String receiverName,
        String receiverSurname,
        BigDecimal amount,
        String description,
        String qr
) {
}
