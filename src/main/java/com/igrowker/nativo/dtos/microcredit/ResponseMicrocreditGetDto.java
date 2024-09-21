package com.igrowker.nativo.dtos.microcredit;

import com.igrowker.nativo.dtos.contribution.ResponseContributionGetDto;
import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResponseMicrocreditGetDto(
        String id,
        String borrowerAccountId,
        BigDecimal amount,
        BigDecimal remainingAmount,
        LocalDate createdDate,
        LocalDate expirationDate,
        String title,
        String description,
        TransactionStatus transactionStatus,
        List<ResponseContributionGetDto> contributions
) {
}
/* debería mostrar un listado de todos los microcréditos disponibles,
aclarando en cada uno el nombre receptor, título, descripción,
monto total a recaudar, fecha de vencimiento
(cuándo recuperará su dinero). */
