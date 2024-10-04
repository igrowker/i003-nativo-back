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
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String senderName;
    private String senderSurname;
    private String senderAccount;

    private String receiverName;
    private String receiverSurname;
    private String receiverAccount;

    @Column(length=1000)
    private BigDecimal amount;
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(length=1000)
    private String description;

    @Column(length=10000)
    private String qr;

    @PrePersist
    public void onCreate(){
        this.transactionDate = LocalDateTime.now();
        this.transactionStatus = TransactionStatus.PENDING;
    }
}