package com.igrowker.nativo.dtos.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseTransactionDto(
        String id,
        String transaction,
        BigDecimal amount,
        String senderName,
        String senderSurname,
        String senderAccount,
        String receiverName,
        String receiverSurname,
        String receiverAccount,
        LocalDateTime creationDate,
        LocalDateTime endDate,
        String status
) {}