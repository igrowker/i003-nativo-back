package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.dtos.account.ResponseOtherAccountDto;
import com.igrowker.nativo.dtos.account.ResponseSelfAccountDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.mappers.AccountMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.services.AccountService;
import com.igrowker.nativo.validations.Validations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final Validations validations;

    @Override
    @Transactional
    public ResponseSelfAccountDto addAmount(AddAmountAccountDto addAmountAccountDto) {
        if (validations.isUserAccountMismatch(addAmountAccountDto.id())) {
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
        }
        Account account = accountRepository.findById(addAmountAccountDto.id())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        var previousAmount = account.getAmount();
        account.setAmount(previousAmount.add(addAmountAccountDto.amount()));
        Account savedAccount = accountRepository.save(account);
        return accountMapper.accountToResponseSelfDto(savedAccount);
    }

    @Override
    public ResponseSelfAccountDto readSelfAccount(String id) {
        if (validations.isUserAccountMismatch(id)) {
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
        }
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return accountMapper.accountToResponseSelfDto(account);
    }

    @Override
    public ResponseOtherAccountDto readOtherAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return accountMapper.accountToResponseOtherDto(account);
    }

}
