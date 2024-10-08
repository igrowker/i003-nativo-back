package com.igrowker.nativo.dtos.account;

import java.math.BigDecimal;

public record ResponseSelfAccountDto(
        String id,
        Long accountNumber,
        BigDecimal amount,
        BigDecimal reservedAmount,
        String userId
) {
}
