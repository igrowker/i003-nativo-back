package com.igrowker.nativo.dtos;

public record RequestProcessPaymentDto(
        Long paymentId,
        Long senderId,
        String status
) {
}
