package com.igrowker.miniproject.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
