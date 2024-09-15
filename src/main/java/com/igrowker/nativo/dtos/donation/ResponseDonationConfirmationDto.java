package com.igrowker.nativo.dtos.donation;

import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ResponseDonationConfirmationDto(
        String id,

        BigDecimal amount,


        String accountIdDonor,


        String accountIdBeneficiary,

        TransactionStatus status

) {}
