package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.dtos.account.ResponseOtherAccountDto;
import com.igrowker.nativo.dtos.account.ResponseSelfAccountDto;
import com.igrowker.nativo.dtos.account.ResponseTransactionDto;

import java.util.List;

public interface AccountService {

    ResponseSelfAccountDto addAmount(AddAmountAccountDto addAmountAccountDto);
    ResponseSelfAccountDto readSelfAccount(String id);
    ResponseOtherAccountDto readOtherAccount(String id);
    List<ResponseTransactionDto> getAll();
    List<ResponseTransactionDto> getAllStatus(String status);
    List<ResponseTransactionDto> getAllBetweenDates(String fromDate, String toDate);

}
