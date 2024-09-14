package com.igrowker.nativo.dtos.user;

public record VerifyUserDto(
        String email,
        String verificationCode
) {
}
