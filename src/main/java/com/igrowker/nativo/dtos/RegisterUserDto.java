package com.igrowker.nativo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserDto (

    @NotBlank
    Long dni,

    @NotNull
    String name,

    @NotNull
    String surname,

    @Email
    String email,

    @NotBlank
    String password,
    
    @NotNull
    @Size(min=8 , max = 16)
    String phone

) {}
