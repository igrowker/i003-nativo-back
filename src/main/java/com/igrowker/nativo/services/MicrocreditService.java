package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.microcredit.*;
import com.igrowker.nativo.dtos.payment.ResponseRecordPayment;
import com.igrowker.nativo.entities.Microcredit;
import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.util.List;

public interface MicrocreditService {
    ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) throws MessagingException;

    List<ResponseMicrocreditGetDto> getAll();

    ResponseMicrocreditGetDto getOne(String id);

    List<ResponseMicrocreditGetDto> getMicrocreditsByTransactionStatus(String transactionStatus);

    List<ResponseMicrocreditGetDto> getBy(String transactionStatus);

    ResponseMicrocreditPaymentDto payMicrocredit(String microcreditId) throws MessagingException;

    List<ResponseMicrocreditGetDto> getMicrocreditsBetweenDates(String fromDate, String toDate);

    BigDecimal totalAmountToPay(Microcredit microcredit);
}
