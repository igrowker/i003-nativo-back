package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.user.*;

import com.igrowker.nativo.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User registerUsertoUser(RegisterUserDto registerUserDto);
    UserDto userToUserDTO(User user);

    User UpdateUserDtoToUser(UpdateUserDto updateUserDto);
    ResponseUserDto userToResponseUserDto(User user);
}
