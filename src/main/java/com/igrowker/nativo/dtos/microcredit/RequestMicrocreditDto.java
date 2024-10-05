package com.igrowker.nativo.dtos.microcredit;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RequestMicrocreditDto(
        @NotNull(message = "El título solicitado es obligatorio.")
        @Size(max = 256, message = "El título no puede tener más de 256 caracteres.")
        String title,

        @Size(max = 256, message = "La descripción no puede tener más de 256 caracteres.")
        String description,

        @NotNull(message = "El monto solicitado es obligatorio.")
        BigDecimal amount
) {
}