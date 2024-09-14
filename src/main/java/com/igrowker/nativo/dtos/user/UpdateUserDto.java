package com.igrowker.nativo.dtos.user;

public record UpdateUserDto (
    Long dni,
    String email,
    String phone,
    String name,
    String surname
)
{}
