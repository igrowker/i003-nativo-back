package com.igrowker.nativo.dtos.donation;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;

public record ResponseDonationRecord(
        String id,

        BigDecimal amount,

        String accountIdDonor,

        String accountIdBeneficiary,

        TransactionStatus status
) {
}
