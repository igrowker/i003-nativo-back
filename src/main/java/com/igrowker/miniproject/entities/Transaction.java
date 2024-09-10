package com.igrowker.miniproject.entities;

import jakarta.persistence.Entity;

@Entity
public class Transaction {

    private Long id;

    private String sender;

    private String receiver;

    private TransactionType transactionType;

    private TransactionStatus transactionStatus;

}
