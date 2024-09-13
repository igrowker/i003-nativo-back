package com.igrowker.nativo.dtos.user;

public record UserDto (
    String id,
    Long dni,
    String name,
    String surname,
    String email,
    String phone

) {}
