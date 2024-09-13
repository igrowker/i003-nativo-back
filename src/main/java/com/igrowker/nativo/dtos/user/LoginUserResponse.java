package com.igrowker.nativo.dtos.user;

public record LoginUserResponse (
     String id,
     String token,
    Long expiresIn
){}
