package com.igrowker.nativo.dtos.user;

public record LoginUserResponse (
    String token,
    Long expiresIn
){}
