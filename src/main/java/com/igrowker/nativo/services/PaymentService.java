package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.Payment;

import java.util.List;

public interface PaymentService {
    ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto);
    ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto);
    List<ResponseHistoryPayment> getAllPayments();
    List<ResponseHistoryPayment> getPaymentsByStatus(String status);
    List<ResponseHistoryPayment> getPaymentsByDate(String date);
}
