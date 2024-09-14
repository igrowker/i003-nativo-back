package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.dtos.account.ResponseAccountDto;
import com.igrowker.nativo.dtos.account.AccountDto;
import com.igrowker.nativo.entities.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Account dtoToAccount(AddAmountAccountDto addAmountAccountDto);
    AddAmountAccountDto accountToAmountAccountDto( Account account) ;

    Account responseAccountDtoToAcount(ResponseAccountDto responseAccountDto);
    ResponseAccountDto accountToResponseAccountDto(Account account);

    Account accountDtoTAccount(AccountDto accountDto);
    AccountDto accountToAccountDto(Account account);

}