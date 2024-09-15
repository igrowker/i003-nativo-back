package com.igrowker.nativo.dtos.microcredit;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestMicrocreditDto(
        @NotNull(message = "El monto solicitado es obligatorio.")
        @Positive(message = "El monto debe ser mayor a 0.")
        BigDecimal amount,

        @NotNull(message = "La fecha de vencimiento del Microcrédito solicitado es obligatoria.")
        @FutureOrPresent
        @Max(value = 31, message = "La fecha de vencimiento no puede ser mayor a un mes")
        LocalDate expirationDate,

        @Size(max = 256, message = "La descripción no puede tener más de 256 caracteres.")
        String description
) {
}
/*Título, Descripción (razón de necesidad del dinero),
Monto total, Fecha de vencimiento (cuando devolverá el dinero)
 */