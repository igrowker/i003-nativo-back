package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.payment.*;

import java.util.List;

public interface PaymentService {
    ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto);
    ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto);
    List<ResponseHistoryPayment> getAllPayments(String id);
    List<ResponseHistoryPayment> getPaymentsByStatus(String id, String status);

}
