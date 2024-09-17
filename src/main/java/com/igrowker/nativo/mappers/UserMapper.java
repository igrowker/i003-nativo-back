package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.user.*;

import com.igrowker.nativo.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User registerUsertoUser(RequestRegisterDto requestRegisterDto);
    ResponseUserDto userToUserDTO(User user);

    User UpdateUserDtoToUser(UpdateUserDto updateUserDto);
    ResponseRegisterDto userToResponseUserDto(User user);
}
