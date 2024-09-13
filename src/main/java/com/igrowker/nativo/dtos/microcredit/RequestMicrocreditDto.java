package com.igrowker.nativo.dtos.microcredit;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestMicrocreditDto(
        @NotNull(message = "El monto solicitado es obligatorio.")
        BigDecimal amount,
        @NotNull(message = "La fecha de vencimiento del Microcrédito solicitado es obligatoria.")
        LocalDate expirationDate,
        String description
) {
}
