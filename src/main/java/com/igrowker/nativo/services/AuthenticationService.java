package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.user.*;
import jakarta.validation.Valid;

public interface AuthenticationService {
    ResponseUserNonVerifiedDto signUp(@Valid RequestRegisterDto requestRegisterDto);

    ResponseLoginDto login(RequestLoginDto requestLoginDto);

    ResponseUserVerifiedDto verifyUser(RequestVerifyUserDto verifyUserDto);

    ResponseUserNonVerifiedDto resendVerificationCode(String email);
}