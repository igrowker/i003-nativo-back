package com.igrowker.nativo.dtos.user;

public record ResponseUserDto(
    String id,
    Long dni,
    String name,
    String surname,
    String accountId,
    String email,
    String phone,
    String verificationCode
) {}
