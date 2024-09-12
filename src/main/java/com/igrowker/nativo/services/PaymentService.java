package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.RequestPaymentDto;
import com.igrowker.nativo.dtos.RequestProcessPaymentDto;
import com.igrowker.nativo.dtos.ResponsePaymentDto;
import com.igrowker.nativo.dtos.ResponseProcessPaymentDto;

public interface PaymentService {

    ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto);

    ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto);

}
