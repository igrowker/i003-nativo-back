package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.user.*;

public interface UserService {
    ResponseUserDto updateAccount(UpdateUserDto updateUserDto);
    ResponseUserDto assignAccountToUser(UpdateUserDto updateUserDto);
   
}