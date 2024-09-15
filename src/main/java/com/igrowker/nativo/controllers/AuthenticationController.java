package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.user.RequestLoginDto;
import com.igrowker.nativo.dtos.user.ResponseLoginDto;
import com.igrowker.nativo.dtos.user.RequestRegisterDto;
import com.igrowker.nativo.dtos.user.ResponseUserDto;
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
    public ResponseEntity<ResponseUserDto> registerUser(@Valid @RequestBody RequestRegisterDto requestRegisterDto) {
        ResponseUserDto registeredUser = authenticationService.signUp(requestRegisterDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseLoginDto> loginUser(@Valid @RequestBody RequestLoginDto requestLoginDto) {
        ResponseLoginDto loginResponse = authenticationService.login(requestLoginDto);
        return ResponseEntity.ok(loginResponse);
    }
}
