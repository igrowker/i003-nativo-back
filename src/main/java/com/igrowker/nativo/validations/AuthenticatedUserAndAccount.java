package com.igrowker.nativo.validations;

import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
@Configuration
public class AuthenticatedUserAndAccount {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public Account getAuthenticatedUserAccount() {
        String userNameAuthentication = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(userNameAuthentication)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada para el usuario"));
    }

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
}
