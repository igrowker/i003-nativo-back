package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment requestDtoToPayment(RequestPaymentDto requestPaymentDto);

    ResponsePaymentDto paymentToResponseDto(Payment payment);

    Payment requestProcessDtoToPayment(RequestProcessPaymentDto requestProcessPaymentDto);

    ResponseProcessPaymentDto paymentToResponseProcessDto(Payment payment);

    List<ResponseHistoryPayment> paymentListToResponseHistoryList(List<Payment> paymentList);
}
