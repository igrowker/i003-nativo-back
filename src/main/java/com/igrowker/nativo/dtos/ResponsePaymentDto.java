package com.igrowker.nativo.dtos;

import java.math.BigDecimal;

public record ResponsePaymentDto(
        Long id,
        Long receiver,
        BigDecimal amount,
        String description,
        String qr
) {
}
