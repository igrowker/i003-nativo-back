package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.exceptions.InvalidDataException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import java.util.*;
import java.math.BigDecimal;

import com.igrowker.nativo.dtos.user.*;
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

    // actualiza campos nombre, apellido y celular
    @Override
    @Transactional
    public ResponseUpdateUserDto updateUser(UpdateUserDto updateUserDto) {
        Optional<User> userOptional = userRepository.findById(updateUserDto.id());
        if (userOptional.isPresent()) {
            User dbUser = userOptional.orElseThrow();
            dbUser.setPhone(updateUserDto.phone());
            dbUser.setName(updateUserDto.name());
            dbUser.setSurname(updateUserDto.surname());
            userRepository.save(dbUser);
            ResponseUpdateUserDto responseUpdateUserDto = userMapper.userToResponseUpdateUserDto(dbUser);
            return responseUpdateUserDto;
        } else {
            throw new ResourceNotFoundException("No existe un usuario con ese id");
        }
    }

    @Override
    @Transactional
    //al modificar el correo, se rompe la sesion actual
    public ResponseUpdateMailDto updateMail(UpdateMailDto updateMailDto) {
        User dbUser = userRepository.findById(updateMailDto.id())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con ese id"));
                
        if (dbUser.getEmail().equals(updateMailDto.email())) {
            throw new InvalidDataException("El nuevo correo electrónico es igual al actual.");
        }
    
        dbUser.setEmail(updateMailDto.email());
        userRepository.save(dbUser);
    
        return userMapper.userToResponsUpdateMailDto(dbUser);
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