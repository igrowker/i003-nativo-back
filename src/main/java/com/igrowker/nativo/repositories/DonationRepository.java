package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation,String> {

    Optional<List<Donation>> findByStatus(TransactionStatus status);

    Optional<Donation> findById(String idDonation);

    Optional<List<Donation>> findAllByAccountIdDonor(String accountIdDonor);

    Optional<List<Donation>> findAllByAccountIdBeneficiary(String accountIdBeneficiary);
}
