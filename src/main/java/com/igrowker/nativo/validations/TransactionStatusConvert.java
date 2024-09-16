package com.igrowker.nativo.validations;

import com.igrowker.nativo.entities.TransactionStatus;

public class TransactionStatusConvert {

    public TransactionStatus statusConvert(String transactionStatus) {
        switch (transactionStatus.toUpperCase()) {
            case "PENDENT":
                return TransactionStatus.PENDENT;
            case "ACCEPTED":
                return TransactionStatus.ACCEPTED;
            case "FAILED":
                return TransactionStatus.FAILED;
            case "DENIED":
                return TransactionStatus.DENIED;
            default:
                throw new IllegalArgumentException("El estado de la transacción no existe: " + transactionStatus);
        }
    }
}


