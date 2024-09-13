package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, String> {
    Optional<Contribution> findByTaxpayerAndCreatedDate(String taxpayer, LocalDate createdDate);
}
