package com.igrowker.nativo.dtos.payment;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DemodayDtoRequestPayment(
        @NotNull(message = "El id de cuenta del cliente es obligatorio.")
        String senderAccount,
        @NotNull(message = "El id de cuenta del vendedor es obligatorio.")
        String receiverAccount,
        @NotNull(message = "El monto a pagar es obligatorio.")
        BigDecimal amount,
        String description
) {
}
