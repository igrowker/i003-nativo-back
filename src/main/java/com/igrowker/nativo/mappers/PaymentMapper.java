package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.RequestPaymentDto;
import com.igrowker.nativo.dtos.ResponsePaymentDto;
import com.igrowker.nativo.entities.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment requestDtoToPayment(RequestPaymentDto requestPaymentDto);

    ResponsePaymentDto paymentToResponseDto(Payment payment);
}
