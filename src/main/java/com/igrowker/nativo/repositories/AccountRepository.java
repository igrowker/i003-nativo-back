package com.igrowker.nativo.repositories;

import com.igrowker.nativo.entities.Account;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUserId(String userId);
    Optional<Account> findByAccountNumber(Long accountNumber);

    @Query("SELECT a.amount FROM Account a WHERE a.id = :accountId")
    BigDecimal getBalanceByUserId(@Param("accountId") String accountId);

    @Modifying
    @Query("UPDATE Account a SET a.amount = a.amount - :amount WHERE a.id = :accountId")
    void deductBalance(@Param("accountId") String accountId, @Param("amount") BigDecimal amount);

   @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findAccountByNumberAccount(@Param("accountNumber") Long accountNumber);
}
