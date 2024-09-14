package com.igrowker.nativo.dtos.account;

import java.math.BigDecimal;

public record AddAmountAccountDto(
        Long dni,
        BigDecimal amount
) {
}
