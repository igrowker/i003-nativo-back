package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.microcredit.*;
import com.igrowker.nativo.entities.Microcredit;
import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.util.List;

public interface MicrocreditService {
    ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) throws MessagingException;

    ResponseMicrocreditPaymentDto payMicrocredit(String microcreditId) throws MessagingException;

    List<ResponseMicrocreditGetDto> getAllMicrocreditsByUser();

    List<ResponseMicrocreditGetDto> getAllMicrocreditsByUserByStatus(String transactionStatus);

    List<ResponseMicrocreditGetDto> getMicrocreditsBetweenDates(String fromDate, String toDate);

    List<ResponseMicrocreditGetDto> getMicrocreditsByDateAndStatus(String date, String status);

    List<ResponseMicrocreditGetDto> getAll();

    ResponseMicrocreditGetDto getOne(String id);

    List<ResponseMicrocreditGetDto> getMicrocreditsByTransactionStatus(String transactionStatus);

    BigDecimal totalAmountToPay(Microcredit microcredit);

    void updateMicrocreditAmounts(Microcredit microcredit);
}
