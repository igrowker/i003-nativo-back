package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.user.*;
import com.igrowker.nativo.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;

    //Editar nombre, apellido y celular
    @PostMapping("/editar-usuario")
    public ResponseEntity<ResponseUpdateUserDto> updateUser(@Valid @RequestBody UpdateUserDto updateUserDto) {
        ResponseUpdateUserDto responseUpdateRegisterDto = userService.updateUser(updateUserDto);
        return ResponseEntity.ok(responseUpdateRegisterDto);
    }

    // Editar mail 
    @PostMapping("editar-correo")
    public ResponseEntity<ResponseUpdateMailDto> updateMail(@Valid @RequestBody UpdateMailDto updateMailDto) {
        ResponseUpdateMailDto responseUpdateMailDto = userService.updateMail(updateMailDto);
        return ResponseEntity.ok(responseUpdateMailDto);
    }
    
}
