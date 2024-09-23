package com.igrowker.nativo.dtos.microcredit;

import java.math.BigDecimal;

public record ResponseMicrocreditPaymentDto(
        String id,
        BigDecimal totalPaidAmount
) {
}
