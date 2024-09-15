package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.account.ResponseOtherAccountDto;
import com.igrowker.nativo.dtos.account.ResponseSelfAccountDto;
import com.igrowker.nativo.entities.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    ResponseSelfAccountDto accountToResponseSelfDto (Account account);
    ResponseOtherAccountDto accountToResponseOtherDto (Account account);
}
