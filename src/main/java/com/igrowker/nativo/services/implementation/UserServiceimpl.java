package com.igrowker.nativo.services.implementation;

import org.springframework.stereotype.Service;
import java.util.*;
import java.math.BigDecimal;

import com.igrowker.nativo.dtos.user.ResponseUserDto;
import com.igrowker.nativo.dtos.user.UpdateUserDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.mappers.UserMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceimpl implements UserService{
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserMapper userMapper;


    @Override
    public ResponseUserDto updateAccount(UpdateUserDto updateUserDto) {

        Optional<User> userOptional = userRepository.findByDni(updateUserDto.dni());

        if (userOptional.isPresent()) {

            User dbUser = userOptional.orElseThrow();
            
            dbUser.setEmail(updateUserDto.email());
            dbUser.setPhone(updateUserDto.phone());
            dbUser.setName(updateUserDto.name());
            dbUser.setSurname(updateUserDto.surname());

            userRepository.save(dbUser);

            ResponseUserDto responseUserDto = userMapper.userToResponseUserDto(dbUser);

            return responseUserDto;
        } else {
            throw new NoSuchElementException("No existe un user con ese DNI");
        }
    }

    @Override
    public ResponseUserDto assignAccountToUser(UpdateUserDto updateUserDto) {
        Optional<User> userOptional = userRepository.findByDni(updateUserDto.dni());
    
        if (userOptional.isPresent()) {
            User dbUser = userOptional.get();
    
            if (dbUser.getAccount() != null) {
                throw new IllegalStateException("El usuario ya tiene una cuenta asociada.");
            }
    
            Account account = new Account();
            account.setAmount(BigDecimal.ZERO);
            account.setEnabled(true);
            accountRepository.save(account);
    
            dbUser.setAccount(account);
            userRepository.save(dbUser);
    
            ResponseUserDto responseUserDto = userMapper.userToResponseUserDto(dbUser);
            return responseUserDto;
    
        } else {
            throw new NoSuchElementException("No existe un usuario con ese DNI.");
        }
    }
    
    
}