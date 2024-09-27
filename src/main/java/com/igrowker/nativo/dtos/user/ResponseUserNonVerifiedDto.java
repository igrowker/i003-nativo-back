package com.igrowker.nativo.dtos.user;

import java.time.LocalDate;

public record ResponseUserNonVerifiedDto(
        String id,
        Long dni,
        String name,
        String surname,
        String email,
        String phone,
        LocalDate birthday,
        String verificationCode
) {
}
