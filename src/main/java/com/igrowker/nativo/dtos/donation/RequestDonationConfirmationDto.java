package com.igrowker.nativo.dtos.donation;

import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RequestDonationConfirmationDto(

        @NotNull(message = "EL Id de la donacion es nulo.")
        String id,

        @NotNull(message = "El Status de la donacion es nulo.")
        TransactionStatus status
) {
}
