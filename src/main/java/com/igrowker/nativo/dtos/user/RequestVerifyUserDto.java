package com.igrowker.nativo.dtos.user;

public record RequestVerifyUserDto(
        String email,
        String verificationCode
) {
}