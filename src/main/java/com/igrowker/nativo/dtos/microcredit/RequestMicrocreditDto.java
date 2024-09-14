package com.igrowker.nativo.dtos.microcredit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestMicrocreditDto(
        @NotNull(message = "El monto solicitado es obligatorio.")
        @Positive(message = "El monto debe ser mayor a 0.")
        BigDecimal amount,
        @NotNull(message = "La fecha de vencimiento del Microcrédito solicitado es obligatoria.")
        LocalDate expirationDate,
        String description
) {
}
