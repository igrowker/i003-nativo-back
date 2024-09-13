package com.igrowker.nativo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String taxpayer;

    @Column(length = 1000)
    private BigDecimal amount;

    private LocalDate createdDate;

    @ManyToOne
    private Microcredit microcredit;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now();
    }
}
