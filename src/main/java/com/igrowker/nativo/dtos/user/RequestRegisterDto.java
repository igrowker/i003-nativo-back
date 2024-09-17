package com.igrowker.nativo.dtos.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

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
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
                message = "La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una letra minúscula y un número.")
        String password,

        @NotNull(message = "El teléfono es obligatorio.")
        String phone,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        @JsonFormat(pattern="yyyy-MM-dd")
        @Past(message = "La fecha de nacimiento debe estar en el pasado.")
        LocalDate birthday

) {
}