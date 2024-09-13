package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MicrocreditRepository extends JpaRepository<Microcredit, String> {
    Optional<Microcredit> findByRequesterAndTransactionStatus(String requester, TransactionStatus transactionStatus);

    Optional<Microcredit> findByRequesterAndCreatedDate(String senderAccount, LocalDate createdDate);
}
