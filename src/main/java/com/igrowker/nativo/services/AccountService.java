package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.dtos.account.ResponseOtherAccountDto;
import com.igrowker.nativo.dtos.account.ResponseSelfAccountDto;

public interface AccountService {

    ResponseSelfAccountDto addAmount(AddAmountAccountDto addAmountAccountDto);
    ResponseSelfAccountDto readSelfAccount(String id);
    ResponseOtherAccountDto readOtherAccount(String id);
}
