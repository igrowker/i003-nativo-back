package com.igrowker.nativo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Long accountNumber;
    private BigDecimal amount = BigDecimal.ZERO;
    private boolean enabled;
    private String userId;
    private BigDecimal reservedAmount = BigDecimal.ZERO;
}
