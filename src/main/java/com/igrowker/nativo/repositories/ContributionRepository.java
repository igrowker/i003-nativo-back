package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    /*
    Optional<Contribution> findByIdAndEnabled(Long id, Boolean enabled);
    Optional<Contribution> findByTaxpayerAndEnabled(Long taxpayer, Boolean enabled);
    Optional<Contribution> findByTaxpayerAndTransactionDate(Long sender, LocalDate transactionDate);
     */
}
