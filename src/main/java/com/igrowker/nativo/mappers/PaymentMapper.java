package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.RequestProcessPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponsePaymentDto;
import com.igrowker.nativo.dtos.payment.ResponseProcessPaymentDto;
import com.igrowker.nativo.entities.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment requestDtoToPayment(RequestPaymentDto requestPaymentDto);

    ResponsePaymentDto paymentToResponseDto(Payment payment);

    Payment requestProcessDtoToPayment(RequestProcessPaymentDto requestProcessPaymentDto);

    ResponseProcessPaymentDto paymentToResponseProcessDto(Payment payment);

    List<ResponsePaymentDto> paymentListToResponseDtoList(List<Payment> paymentList);
}
