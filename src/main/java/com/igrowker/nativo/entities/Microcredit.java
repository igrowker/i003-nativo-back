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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requester;

    private BigDecimal amount;

    private LocalDate createdDate;

    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @OneToMany(mappedBy = "microcredit")
    private List<Contribution> contributions;

    private boolean enabled;

    //Para que se genere de forma automática cuando se cree la entidad
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now();
        this.transactionStatus = TransactionStatus.PENDENT;
    }
}
