package com.igrowker.nativo.unit.services.implementations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.dtos.user.ResponseUpdateMailDto;
import com.igrowker.nativo.dtos.user.ResponseUpdateUserDto;
import com.igrowker.nativo.dtos.user.UpdateMailDto;
import com.igrowker.nativo.dtos.user.UpdateUserDto;
import com.igrowker.nativo.mappers.UserMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.implementation.UserServiceImpl;
import com.igrowker.nativo.exceptions.InvalidDataException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    class UpdateUserTests {

        @Test
        public void updateUserShouldBeOk() throws Exception {
            String userId = "updateId";
            UpdateUserDto updateUserDto = new UpdateUserDto(userId, "261666666", "John", "Doe");
            ResponseUpdateUserDto expectedResponse = new ResponseUpdateUserDto("261666666", "John", "Doe");
            
            User existingUser = new User(userId, 12345678L, "Jane", "Roe", "jhondoe@doe.com", "Password123!", "261555555", "AccountId", LocalDate.of(2000, 1, 1), LocalDateTime.now(), true, null, null, true, true, true);
            User updatedUser = new User(userId, 12345678L, "John", "Doe", "jhondoe@doe.com", "Password123!", "261666666", "AccountId", LocalDate.of(2000, 1, 1), LocalDateTime.now(), true, null, null, true, true, true);
                        

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);
            when(userMapper.userToResponseUpdateUserDto(updatedUser)).thenReturn(expectedResponse);

            var actualResponse = userService.updateUser(updateUserDto);

            assertThat(actualResponse).isNotNull();
            assertEquals(expectedResponse.phone(), actualResponse.phone(), "El teléfono debería coincidir");
            assertEquals(expectedResponse.name(), actualResponse.name(), "El nombre debería coincidir");
            assertEquals(expectedResponse.surname(), actualResponse.surname(), "El apellido debería coincidir");

            verify(userRepository, times(1)).findById(userId);
            verify(userRepository, times(1)).save(any(User.class));
            verify(userMapper, times(1)).userToResponseUpdateUserDto(any(User.class));
        }

        @Test
        public void updateUserShouldThrowExceptionWhenUserNotFound() {
            UpdateUserDto updateUserDto = new UpdateUserDto("nonExistentId", "261666666", "John", "Doe");

            when(userRepository.findById(updateUserDto.id())).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(updateUserDto),
                    "Debería lanzar ResourceNotFoundException cuando el usuario no existe");

            verify(userRepository).findById(updateUserDto.id());
            verifyNoMoreInteractions(userRepository, userMapper);
        }
    }

    @Nested
    class UpdateMailTests{
        @Test
        public void updateMailShouldbeOk() throws Exception{
            String userId = "ExistingId";
            UpdateMailDto updateMailDto = new UpdateMailDto(userId, "jhondoenewmail@doe.com");
            ResponseUpdateMailDto responseUpdateMailDto = new ResponseUpdateMailDto("jhondoenewmail@doe.com");

            User existingUser = new User(userId, 12345678L, "Jane", "doe", "jhondoe@doe.com", "Password123!", "261555555", "AccountId", LocalDate.of(2000, 1, 1), LocalDateTime.now(), true, null, null, true, true, true);
            User updatedUser = new User(userId, 12345678L, "Jane", "doe", "jhondoenewmail@doe.com", "Password123!", "261555555", "AccountId", LocalDate.of(2000, 1, 1), LocalDateTime.now(), true, null, null, true, true, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);
            when(userMapper.userToResponsUpdateMailDto(updatedUser)).thenReturn(responseUpdateMailDto);

            var actualResponse = userService.updateMail(updateMailDto);

            assertThat(actualResponse).isNotNull();
            assertEquals(responseUpdateMailDto.email(), responseUpdateMailDto.email(), "El correo deberia coincidir");

            verify(userRepository, times(1)).findById(any());
            verify(userRepository, times(1)).save(any());
            verify(userMapper, times(1)).userToResponsUpdateMailDto(any());
        }
        @Test
        public void updateMailShouldNotBeOkBadId() throws Exception {
            UpdateMailDto updateMailDto = new UpdateMailDto("inexistingId", "jhondoe@doe.com");
    
            when(userRepository.findById(updateMailDto.id())).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));
            assertThrows(ResourceNotFoundException.class, () -> userService.updateMail(updateMailDto),
            "Deberia lanzar ResourceNotFoundException cuando el suuario no existe");
    
            verify(userRepository).findById(updateMailDto.id());
            verifyNoMoreInteractions(userRepository, userMapper);
        }
        @Test
        public void updateMailShouldNotbeOkBadEmail() throws Exception {
            UpdateMailDto updateMailDto = new UpdateMailDto("ExistingId", "jhondoe@doe.com");
            User existingUser = new User("ExistingId", 12345678L, "Jane", "doe", "jhondoe@doe.com", "Password123!", "261555555", "AccountId", LocalDate.of(2000, 1, 1), LocalDateTime.now(), true, null, null, true, true, true);

            when(userRepository.findById(updateMailDto.id())).thenReturn(Optional.of(existingUser));
           
            InvalidDataException exception = assertThrows(InvalidDataException.class, 
            () -> userService.updateMail(updateMailDto),
            "Debería lanzar InvalidDataException cuando el nuevo correo es igual al anterior");

            assertEquals("El nuevo correo electrónico es igual al actual.", exception.getMessage());

            verify(userRepository).findById(updateMailDto.id());
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    class assignAccountToUserTests {
        public void assignAccountToUserShouldBeOk() {
        String userId = "testUserId";
        Long userDni = 12345678L;
        User user = new User();
        user.setId(userId);

        Account newAccount = new Account();
        newAccount.setAccountNumber(userDni);
        newAccount.setAmount(BigDecimal.ZERO);
        newAccount.setEnabled(true);
        newAccount.setUserId(userId);

        Account savedAccount = new Account();
        savedAccount.setId("testAccountId");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        userService.assignAccountToUser(userDni, userId);

        verify(userRepository).findById(userId);
        verify(accountRepository).save(argThat(account -> 
            account.getAccountNumber().equals(userDni) &&
            account.getAmount().equals(BigDecimal.ZERO) &&
            account.isEnabled() &&
            account.getUserId().equals(userId)));

        verify(userRepository).save(argThat(updatedUser -> 
            updatedUser.getAccountId().equals("testAccountId")));
    }

    @Test
    public void assignAccountToUserShouldnotBeOk() {
        String userId = "nonExistentUserId";
        Long dni = 12345678L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.assignAccountToUser(dni, userId),
            "Should throw ResourceNotFoundException when user is not found"
        );

        verify(userRepository).findById(userId);
        verifyNoInteractions(accountRepository);
        verify(userRepository, never()).save(any(User.class));
        }

    }

}