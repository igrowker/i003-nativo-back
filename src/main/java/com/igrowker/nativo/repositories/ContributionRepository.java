package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Contribution;
import com.igrowker.nativo.entities.Microcredit;
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

    List<Contribution> findByTransactionStatus(TransactionStatus enumStatus);

    List<Contribution> findAllByLenderAccountId(String lenderAccountId);

    @Query("SELECT c FROM Contribution c WHERE (c.lenderAccountId = :idAccount) " +
            "AND c.createdDate >= :startDate AND c.createdDate < :endDate")
    List<Contribution> findContributionsBetweenDates(@Param("idAccount") String idAccount,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}
