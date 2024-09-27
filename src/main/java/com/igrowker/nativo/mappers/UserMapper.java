package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.user.*;

import com.igrowker.nativo.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User registerUsertoUser(RequestRegisterDto requestRegisterDto);

    ResponseUserNonVerifiedDto userToUserNonVerifiedDTO(User user);

    ResponseUserVerifiedDto userToUserVerifiedDTO(User user);

    User UpdateUserDtoToUser(UpdateUserDto updateUserDto);

    ResponseUpdateUserDto userToResponseUpdateUserDto(User user);

    User updateMailDtoToUser(UpdateMailDto updateMailDto);

    ResponseUpdateMailDto userToResponsUpdateMailDto(User user);
}
