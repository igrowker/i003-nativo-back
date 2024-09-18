package com.igrowker.nativo.dtos.microcredit;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditGetDto(
        String id,
        String borrowerAccount,
        BigDecimal amount,
        BigDecimal remainingAmount,
        LocalDate createdDate,
        LocalDate expirationDate,
        String title,
        String description,
        TransactionStatus transactionStatus
) {
}
/* debería mostrar un listado de todos los microcréditos disponibles,
aclarando en cada uno el nombre receptor, título, descripción,
monto total a recaudar, fecha de vencimiento
(cuándo recuperará su dinero). */
