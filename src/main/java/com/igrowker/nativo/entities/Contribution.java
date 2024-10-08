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
@Table(name = "contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String lenderAccountId;

    @Column(length = 1000)
    private BigDecimal amount;

    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @ManyToOne
    @JoinColumn(name = "microcredit_id", nullable = false)
    private Microcredit microcredit;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.transactionStatus = TransactionStatus.ACCEPTED;
    }
}
