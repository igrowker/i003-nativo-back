package com.igrowker.nativo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginUserDto(
        @NotBlank
        String password,
        @NotNull
        String email
) {
}