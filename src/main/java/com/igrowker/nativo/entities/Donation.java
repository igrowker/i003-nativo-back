package com.igrowker.nativo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length=1000)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String accountIdDonor;

    private String accountIdBeneficiary;

    private Boolean anonymousDonation;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;


    @PrePersist
    protected void onPrePersist() {
        onCreate();
        onStatus();
    }

    protected void onCreate() {
        this.createdAt =  LocalDateTime.now();
    }


    protected void onStatus() {
        this.status =  TransactionStatus.PENDING;
    }

    @PreUpdate
    protected void updateAt() {
        this.updateAt =  LocalDateTime.now();
    }
}
