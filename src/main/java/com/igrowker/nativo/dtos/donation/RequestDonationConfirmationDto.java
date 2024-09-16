package com.igrowker.nativo.dtos.donation;

import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RequestDonationConfirmationDto(

        @NotNull(message = "EL Id de la donacion es nulo.")
        String id,

        @NotNull(message = "El Monto de la donacion es nulo.")
        BigDecimal amount,

        @NotNull(message = "El id de la cuenta del donante es nulo.")
        String accountIdDonor,

        @NotNull(message = "El id de la cuenta del beneficiario es nulo.")
        String accountIdBeneficiary,

        @NotNull(message = "El Status de la donacion es nulo.")
        TransactionStatus status
) {
}
