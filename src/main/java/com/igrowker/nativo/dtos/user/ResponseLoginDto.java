package com.igrowker.nativo.dtos.user;

public record ResponseLoginDto(
     String id,
     String token,
    Long expiresIn
){}
