package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation,String> {

    Optional<List<Donation>> findByStatus(TransactionStatus status);

    Optional<Donation> findById(String idDonation);

    Optional<List<Donation>> findAllByAccountIdDonor(String accountIdDonor);

    Optional<List<Donation>> findAllByAccountIdBeneficiary(String accountIdBeneficiary);

    @Query("SELECT d FROM Donation d WHERE d.accountIdDonor = :idAccount OR d.accountIdBeneficiary = :idAccount")
    List<Donation> findDonationsByAccount(@Param("idAccount") String idAccount);

    @Query("SELECT d FROM Donation d WHERE (d.accountIdDonor = :idAccount OR d.accountIdBeneficiary = :idAccount) AND d.status = :status")
    List<Donation> findDonationsByStatus(@Param("idAccount") String idAccount, @Param("status") TransactionStatus status);

    @Query("SELECT d FROM Donation d WHERE (d.accountIdDonor = :idAccount OR d.accountIdBeneficiary = :idAccount) " +
            "AND (d.createdAt BETWEEN :fromDate AND :toDate OR d.updateAt BETWEEN :fromDate AND :toDate)")
    List<Donation> findDonationsByDateRange(@Param("idAccount") String idAccount,
                                                      @Param("fromDate") LocalDateTime fromDate,
                                                      @Param("toDate") LocalDateTime toDate);
}
