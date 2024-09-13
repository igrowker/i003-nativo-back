package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.entities.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Account dtoToAccount(AddAmountAccountDto addAmountAccountDto);
}
