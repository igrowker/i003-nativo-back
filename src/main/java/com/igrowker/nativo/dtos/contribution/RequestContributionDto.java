package com.igrowker.nativo.dtos.contribution;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RequestContributionDto(
        @NotNull(message = "El id del Microcrédito a contribuir es obligatorio.")
        String microcreditId,

        @Positive(message = "El monto a contribuir debe ser mayor a cero.")
        @NotNull(message = "El monto a contribuir es obligatorio y debe ser mayor a cero.")
        BigDecimal amount
) {
}
/*Una vez elegida la opción,
debería abrirse espacio para ingresar el monto a contribuir
 */