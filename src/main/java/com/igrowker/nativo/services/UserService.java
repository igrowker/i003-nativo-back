package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.user.*;

public interface UserService {
    ResponseUpdateUserDto updateUser(UpdateUserDto updateUserDto);
    ResponseUpdateMailDto updateMail(UpdateMailDto updateMailDto);
    void assignAccountToUser(Long dni, String id);
   
}