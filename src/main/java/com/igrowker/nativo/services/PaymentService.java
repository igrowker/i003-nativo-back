package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.RequestPaymentDto;
import com.igrowker.nativo.dtos.ResponsePaymentDto;

public interface PaymentService {

    ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto);

}
