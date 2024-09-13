package com.igrowker.nativo.dtos.donation;

import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RequestDonationConfirmationDto(

        @NotNull(message = "El Monto de la donacion es nulo.")
        BigDecimal amount,

        @NotNull(message = "El Donante de la donacion es nulo.")
        User donor,

        @NotNull(message = "El Beneficiario de la donacion es nulo.")
        User beneficiary,

        @NotNull(message = "El Status de la donacion es nulo.")
        TransactionStatus status
) {
}
