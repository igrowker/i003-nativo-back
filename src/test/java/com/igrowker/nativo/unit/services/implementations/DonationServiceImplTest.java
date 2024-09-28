package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.donation.RequestDonationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationDtoFalse;
import com.igrowker.nativo.dtos.donation.ResponseDonationDtoTrue;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.DonationMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.implementation.DonationServiceImpl;
import com.igrowker.nativo.validations.Validations;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DonationServiceImplTest {

    @Mock
    private DonationRepository donationRepository;
    @Mock
    private DonationMapper donationMapper;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Validations validations;

    @InjectMocks
    private DonationServiceImpl donationServiceImpl;

    @Nested
    class CreateDonationTest {

        @Test
        public void create_donation_true_should_be_Ok() throws Exception {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    true);
            var donation = new Donation("b600b795-6420-412f-b1a5-e2d6501adc2a",
                    BigDecimal.valueOf(100.0), TransactionStatus.PENDING, "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192", true, LocalDateTime.now(), LocalDateTime.now());
            var responseDonationDtoTrue = new ResponseDonationDtoTrue("b600b795-6420-412f-b1a5-e2d6501adc2a",
                    BigDecimal.valueOf(100.0), "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "Mario", "Grande", "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    "Ulises", "Gonzales", LocalDateTime.now(),
                    "PENDING");

            var accountDonor = new Account("348ad942-10aa-42b8-8173-a763c8d9b7e3", 123456789L, BigDecimal.valueOf(1000.0), true, "donorUserId", BigDecimal.ZERO);
            var accountBeneficiary = new Account("218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", 987654321L, BigDecimal.valueOf(500.0), true, "beneficiaryUserId", BigDecimal.ZERO);
            var donorUser = new User("donorUserId", 12345678L, "Mario", "Grande", "mario@gmail.com", "password123", "123456789", "348ad942-10aa-42b8-8173-a763c8d9b7e3", LocalDate.of(1990, 5, 15), LocalDateTime.now(), true, null, null, true, true, true);
            var beneficiaryUser = new User("beneficiaryUserId", 87654321L, "Ulises", "Gonzales", "ulises@gmail.com", "password123", "987654321", "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", LocalDate.of(1995, 3, 22), LocalDateTime.now(), true, null, null, true, true, true);

            // Mockeando el comportamiento de los repositorios y validaciones
            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(accountDonor));
            when(accountRepository.findById("218d6f62-d5cf-423d-a0ac-4df8d7f1d06c")).thenReturn(Optional.of(accountBeneficiary));
            when(userRepository.findById("donorUserId")).thenReturn(Optional.of(donorUser));
            when(userRepository.findById("beneficiaryUserId")).thenReturn(Optional.of(beneficiaryUser));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(donationRepository.save(any(Donation.class))).thenReturn(donation);
            when(donationMapper.requestDtoToDonation(any(RequestDonationDto.class))).thenReturn(donation);

            // Ejecutando el servicio
            ResponseDonationDtoTrue res = donationServiceImpl.createDonationTrue(requestDonationDto);
            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(responseDonationDtoTrue.id());
            assertThat(res.accountIdDonor()).isEqualTo(responseDonationDtoTrue.accountIdDonor());
            assertThat(res.donorName()).isEqualTo(responseDonationDtoTrue.donorName());
            assertThat(res.donorLastName()).isEqualTo(responseDonationDtoTrue.donorLastName());
            assertThat(res.accountIdBeneficiary()).isEqualTo(responseDonationDtoTrue.accountIdBeneficiary());
            assertThat(res.beneficiaryName()).isEqualTo(responseDonationDtoTrue.beneficiaryName());
            assertThat(res.beneficiaryLastName()).isEqualTo(responseDonationDtoTrue.beneficiaryLastName());
            assertThat(res.amount()).isEqualTo(responseDonationDtoTrue.amount());
            assertThat(res.createdAt()).isEqualTo(responseDonationDtoTrue.createdAt());
            assertThat(res.status()).isEqualTo(responseDonationDtoTrue.status());

            // Verificando que se llama a los métodos correctos
            verify(accountRepository, times(2)).findById("348ad942-10aa-42b8-8173-a763c8d9b7e3");
            verify(accountRepository, times(1)).findById("218d6f62-d5cf-423d-a0ac-4df8d7f1d06c");
            verify(userRepository, times(1)).findById("donorUserId");
            verify(userRepository, times(1)).findById("beneficiaryUserId");
            verify(donationRepository).save(any(Donation.class));
            verify(donationMapper).requestDtoToDonation(any(RequestDonationDto.class));


        }

        @Test
        public void create_donation_false_should_be_Ok() throws Exception {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    false);
            var donation = new Donation("b600b795-6420-412f-b1a5-e2d6501adc2a",
                    BigDecimal.valueOf(100.0), TransactionStatus.PENDING, "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192", false, LocalDateTime.now(), LocalDateTime.now());
            var responseDonationDtoFalse = new ResponseDonationDtoFalse("b600b795-6420-412f-b1a5-e2d6501adc2a",
                    BigDecimal.valueOf(100.0), "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", LocalDateTime.now(),
                    "PENDING");

            var accountDonor = new Account("348ad942-10aa-42b8-8173-a763c8d9b7e3", 123456789L, BigDecimal.valueOf(1000.0), true, "donorUserId", BigDecimal.ZERO);

            // Mockeando el comportamiento de los repositorios y validaciones
            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(accountDonor));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(donationRepository.save(any(Donation.class))).thenReturn(donation);
            when(donationMapper.requestDtoToDonation(any(RequestDonationDto.class))).thenReturn(donation);
            when(donationMapper.donationToResponseDtoFalse(any(Donation.class))).thenReturn(responseDonationDtoFalse);

            // Ejecutando el servicio
            ResponseDonationDtoFalse res = donationServiceImpl.createDonationFalse(requestDonationDto);
            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(responseDonationDtoFalse.id());
            assertThat(res.accountIdDonor()).isEqualTo(responseDonationDtoFalse.accountIdDonor());
            assertThat(res.accountIdBeneficiary()).isEqualTo(responseDonationDtoFalse.accountIdBeneficiary());
            assertThat(res.amount()).isEqualTo(responseDonationDtoFalse.amount());
            assertThat(res.createdAt()).isEqualTo(responseDonationDtoFalse.createdAt());
            assertThat(res.status()).isEqualTo(responseDonationDtoFalse.status());

            // Verificando que se llama a los métodos correctos
            verify(accountRepository, times(1)).findById("348ad942-10aa-42b8-8173-a763c8d9b7e3");
            verify(donationRepository).save(any(Donation.class));
            verify(donationMapper).requestDtoToDonation(any(RequestDonationDto.class));
            verify(donationMapper).donationToResponseDtoFalse(any(Donation.class));
        }

        @Test
        public void create_donation_true_ResourceNotFoundException_idDonorAccount() throws Exception {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    "invalid-donor-account-id",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    true);

            when(accountRepository.findById("invalid-donor-account-id"))
                    .thenReturn(Optional.empty());

            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDto);
            });

            String expectedMessage = "El id de la cuenta donante no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_InvalidUserCredentialsException() {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    true);

            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(new Account()));
            when(validations.isUserAccountMismatch(any())).thenReturn(true);

            Exception exception = assertThrows(InvalidUserCredentialsException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDto);
            });

            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }


        @Test
        public void create_donation_true_InsufficientFundsException() {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    true);

            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(new Account()));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(false);

            Exception exception = assertThrows(InsufficientFundsException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDto);
            });

            String expectedMessage = "Tu cuenta no tiene suficientes fondos";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

       /* @Test
        public void create_donation_true_ResourceNotFoundException_idBeneficiaryAccount() throws Exception {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3", // Cuenta del donante válida
                    "invalid-beneficiary-account-id",       // Cuenta beneficiaria inválida
                    true);

            Account donorAccount = new Account(); // Mock de cuenta del donante válido
            donorAccount.setId("348ad942-10aa-42b8-8173-a763c8d9b7e3");
            donorAccount.setUserId("donor-user-id");

            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(donorAccount));
            when(accountRepository.findById("invalid-beneficiary-account-id")).thenReturn(Optional.empty());
            when(validations.isUserAccountMismatch("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(false);
            when(validations.validateTransactionUserFunds(BigDecimal.valueOf(100.0))).thenReturn(true);

            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDto);
            });

            String expectedMessage = "El id de la cuenta beneficiario no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }*/


    }
}