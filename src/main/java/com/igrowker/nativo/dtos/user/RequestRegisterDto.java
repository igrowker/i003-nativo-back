package com.igrowker.nativo.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RequestRegisterDto(
        @NotNull(message = "El DNI es obligatorio.")
        Long dni,

        @NotNull(message = "El nombre es obligatorio.")
        String name,

        @NotNull(message = "El apellido es obligatorio.")
        String surname,

        @NotNull(message = "El email es obligatorio.")
        @Email(message = "Formato de email no válido.")
        String email,

        @NotNull(message = "La contraseña es obligatoria.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        String password,

        @NotNull(message = "El teléfono es obligatorio.")
        String phone

) {
}