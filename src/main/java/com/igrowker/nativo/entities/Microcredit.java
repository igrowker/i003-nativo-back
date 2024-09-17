package com.igrowker.nativo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private String borrowerAccountId; // solicitante

    @Column(length = 1000)
    private BigDecimal amount;

    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;

    @Column(length = 300)
    private String title;

    @Column(length = 1000)
    private String description;

    private LocalDate expirationDate;

    private LocalDate createdDate;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @OneToMany(mappedBy = "microcredit")
    private List<Contribution> contributions;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now();
        this.transactionStatus = TransactionStatus.PENDENT;
        this.remainingAmount = this.amount;
    }
}
