package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.user.*;
import jakarta.validation.Valid;

public interface AuthenticationService {
    UserDto signUp(@Valid RegisterUserDto registerUserDto);
    LoginUserResponse login(LoginUserDto loginUserDto);
    void verifyUser(VerifyUserDto verifyUserDto);
    void resendVerificationCode(String email);
}
