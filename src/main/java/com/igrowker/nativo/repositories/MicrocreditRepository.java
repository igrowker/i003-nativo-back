package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Microcredit;

import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MicrocreditRepository extends JpaRepository<Microcredit, String> {
    List<Microcredit> findAllByBorrowerAccountId(String borrowerAccountId);

    List<Microcredit> findByTransactionStatusAndBorrowerAccountId(TransactionStatus enumStatus, String id);

    @Query("SELECT m FROM Microcredit m WHERE (m.borrowerAccountId = :idAccount) " +
            "AND m.createdDate >= :startDate AND m.createdDate < :endDate")
    List<Microcredit> findMicrocreditsBetweenDates(@Param("idAccount") String idAccount,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM Microcredit m WHERE (m.borrowerAccountId = :borrowerAccountId) " +
            "AND m.createdDate >= :startDate AND m.createdDate < :endDate AND m.transactionStatus = :status")
    List<Microcredit> findMicrocreditsByDateAndTransactionStatus(String borrowerAccountId, LocalDateTime startDate,
                                                                 LocalDateTime endDate, TransactionStatus status);

    List<Microcredit> findByTransactionStatus(TransactionStatus transactionStatus);

    Optional<Microcredit> findByBorrowerAccountIdAndTransactionStatus(String borrowerAccountId, TransactionStatus transactionStatus);

    List<Microcredit> findByExpirationDateBeforeAndTransactionStatusNotIn(LocalDateTime today, List<TransactionStatus> expired);
}
