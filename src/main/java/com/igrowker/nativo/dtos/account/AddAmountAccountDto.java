package com.igrowker.nativo.dtos.account;

import java.math.BigDecimal;

public record AddAmountAccountDto(
        String id,
        BigDecimal amount
) {
}
