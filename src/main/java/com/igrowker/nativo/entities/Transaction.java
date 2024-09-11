package com.igrowker.nativo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    private String receiver;

    private BigDecimal amount;

    private TransactionType transactionType;

    private TransactionStatus transactionStatus;

    private LocalDateTime acceptedAt;

    private LocalDateTime createdAt;

    private boolean enabled;


    @PrePersist
    protected void onCreate() {
        this.createdAt =  LocalDateTime.now();
    }
    
}
