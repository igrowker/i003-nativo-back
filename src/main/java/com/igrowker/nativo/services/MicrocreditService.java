package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.TransactionStatus;

import java.util.List;

public interface MicrocreditService {
    ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto);
    List<ResponseMicrocreditGetDto> getAll();
    ResponseMicrocreditGetDto getOne(String id);
    List<ResponseMicrocreditGetDto> getMicrocreditsByTransactionStatus(String transactionStatus);
}
