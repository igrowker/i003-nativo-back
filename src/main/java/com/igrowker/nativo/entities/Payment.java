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
    private Long sender;
    private Long receiver;
    private BigDecimal amount;
    private String qr;
    private LocalDateTime transactionDate;
    private TransactionStatus transactionStatus;
    private boolean enabled;
}