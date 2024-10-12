package com.igrowker.nativo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "microcredits")
public class Microcredit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String borrowerAccountId;

    @Column(length = 1000)
    private BigDecimal amount;

    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;

    @Column
    private BigDecimal amountFinal;

    private BigDecimal pendingAmount = BigDecimal.ZERO;

    private BigDecimal frozenAmount = BigDecimal.ZERO;

    @Column(length = 300)
    private String title;

    @Column(length = 1000)
    private String description;

    private LocalDateTime expirationDate;

    private LocalDateTime createdDate;

    private Integer installmentCount;

    private BigDecimal interestRate = BigDecimal.valueOf(10);

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @OneToMany(mappedBy = "microcredit")
    private List<Contribution> contributions;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.expirationDate = LocalDateTime.now().plusDays(30);
        this.transactionStatus = TransactionStatus.PENDING;
        this.installmentCount = 1;
        this.remainingAmount = this.amount;
    }
}
