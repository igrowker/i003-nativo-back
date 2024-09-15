package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.user.ResponseRegisterDto;
import com.igrowker.nativo.dtos.user.UpdateUserDto;
import com.igrowker.nativo.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/update")
    public ResponseEntity<ResponseRegisterDto> updateUser(@Valid @RequestBody UpdateUserDto updateUserDto) {
        ResponseRegisterDto responseRegisterDto = userService.updateUser(updateUserDto);
        return ResponseEntity.ok(responseRegisterDto);
    }

}
