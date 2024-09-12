package com.igrowker.nativo.dtos.payment;

public record RequestProcessPaymentDto(
        Long id,
        Long senderAccount,
        String status
) {
}
