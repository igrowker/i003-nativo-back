package com.igrowker.nativo.dtos.donation;

import com.igrowker.nativo.entities.User;

import java.math.BigDecimal;
import java.util.Optional;

public record ResponseDonationDto(

        String id,

        BigDecimal amount,

        User donor,

        User beneficiary,


        String status
) {
}
