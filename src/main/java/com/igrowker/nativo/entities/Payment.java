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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderAccount;
    private Long receiverAccount;

    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private TransactionStatus transactionStatus;
    private String description;

    private String qr;

    @PrePersist
    public void onCreate(){
        this.transactionDate = LocalDateTime.now();
        this.transactionStatus = TransactionStatus.PENDENT;
    }
}