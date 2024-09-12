package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.user.LoginUserDto;
import com.igrowker.nativo.dtos.user.LoginUserResponse;
import com.igrowker.nativo.dtos.user.RegisterUserDto;
import com.igrowker.nativo.dtos.user.UserDto;
import jakarta.validation.Valid;

public interface AuthenticationService {
    UserDto signUp(@Valid RegisterUserDto registerUserDto);
    LoginUserResponse login(LoginUserDto loginUserDto);

}
