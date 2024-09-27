package com.igrowker.nativo.dtos.user;

import java.time.LocalDate;

public record ResponseUserVerifiedDto(
        String id,
        Long dni,
        String name,
        String surname,
        String accountId,
        String email,
        String phone,
        LocalDate birthday
) {
}
