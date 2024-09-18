package com.igrowker.nativo.dtos.payment;

import com.igrowker.nativo.entities.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public record RequestProcessPaymentDto(
        @NotNull(message = "El id del pago es obligatorio.")
        String id,
        @NotNull(message = "El id de cuenta del cliente es obligatorio.")
        String senderAccount,
        @NotNull(message = "El status del pago es obligatorio.")
        String transactionStatus
) {
}
