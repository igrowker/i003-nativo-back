package com.igrowker.nativo.dtos.user;

public record UserDto (

    Long dni,
    String name,
    String surname,
    String email,
    String phone

) {}
