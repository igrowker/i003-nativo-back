package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Query("SELECT p FROM Payment p WHERE p.senderAccount = :idAccount OR p.receiverAccount = :idAccount")
    List<Payment> findPaymentsByAccount(@Param("idAccount") String idAccount);

    @Query("SELECT p FROM Payment p WHERE (p.senderAccount = :idAccount OR p.receiverAccount = :idAccount) AND p.transactionStatus = :status")
    List<Payment> findPaymentsByStatus(@Param("idAccount") String idAccount, @Param("status") TransactionStatus status);

    @Query("SELECT p FROM Payment p WHERE (p.senderAccount = :idAccount OR p.receiverAccount = :idAccount) " +
            "AND p.transactionDate >= :startDate AND p.transactionDate < :endDate")
    List<Payment> findPaymentsByTransactionDate(@Param("idAccount") String idAccount,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE (p.senderAccount = :idAccount OR p.receiverAccount = :idAccount) " +
            "AND p.transactionDate >= :startDate AND p.transactionDate < :endDate")
    List<Payment> findPaymentsBetweenDates(@Param("idAccount") String idAccount,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.senderAccount = :idAccount")
    List<Payment> findPaymentsAsClient(@Param("idAccount") String idAccount);

    @Query("SELECT p FROM Payment p WHERE p.receiverAccount = :idAccount")
    List<Payment> findPaymentsAsSeller(@Param("idAccount") String idAccount);

    @Query("SELECT p FROM Payment p WHERE p.senderAccount = :idAccount AND p.transactionStatus = 'PENDING'")
    List<Payment> findPendingPaymentsBySender(@Param("idAccount") String idAccount);

    Optional<Payment> findById(String id);
}
