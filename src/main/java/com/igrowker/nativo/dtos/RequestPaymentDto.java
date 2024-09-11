package com.igrowker.nativo.dtos;

import java.math.BigDecimal;

public record RequestPaymentDto(
        Long sender,
        Long receiver,
        BigDecimal amount,
        String qr
) {
}
