package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.*;
import java.math.BigDecimal;

import com.igrowker.nativo.dtos.user.ResponseRegisterDto;
import com.igrowker.nativo.dtos.user.UpdateUserDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.mappers.UserMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public ResponseRegisterDto updateUser(UpdateUserDto updateUserDto) {
        Optional<User> userOptional = userRepository.findByDni(updateUserDto.dni());
        if (userOptional.isPresent()) {
            User dbUser = userOptional.orElseThrow();
            dbUser.setEmail(updateUserDto.email());
            dbUser.setPhone(updateUserDto.phone());
            dbUser.setName(updateUserDto.name());
            dbUser.setSurname(updateUserDto.surname());
            userRepository.save(dbUser);
            ResponseRegisterDto responseRegisterDto = userMapper.userToResponseUserDto(dbUser);
            return responseRegisterDto;
        } else {
            throw new NoSuchElementException("No existe un user con ese DNI");
        }
    }

    @Override
    @Transactional
    public void assignAccountToUser(Long dni, String id) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
            Account account = new Account();
            account.setAccountNumber(dni);
            account.setAmount(BigDecimal.ZERO);
            account.setEnabled(true);
            account.setUserId(user.getId());
            Account savedAccount = accountRepository.save(account);
            String accountId = savedAccount.getId();
            user.setAccountId(accountId);
            userRepository.save(user);
    }
    
    
}