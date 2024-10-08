package com.igrowker.nativo.dtos.donation;

import com.igrowker.nativo.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseDonationRecord(
        String id,

        BigDecimal amount,

        String donorName,

        String donorLastName,

        String accountIdDonor,

        String beneficiaryName,

        String beneficiaryLastName,

        String accountIdBeneficiary,

        TransactionStatus status,

        LocalDateTime createdAt,

        LocalDateTime updateAt
) {
}
