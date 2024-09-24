package com.igrowker.nativo.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResponseUpdateMailDto(
    
    @Email
    @NotNull(message = "El correo es obligatorio")
    String email
) {}
