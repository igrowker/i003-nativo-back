package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.RequestProcessPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponsePaymentDto;
import com.igrowker.nativo.dtos.payment.ResponseProcessPaymentDto;

import java.util.List;

public interface PaymentService {

    ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto);

    ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto);

    List<ResponsePaymentDto> getAllPayments(String id);

}
