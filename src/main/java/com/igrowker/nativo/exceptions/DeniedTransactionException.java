package com.igrowker.nativo.exceptions;

public class DeniedTransactionException extends RuntimeException {
    public DeniedTransactionException(String message) {
        super(message);
    }
}
