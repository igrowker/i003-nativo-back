package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MicrocreditRepository extends JpaRepository<Microcredit, String> {
    //Optional<Microcredit> findByBorrowerAccountAndTransactionStatus(String borrowerAccount, TransactionStatus transactionStatus);

    //Optional<Microcredit> findByBorrowerAccountAndCreatedDate(String borrowerAccount, LocalDate createdDate);
}
