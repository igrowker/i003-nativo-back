package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.user.LoginUserDto;
import com.igrowker.nativo.dtos.user.LoginUserResponse;
import com.igrowker.nativo.dtos.user.RegisterUserDto;
import com.igrowker.nativo.dtos.user.UserDto;
import com.igrowker.nativo.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/authentication")
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        UserDto registeredUser = authenticationService.signUp(registerUserDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> loginUser(@Valid @RequestBody LoginUserDto loginUserDto) {
        LoginUserResponse loginResponse = authenticationService.login(loginUserDto);
        return ResponseEntity.ok(loginResponse);
    }
}
