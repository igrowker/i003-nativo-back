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
    private BigDecimal amount; // monto solicitado

    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount; // monto faltante para completar el microcredito

    @Column(length = 300)
    private String title;

    @Column(length = 1000)
    private String description; // motivo

    private LocalDate expirationDate; // fecha de vencimiento

    private LocalDate createdDate; // fecha de creacion

    //private Integer installmentCount; //cantidad de cuotas

    //private Integer interestRate; //tasa de interes

    //private Integer remainingInstallments; //cuotas pendientes

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus; // estado de la transaccion

    @OneToMany(mappedBy = "microcredit")
    private List<Contribution> contributions; // listado de contribuyentes

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now();
        this.transactionStatus = TransactionStatus.PENDING;
        this.remainingAmount = this.amount;
        //this.remainingInstallments = this.installmentCount;
    }
}
