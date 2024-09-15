package com.igrowker.nativo.dtos.account;


public record ResponseOtherAccountDto(
        String id,
        Long accountNumber,
        String userId
) {
}
