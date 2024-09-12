package com.igrowker.nativo.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InsufficientFundsException extends RuntimeException {
    private String message;
}
