package com.igrowker.nativo.validations;

import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InvalidDataException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class Validations {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public UserAccountPair getAuthenticatedUserAndAccount() {
        String userNameAuthentication = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(userNameAuthentication)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada para el usuario: " + user.getEmail()));

        return new UserAccountPair(user, account);
    }

    public static class UserAccountPair {
        public final User user;
        public final Account account;

        public UserAccountPair(User user, Account account) {
            this.user = user;
            this.account = account;
        }
    }

    public boolean isUserAccountMismatch(String userAccount) {
        Account loggedUserAccount = this.getAuthenticatedUserAndAccount().account;
        Account providedUserAccount = accountRepository.findById(userAccount)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta provista no fue encontrada."));

        return !loggedUserAccount.equals(providedUserAccount);
    }

    public boolean validateTransactionUserFunds(BigDecimal TransactionAmount) {
        Account userAccount = this.getAuthenticatedUserAndAccount().account;
        BigDecimal userFunds = userAccount.getAmount();
        BigDecimal reservedFunds = userAccount.getReservedAmount();
        return userFunds.compareTo(TransactionAmount.add(reservedFunds)) >= 0;
    }

    public TransactionStatus statusConvert(String transactionStatus) {
        switch (transactionStatus.toUpperCase()) {
            case "COMPLETED":
                return TransactionStatus.COMPLETED;
            case "EXPIRED":
                return TransactionStatus.EXPIRED;
            case "PENDING":
                return TransactionStatus.PENDING;
            case "ACCEPTED":
                return TransactionStatus.ACCEPTED;
            case "FAILED":
                return TransactionStatus.FAILED;
            case "DENIED":
                return TransactionStatus.DENIED;
            default:
                throw new InvalidDataException("El estado de la transacción no existe: " + transactionStatus);
        }
    }

    public String fullname(String accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() ->
                new ResourceNotFoundException("Cuenta no encontrada"));

        User user = userRepository.findById(account.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException("Usuario no encontrado"));

        return user.getSurname().toUpperCase() + ", " + user.getName();
    }

    public boolean isSecondDateBefore(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            return true;
        } else return false;
    }

    public boolean validateUserFundsForJob(Account userAccount, BigDecimal transactionAmount) {
        if (userAccount == null) {
            throw new IllegalArgumentException("La cuenta del usuario no puede ser nula.");
        }

        BigDecimal userFunds = userAccount.getAmount();
        BigDecimal reservedFunds = userAccount.getReservedAmount();

        return userFunds.compareTo(transactionAmount.add(reservedFunds)) >= 0;
    }
}