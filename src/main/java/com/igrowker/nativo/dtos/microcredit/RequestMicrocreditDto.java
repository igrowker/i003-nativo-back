package com.igrowker.nativo.dtos.microcredit;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestMicrocreditDto(
        @NotNull(message = "El monto solicitado es obligatorio.")
        @Positive(message = "El monto debe ser mayor a 0.")
        @Max(value=500000, message = "El monto debe ser menor a $500.000.")
        BigDecimal amount,

        @NotNull(message = "La fecha de vencimiento del Microcrédito solicitado es obligatoria.")
        @FutureOrPresent(message = "La fecha de vencimiento debe ser mayor a la fecha de creación")
        LocalDate expirationDate,

        @Size(max = 256, message = "La descripción no puede tener más de 256 caracteres.")
        String description
) {
}
/*Título, Descripción (razón de necesidad del dinero),
Monto total, Fecha de vencimiento (cuando devolverá el dinero)
 */