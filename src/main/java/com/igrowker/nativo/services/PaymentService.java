package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.RequestProcessPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponsePaymentDto;
import com.igrowker.nativo.dtos.payment.ResponseProcessPaymentDto;

public interface PaymentService {

    ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto);

    ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto);

}
