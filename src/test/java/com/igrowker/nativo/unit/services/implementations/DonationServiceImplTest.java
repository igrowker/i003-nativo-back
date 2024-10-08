package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.donation.*;
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
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.validations.Validations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Mock
    private GeneralTransactions generalTransactions;
    @Mock
    private DonationServiceImpl donationServiceImplTest;

    @InjectMocks
    private DonationServiceImpl donationServiceImpl;

    //esto es para el beforeeach
    private Donation donation;
    private RequestDonationDto requestDonationDtoTrue;
    private RequestDonationDto requestDonationDtoFalse;
    private RequestDonationDto requestDonationDtoResourceNotFoundException;
    private ResponseDonationDtoTrue responseDonationDtoTrue;
    private ResponseDonationDtoFalse responseDonationDtoFalse;
    private User userDonor;
    private User userBeneficiary;
    private Account accountDonor;
    private Account accountBeneficiary;
    @BeforeEach
    public void setupTest() {
        requestDonationDtoTrue = new RequestDonationDto(BigDecimal.valueOf(100.0),
                "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                true);
        donation = new Donation("b600b795-6420-412f-b1a5-e2d6501adc2a",
                BigDecimal.valueOf(100.0), TransactionStatus.PENDING, "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", true, LocalDateTime.now(), LocalDateTime.now());
        responseDonationDtoTrue = new ResponseDonationDtoTrue("b600b795-6420-412f-b1a5-e2d6501adc2a",
                BigDecimal.valueOf(100.0), "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                "Mario", "Grande", "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                "Ulises", "Gonzales", LocalDateTime.now(),
                "PENDING");
        requestDonationDtoFalse = new RequestDonationDto(BigDecimal.valueOf(100.0),
                "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                false);
        responseDonationDtoFalse = new ResponseDonationDtoFalse("b600b795-6420-412f-b1a5-e2d6501adc2a",
                BigDecimal.valueOf(100.0), "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", LocalDateTime.now(),
                "PENDING");

        accountDonor = new Account("348ad942-10aa-42b8-8173-a763c8d9b7e3", 123456789L, BigDecimal.valueOf(1000.0), true, "donorUserId", BigDecimal.ZERO);
        accountBeneficiary = new Account("218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", 987654321L, BigDecimal.valueOf(500.0), true, "beneficiaryUserId", BigDecimal.ZERO);
        userDonor = new User("donorUserId", 12345678L, "Mario", "Grande", "mario@gmail.com", "password123", "123456789", "348ad942-10aa-42b8-8173-a763c8d9b7e3", LocalDate.of(1990, 5, 15), LocalDateTime.now(), true, null, null, true, true, true);
        userBeneficiary = new User("beneficiaryUserId", 87654321L, "Ulises", "Gonzales", "ulises@gmail.com", "password123", "987654321", "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", LocalDate.of(1995, 3, 22), LocalDateTime.now(), true, null, null, true, true, true);

        requestDonationDtoResourceNotFoundException = new RequestDonationDto(BigDecimal.valueOf(100.0),
                "invalid-donor-account-id",
                "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                true);
    }


    @Nested
    class CreateDonationTrueTest {

        @Test
        public void create_donation_true_should_be_Ok() throws Exception {

            // Mockeando el comportamiento de los repositorios y validaciones
            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(accountDonor));
            when(accountRepository.findById("218d6f62-d5cf-423d-a0ac-4df8d7f1d06c")).thenReturn(Optional.of(accountBeneficiary));
            when(userRepository.findById("donorUserId")).thenReturn(Optional.of(userDonor));
            when(userRepository.findById("beneficiaryUserId")).thenReturn(Optional.of(userBeneficiary));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(donationRepository.save(any(Donation.class))).thenReturn(donation);
            when(donationMapper.requestDtoToDonation(any(RequestDonationDto.class))).thenReturn(donation);
            when(accountRepository.save(any(Account.class))).thenReturn(accountDonor);


            // Ejecutando el servicio
            ResponseDonationDtoTrue res = donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
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
            verify(accountRepository, times(1)).findById("348ad942-10aa-42b8-8173-a763c8d9b7e3");
            verify(accountRepository, times(1)).findById("218d6f62-d5cf-423d-a0ac-4df8d7f1d06c");
            verify(userRepository, times(1)).findById("donorUserId");
            verify(userRepository, times(1)).findById("beneficiaryUserId");
            verify(donationRepository).save(any(Donation.class));
            verify(donationMapper).requestDtoToDonation(any(RequestDonationDto.class));
            verify(accountRepository).save(any(Account.class));


        }

        @Test
        public void create_donation_true_ResourceNotFoundException_idDonorAccount() throws Exception {
            when(accountRepository.findById("invalid-donor-account-id"))
                    .thenReturn(Optional.empty());

            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDtoResourceNotFoundException);
            });

            String expectedMessage = "El id de la cuenta donante no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_InvalidUserCredentialsException() {

            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(new Account()));
            when(validations.isUserAccountMismatch(any())).thenReturn(true);

            Exception exception = assertThrows(InvalidUserCredentialsException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
            });

            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }


        @Test
        public void create_donation_true_InsufficientFundsException() {

            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(new Account()));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(false);

            Exception exception = assertThrows(InsufficientFundsException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
            });

            String expectedMessage = "Tu cuenta no tiene suficientes fondos";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_ResourceNotFoundException_idBeneficiaryAccount() {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "invalid-beneficiary-account-id",
                    true);

            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(new Account()));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findById("invalid-beneficiary-account-id"))
                    .thenReturn(Optional.empty());

            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDto);
            });

            String expectedMessage = "El id de la cuenta beneficiario no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_ResourceNotFoundException_idDonorUser() {

            Account donorAccount = new Account();
            donorAccount.setUserId("invalid-donor-user-id");

            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(donorAccount));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findById("218d6f62-d5cf-423d-a0ac-4df8d7f1d06c")).thenReturn(Optional.of(new Account()));
            when(userRepository.findById("invalid-donor-user-id"))
                    .thenReturn(Optional.empty());


            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
            });

            String expectedMessage = "El id del usuario donante no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_ResourceNotFoundException_idBeneficiaryUser() {

            Account beneficiaryAccount = new Account();
            beneficiaryAccount.setUserId("invalid-beneficiary-user-id");

            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(accountDonor));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findById("218d6f62-d5cf-423d-a0ac-4df8d7f1d06c")).thenReturn(Optional.of(beneficiaryAccount));
            when(userRepository.findById(accountDonor.getUserId())).thenReturn(Optional.of(new User()));
            when(userRepository.findById("invalid-beneficiary-user-id"))
                    .thenReturn(Optional.empty());


            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
            });

            String expectedMessage = "El id del usuario beneficiario no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }


    }

    @Nested
    class CreateDonationFalseTest {
        @Test
        public void create_donation_false_should_be_Ok() throws Exception {

            // Mockeando el comportamiento de los repositorios y validaciones
            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(accountDonor));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(donationRepository.save(any(Donation.class))).thenReturn(donation);
            when(donationMapper.requestDtoToDonation(any(RequestDonationDto.class))).thenReturn(donation);
            when(donationMapper.donationToResponseDtoFalse(any(Donation.class))).thenReturn(responseDonationDtoFalse);

            // Ejecutando el servicio
            ResponseDonationDtoFalse res = donationServiceImpl.createDonationFalse(requestDonationDtoFalse);
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
        public void create_donation_false_InvalidUserCredentialsException() {

            when(validations.isUserAccountMismatch(any())).thenReturn(true);

            Exception exception = assertThrows(InvalidUserCredentialsException.class, () -> {
                donationServiceImpl.createDonationFalse(requestDonationDtoFalse);
            });

            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_false_InsufficientFundsException() {

            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(false);

            Exception exception = assertThrows(InsufficientFundsException.class, () -> {
                donationServiceImpl.createDonationFalse(requestDonationDtoFalse);
            });

            String expectedMessage = "Tu cuenta no tiene suficientes fondos";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_false_ResourceNotFoundException_idDonorAccount() throws Exception {

            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findById("invalid-donor-account-id"))
                    .thenReturn(Optional.empty());

            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationFalse(requestDonationDtoResourceNotFoundException);
            });

            String expectedMessage = "El id de la cuenta donante no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class ConfirmationDonationTest {

        @Test
        public void confirmation_donation_should_be_Ok() throws Exception {
            var requestDonationConfirmationDto = new RequestDonationConfirmationDto("b600b795-6420-412f-b1a5-e2d6501adc2a",
                    "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    TransactionStatus.ACCEPTED);
            var responseDonationConfirmationDto = new ResponseDonationConfirmationDto("b600b795-6420-412f-b1a5-e2d6501adc2a",
                    BigDecimal.valueOf(100.0), "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", TransactionStatus.ACCEPTED);

            // Mockeando el comportamiento de los repositorios y validaciones
            when(donationRepository.findById(donation.getId())).thenReturn(Optional.of(donation));
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(donationMapper.requestConfirmationDtoToDonation(any(RequestDonationConfirmationDto.class))).thenReturn(donation);
            when(accountRepository.findById("348ad942-10aa-42b8-8173-a763c8d9b7e3")).thenReturn(Optional.of(accountDonor));
            doNothing().when(donationServiceImplTest).returnAmount(donation.getAccountIdDonor(),donation.getAmount());
            doNothing().when(generalTransactions).updateBalances(donation.getAccountIdDonor(), donation.getAccountIdBeneficiary(),donation.getAmount());
            when(donationRepository.save(any(Donation.class))).thenReturn(donation);
            when(donationMapper.donationToResponseConfirmationDto(any(Donation.class))).thenReturn(responseDonationConfirmationDto);



            // Ejecutando el servicio
            ResponseDonationConfirmationDto res = donationServiceImpl.confirmationDonation(requestDonationConfirmationDto);
            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(responseDonationConfirmationDto.id());
            assertThat(res.amount()).isEqualTo(responseDonationConfirmationDto.amount());
            assertThat(res.accountIdDonor()).isEqualTo(responseDonationConfirmationDto.accountIdDonor());
            assertThat(res.accountIdBeneficiary()).isEqualTo(responseDonationConfirmationDto.accountIdBeneficiary());
            assertThat(res.status()).isEqualTo(responseDonationConfirmationDto.status());

            // Verificando que se llama a los métodos correctos
            verify(donationRepository, times(1)).findById(donation.getId());
            verify(validations, times(1)).isUserAccountMismatch(donation.getAccountIdBeneficiary()); // resolver arriba
            verify(accountRepository, times(1)).findById("348ad942-10aa-42b8-8173-a763c8d9b7e3");
            verify(donationMapper, times(1)).requestConfirmationDtoToDonation(any(RequestDonationConfirmationDto.class));
            verify(generalTransactions).updateBalances(
                    donation.getAccountIdDonor(),
                    donation.getAccountIdBeneficiary(),
                    donation.getAmount());
            verify(donationRepository).save(any(Donation.class));
            verify(donationMapper).donationToResponseConfirmationDto(any(Donation.class));


        }
    }
}