package com.igrowker.nativo.dtos.microcredit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditGetDto (
        String id,
        String requester, //Falta la relación entre account y user
        BigDecimal amount,
        BigDecimal amountRest,
        LocalDate createdDate,
        LocalDate expirationDate,
        String description
) {
}
