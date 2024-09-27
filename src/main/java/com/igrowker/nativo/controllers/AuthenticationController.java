package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.user.*;
import com.igrowker.nativo.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/autenticacion")
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/registro")
    public ResponseEntity<ResponseUserNonVerifiedDto> registerUser(@Valid @RequestBody RequestRegisterDto requestRegisterDto) {
        ResponseUserNonVerifiedDto unverifiedRegisteredUser = authenticationService.signUp(requestRegisterDto);
        return new ResponseEntity<>(unverifiedRegisteredUser, HttpStatus.CREATED);
    }

    @PostMapping("/inicio-sesion")
    public ResponseEntity<ResponseLoginDto> loginUser(@Valid @RequestBody RequestLoginDto requestLoginDto) {
        ResponseLoginDto loginResponse = authenticationService.login(requestLoginDto);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verificacion-codigo")
    public ResponseEntity<ResponseUserVerifiedDto> verifyUser(@RequestBody RequestVerifyUserDto verifyUserDto) {
        ResponseUserVerifiedDto verifiedRegisteredUser = authenticationService.verifyUser(verifyUserDto);
        return new ResponseEntity<>(verifiedRegisteredUser, HttpStatus.OK);
    }

    @GetMapping("/reenvio-codigo")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        ResponseUserNonVerifiedDto unverifiedRegisteredUser = authenticationService.resendVerificationCode(email);
        return new ResponseEntity<>(unverifiedRegisteredUser, HttpStatus.OK);
    }
}