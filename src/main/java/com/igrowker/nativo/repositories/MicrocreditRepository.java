package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MicrocreditRepository extends JpaRepository<Microcredit, String> {
    Optional<Microcredit> findByBorrowerAccountIdAndTransactionStatus(String borrowerAccountId, TransactionStatus transactionStatus);

    Optional<Microcredit> findByBorrowerAccountIdAndCreatedDate(String borrowerAccountId, LocalDate createdDate);

    List<Microcredit> findByTransactionStatus(TransactionStatus transactionStatus);

    List<Microcredit> findByTransactionStatusAndBorrowerAccountId(TransactionStatus enumStatus, String id);

    List<Microcredit> findByExpirationDateBeforeAndTransactionStatusNotIn(LocalDate today, List<TransactionStatus> expired);

}
