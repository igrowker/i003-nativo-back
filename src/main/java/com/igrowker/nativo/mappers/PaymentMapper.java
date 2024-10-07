package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment requestDtoToPayment(RequestPaymentDto requestPaymentDto);

    @Mapping(source = "accountNumber", target = "receiverAccount")
    ResponsePaymentDto paymentToResponseDto(Payment payment, String accountNumber);

    Payment requestProcessDtoToPayment(RequestProcessPaymentDto requestProcessPaymentDto);

    @Mapping(source = "senderAccountNumber", target = "senderAccount")
    @Mapping(source = "receiverAccountNumber", target = "receiverAccount")
    ResponseProcessPaymentDto paymentToResponseProcessDto(Payment payment, String senderAccountNumber, String receiverAccountNumber);

    @Mapping(source = "senderAccountNumber", target = "senderAccount")
    @Mapping(source = "receiverAccountNumber", target = "receiverAccount")
    ResponseRecordPayment paymentToResponseRecord(Payment payment, String senderAccountNumber, String receiverAccountNumber);

    Payment demodayDtoToPayment(DemodayDtoRequestPayment requestPaymentDto);
}
