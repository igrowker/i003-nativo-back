package com.igrowker.nativo.validations;

import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class TransactionValidations {
    private final AuthenticatedUserAndAccount authService;
    private final AccountRepository accountRepository;

    public boolean isUserAccountMismatch(String userAccount) {
        Account loggedUserAccount = authService.getAuthenticatedUserAccount();
        Account providedUserAccount = accountRepository.findById(userAccount)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta provista no fue encontrada."));

        return !loggedUserAccount.equals(providedUserAccount);
    }


    public boolean validateTransactionUserFunds(BigDecimal TransactionAmount){
        Account userAccount = authService.getAuthenticatedUserAccount();
        BigDecimal userFunds = userAccount.getAmount();
        return userFunds.compareTo(TransactionAmount)>=0;
    }
}