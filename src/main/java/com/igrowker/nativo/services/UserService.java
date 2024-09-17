package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.user.*;

public interface UserService {
    ResponseRegisterDto updateUser(UpdateUserDto updateUserDto);
    void assignAccountToUser(Long dni, String id);
   
}