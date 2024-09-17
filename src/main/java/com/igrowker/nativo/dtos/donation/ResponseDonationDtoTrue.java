package com.igrowker.nativo.dtos.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseDonationDtoTrue(

        String id,

        BigDecimal amount,

        String accountIdDonor,
        String donorName,
        String donorLastName,

        String accountIdBeneficiary,
        String beneficiaryName,
        String beneficiaryLastName,

        LocalDateTime createdAt,

        String status
) {
}
