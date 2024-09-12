package com.igrowker.nativo.dtos;

import com.igrowker.nativo.entities.User;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


public record RequestDonationDto(
        @NotNull
        BigDecimal amount,

        @NotNull
        User donor,

        @NotNull
        User beneficiary
) {}
