package com.igrowker.nativo.dtos.payment;

import java.math.BigDecimal;

public record ResponseProcessPaymentDto(
        Long id,
        Long senderAccount,
        Long receiverAccount,
        BigDecimal amount,
        String status,
        String message
) {
}