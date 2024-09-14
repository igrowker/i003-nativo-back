package com.igrowker.nativo.services.implementation;

import org.springframework.stereotype.Service;
import java.util.Optional;
import java.math.BigDecimal;

import com.igrowker.nativo.dtos.account.AccountDto;
import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.dtos.account.ResponseAccountDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.mappers.AccountMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.AccountService;

import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    @Override
    public void disableAccount(Long dni) {
        Optional<User> userOptional = userRepository.findByDni(dni);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Account account = user.getAccount();

            if (account != null) {
                account.setEnabled(false);
                accountRepository.save(account);
            } else {
                throw new NoResultException("No se encontró una cuenta para el usuario con DNI: " + dni);
            }
        } else {
            throw new NoResultException("No existe un usuario con DNI: " + dni);
        }
    }

    // @Override
    // public ResponseAccountDto addAmount(AccountDto accountDto) {
    //     // Buscar el usuario por el DNI

    //     Optional<User> optionalUser = userRepository.findByDni(accountDto.dni());
    //     System.out.println(optionalUser);
    
    //     if (optionalUser.isPresent()) {
    //         User dbUser = optionalUser.get();
    
    //         // Verificar si el usuario tiene una cuenta asociada
    //         if (dbUser.getAccount() != null) {
    //             Account existingAccount = dbUser.getAccount();
    
    //             // Sumar el monto al existente
    //             BigDecimal newAmount = existingAccount.getAmount().add(accountDto.amount());
    //             existingAccount.setAmount(newAmount);
    
    //             // Guardar los cambios en la cuenta y devolver el DTO actualizado
    //             Account savedAccount = accountRepository.save(existingAccount);
    //             return accountMapper.accountToResponseAccountDto(savedAccount);
    //         } else {
    //             // Si no tiene cuenta, devolver el DTO original
    //             return accountMapper.accountToResponseAccountDto(new Account());
    //         }
    //     }
    
    //     // Si el usuario no existe, lanzar excepción o manejar el caso como prefieras
    //     throw new NoResultException("No existe un usuario con ese DNI.");
    // }
}
