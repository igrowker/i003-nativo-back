package com.igrowker.nativo.dtos.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseDonationDtoFalse(

        String id,

        BigDecimal amount,

        String accountIdDonor,

        String beneficiary,

        String accountIdBeneficiary,

        LocalDateTime createdAt,

        String status
) {
}
