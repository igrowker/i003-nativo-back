package com.igrowker.nativo.dtos.contribution;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RequestContributionDto(
        @NotNull(message = "El id del Microcrédito a contribuir es obligatorio.")
        String microcreditId,

        @NotNull(message = "El monto a contribuir es obligatorio y debe ser mayor a cero.")
        BigDecimal amount
) {
}