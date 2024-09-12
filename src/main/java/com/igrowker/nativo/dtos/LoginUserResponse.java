package com.igrowker.nativo.dtos;

public record LoginUserResponse (
    String token,
    Long expiresIn
){}
