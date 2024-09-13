package com.igrowker.nativo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

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

    private LocalDateTime createdAt;


    private LocalDateTime updateAt;

    @OneToOne
    private User donor;

    @OneToOne
    private User beneficiary;

    @PrePersist
    protected void onCreate() {
        this.createdAt =  LocalDateTime.now();
    }


/*
    @PrePersist
    protected void onStatus() {
        this.status =  TransactionStatus.PENDENT;
    }
*/

    @PreUpdate
    protected void updateAt() {
        this.updateAt =  LocalDateTime.now();
    }
}
