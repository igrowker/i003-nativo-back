package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MicrocreditRepository extends JpaRepository<Microcredit, Long> {
    /*
    Optional<Microcredit> findByIdAndEnabled(Long id, Boolean enabled);
    Optional<Microcredit> findByRequesterAndEnabled(Long requester, Boolean enabled);
    Optional<Microcredit> findByRequesterAndTransactionStatus(Long sender, TransactionStatus transactionStatus);
    Optional<Microcredit> findByRequesterAndTransactionDate(Long sender, LocalDate transactionDate);

     */
}
