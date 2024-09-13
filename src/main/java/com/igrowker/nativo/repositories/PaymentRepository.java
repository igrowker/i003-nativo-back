package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findBySenderAccountAndTransactionStatus(String senderAccount, TransactionStatus transactionStatus);
    Optional<Payment> findByReceiverAccountAndTransactionStatus(String receiverAccount, TransactionStatus transactionStatus);

    Optional<Payment> findBySenderAccountAndTransactionDate(String senderAccount, LocalDateTime transactionDate);
    Optional<Payment> findByReceiverAccountAndTransactionDate(String receiverAccount, LocalDateTime transactionDate);

    //Optional<Payment> findByAccountBetweenDates(Long sender, Boolean enabled);
    //ToDo. Write the sql query! If possible, making it only one for both sender and receiver.

}
