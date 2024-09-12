package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySenderAndEnabled(Long sender, Boolean enabled);
    Optional<Payment> findByReceiverAndEnabled(Long receiver, Boolean enabled);

    Optional<Payment> findBySenderAndTransactionStatus(Long sender, TransactionStatus transactionStatus);
    Optional<Payment> findByReceiverAndTransactionStatus(Long receiver, TransactionStatus transactionStatus);

    Optional<Payment> findBySenderAndTransactionDate(Long sender, LocalDateTime transactionDate);
    Optional<Payment> findByReceiverAndTransactionDate(Long receiver, LocalDateTime transactionDate);

    //Optional<Payment> findByAccountBetweenDates(Long sender, Boolean enabled);
    //ToDo. Write the sql query! If possible, making it only one for both sender and receiver.

}
