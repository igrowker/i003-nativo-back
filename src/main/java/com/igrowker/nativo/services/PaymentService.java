package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.payment.*;

import java.util.List;

public interface PaymentService {
    ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto);
    ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto);
    List<ResponseRecordPayment> getAllPayments();
    List<ResponseRecordPayment> getPaymentsByStatus(String status);
    List<ResponseRecordPayment> getPaymentsByDate(String date);
    List<ResponseRecordPayment> getPaymentsBetweenDates(String fromDate, String toDate);
    List<ResponseRecordPayment> getPaymentsAsClient();
    List<ResponseRecordPayment> getPaymentsAsSeller();
    List<ResponseRecordPayment> getPaymentsByClient(String clientId);
    ResponseRecordPayment getPaymentsById(String id);
    ResponsePaymentDto createQrId(DemodayDtoRequestPayment requestPaymentDto);
}
