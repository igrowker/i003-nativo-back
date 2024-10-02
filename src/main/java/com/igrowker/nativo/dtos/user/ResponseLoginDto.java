package com.igrowker.nativo.dtos.user;

public record ResponseLoginDto(
        String id,
        String accountId,
        String token,
        Long expiresIn,
        ResponseUserVerifiedDto user
) {
}
