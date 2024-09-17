package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.user.*;
import jakarta.validation.Valid;

public interface AuthenticationService {
    ResponseUserDto signUp(@Valid RequestRegisterDto requestRegisterDto);
    ResponseLoginDto login(RequestLoginDto requestLoginDto);
    void verifyUser(RequestVerifyUserDto verifyUserDto);
    void resendVerificationCode(String email);
}
