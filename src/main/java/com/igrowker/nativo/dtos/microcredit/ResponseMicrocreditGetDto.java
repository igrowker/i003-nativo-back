package com.igrowker.nativo.dtos.microcredit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseMicrocreditGetDto (
        String id,
        String borrowerAccount, //Falta la relación entre account y user
        BigDecimal amount,
        BigDecimal amountRest,
        LocalDate createdDate,
        LocalDate expirationDate,
        String description
) {
}
/* debería mostrar un listado de todos los microcréditos disponibles,
aclarando en cada uno el nombre receptor, título, descripción,
monto total a recaudar, fecha de vencimiento
(cuándo recuperará su dinero). */
