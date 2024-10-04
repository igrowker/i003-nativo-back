package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Contribution;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, String> {
    Optional<Contribution> findByLenderAccountIdAndCreatedDate(String lenderAccountId, LocalDate createdDate);

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
                                                              @Param("fromDate") LocalDate fromDate,
                                                              @Param("toDate") LocalDate toDate);
}
