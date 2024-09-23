package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.microcredit.*;
import jakarta.mail.MessagingException;

import java.util.List;

public interface MicrocreditService {
    ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto);

    List<ResponseMicrocreditGetDto> getAll();

    ResponseMicrocreditGetDto getOne(String id);

    List<ResponseMicrocreditGetDto> getMicrocreditsByTransactionStatus(String transactionStatus);

    List<ResponseMicrocreditGetDto> getBy(String transactionStatus);

    ResponseMicrocreditPaymentDto payMicrocredit(String microcreditId) throws MessagingException;

}
