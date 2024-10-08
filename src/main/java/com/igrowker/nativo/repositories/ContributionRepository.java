package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Contribution;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, String> {

    List<Contribution> findAllByLenderAccountId(String lenderAccountId);

    List<Contribution> findByTransactionStatusAndLenderAccountId(TransactionStatus enumStatus, String id);

    @Query("SELECT c FROM Contribution c WHERE (c.lenderAccountId = :idAccount) " +
            "AND c.createdDate >= :startDate AND c.createdDate < :endDate")
    List<Contribution> findContributionsBetweenDates(@Param("idAccount") String idAccount,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    List<Contribution> findByTransactionStatus(TransactionStatus enumStatus);

    @Query("SELECT c FROM Contribution c JOIN c.microcredit m WHERE c.lenderAccountId = :idAccount OR m.borrowerAccountId = :idAccount")
    List<Contribution> findContributionsByAccount(@Param("idAccount") String idAccount);

    @Query("SELECT c FROM Contribution c JOIN c.microcredit m WHERE (c.lenderAccountId = :idAccount OR m.borrowerAccountId = :idAccount) AND c.transactionStatus = :status")
    List<Contribution> findContributionsByStatus(@Param("idAccount") String idAccount, @Param("status") TransactionStatus status);

    @Query("SELECT c FROM Contribution c JOIN c.microcredit m " +
            "WHERE (c.lenderAccountId = :idAccount OR m.borrowerAccountId = :idAccount) " +
            "AND (c.createdDate BETWEEN :fromDate AND :toDate " +
            "OR m.expirationDate BETWEEN :fromDate AND :toDate " +
            "OR m.createdDate BETWEEN :fromDate AND :toDate)")
    List<Contribution> findContributionsByDateRange(@Param("idAccount") String idAccount,
                                                    @Param("fromDate") LocalDateTime fromDate,
                                                    @Param("toDate") LocalDateTime toDate);

    @Query("SELECT c FROM Contribution c WHERE (c.lenderAccountId = :lenderAccountId) " +
            "AND c.createdDate >= :startDate AND c.createdDate < :endDate AND c.transactionStatus = :status")
    List<Contribution> findContributionsByDateAndTransactionStatus(String lenderAccountId, LocalDateTime startDate,
                                                                   LocalDateTime endDate, TransactionStatus status);
}
