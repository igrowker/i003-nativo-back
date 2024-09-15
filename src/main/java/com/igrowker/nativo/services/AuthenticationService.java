package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.user.RequestLoginDto;
import com.igrowker.nativo.dtos.user.ResponseLoginDto;
import com.igrowker.nativo.dtos.user.RequestRegisterDto;
import com.igrowker.nativo.dtos.user.ResponseUserDto;
import jakarta.validation.Valid;

public interface AuthenticationService {
    ResponseUserDto signUp(@Valid RequestRegisterDto requestRegisterDto);
    ResponseLoginDto login(RequestLoginDto requestLoginDto);

}
