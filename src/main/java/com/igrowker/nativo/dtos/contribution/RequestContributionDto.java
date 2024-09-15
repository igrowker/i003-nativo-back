package com.igrowker.nativo.dtos.contribution;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RequestContributionDto(
        @NotNull(message = "El id del Microcrédito a contribuir es obligatorio.")
        String microcreditId,
        @NotNull(message = "El monto a contribuir es obligatorio.")
        BigDecimal amount
        //min
) {
}
/*Una vez elegida la opción,
debería abrirse espacio para ingresar el monto a contribuir
 */