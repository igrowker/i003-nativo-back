package com.igrowker.nativo.dtos.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseDonationDtoFalse(

        String id,

        BigDecimal amount,

        String donor,

        String beneficiary,

        LocalDateTime createdAt,

        String status
) {
}
