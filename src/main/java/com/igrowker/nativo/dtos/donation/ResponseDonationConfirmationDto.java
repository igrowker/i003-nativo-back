package com.igrowker.nativo.dtos.donation;

import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;

import java.math.BigDecimal;

public record ResponseDonationConfirmationDto(
        String id,

        BigDecimal amount,

        User donor,

        User beneficiary,

        TransactionStatus status

) {}
