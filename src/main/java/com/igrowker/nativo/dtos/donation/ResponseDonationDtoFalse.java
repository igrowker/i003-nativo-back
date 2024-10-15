package com.igrowker.nativo.dtos.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseDonationDtoFalse(

        String id,

        BigDecimal amount,

        Long beneficiaryAccountNumber,

        String beneficiaryName,

        String beneficiaryLastName,

        LocalDateTime createdAt,

        String status
) {
}
