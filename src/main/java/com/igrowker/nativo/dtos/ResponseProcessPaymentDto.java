package com.igrowker.nativo.dtos;

import java.math.BigDecimal;

public record ResponseProcessPaymentDto(
        Long id,
        Long senderId,
        Long receiverId,
        BigDecimal amount,
        String status,
        String message
) {
}