package com.igrowker.nativo.dtos.payment;

import com.igrowker.nativo.entities.TransactionStatus;

public record RequestProcessPaymentDto(
        Long id,
        Long senderAccount,
        TransactionStatus transactionStatus
) {
}
