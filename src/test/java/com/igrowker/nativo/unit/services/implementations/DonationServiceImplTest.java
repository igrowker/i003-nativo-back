package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
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
                345347333L, true);
        requestDonationDtoFalse = new RequestDonationDto(BigDecimal.valueOf(100.0),
                345347333L, false);
        donation = new Donation("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                BigDecimal.valueOf(100.0), TransactionStatus.PENDING, "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                "a63a054d-fbc4-44f4-beaa-084b2c0e0192", true, LocalDateTime.now(), LocalDateTime.now());
        responseDonationDtoTrue = new ResponseDonationDtoTrue("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                BigDecimal.valueOf(100.0), "Ulises", "Gadea", 345347333L,
                "Mario", "Grande", LocalDateTime.now(), "PENDING");
        responseDonationDtoFalse = new ResponseDonationDtoFalse("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                BigDecimal.valueOf(100.0), 345347333L, "Mario",
                "Grande", LocalDateTime.now(), "PENDING");

        accountDonor = new Account("f89de776-3e64-42c0-b880-8d9e1f5697c8", 345347343L, BigDecimal.valueOf(1000.0), true, "donorUserId", BigDecimal.ZERO);
        accountBeneficiary = new Account("a63a054d-fbc4-44f4-beaa-084b2c0e0192", 345347333L, BigDecimal.valueOf(500.0), true, "beneficiaryUserId", BigDecimal.ZERO);
        userDonor = new User("donorUserId", 345347343L, "Ulises", "Gadea", "ulises@gmail.com", "password123", "987654321", "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", LocalDate.of(1995, 3, 22), LocalDateTime.now(), true, null, null, true, true, true);
        userBeneficiary = new User("beneficiaryUserId", 345347333L, "Mario", "Grande", "mario@gmail.com", "password123", "123456789", "348ad942-10aa-42b8-8173-a763c8d9b7e3", LocalDate.of(1990, 5, 15), LocalDateTime.now(), true, null, null, true, true, true);

     /*   requestDonationDtoResourceNotFoundException = new RequestDonationDto(BigDecimal.valueOf(100.0),
                "invalid-donor-account-id",
                "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                true);*/
    }


    @Nested
    class CreateDonationTrueTest {

        @Test
        public void create_donation_true_should_be_Ok() throws Exception {

            // Mockeando el comportamiento de los repositorios y validaciones
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(accountBeneficiary.getAccountNumber())).thenReturn(Optional.of(accountBeneficiary));
            when(userRepository.findById("beneficiaryUserId")).thenReturn(Optional.of(userBeneficiary));
            when(donationMapper.requestDtoToDonation(any(RequestDonationDto.class))).thenReturn(donation);
            donation.setAccountIdDonor(accountDonor.getId());
            donation.setAccountIdBeneficiary(userBeneficiary.getId());
            when(donationRepository.save(any(Donation.class))).thenReturn(donation);
            when(accountRepository.save(any(Account.class))).thenReturn(accountDonor);


            // Ejecutando el servicio
            ResponseDonationDtoTrue res = donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(donation.getId());
            assertThat(res.amount()).isEqualTo(donation.getAmount());
            assertThat(res.donorName()).isEqualTo(userDonor.getName());
            assertThat(res.donorLastName()).isEqualTo(userDonor.getSurname());
            assertThat(res.beneficiaryAccountNumber()).isEqualTo(accountBeneficiary.getAccountNumber());
            assertThat(res.beneficiaryName()).isEqualTo(userBeneficiary.getName());
            assertThat(res.beneficiaryLastName()).isEqualTo(userBeneficiary.getSurname());
            assertThat(res.createdAt()).isEqualTo(donation.getCreatedAt());
            assertThat(res.status()).isEqualTo(donation.getStatus().name());

            // Verificando que se llama a los métodos correctos
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).validateTransactionUserFunds(requestDonationDtoTrue.amount());
            verify(accountRepository, times(1)).findAccountByNumberAccount(requestDonationDtoTrue.numberAccountBeneficiary());
            verify(userRepository, times(1)).findById(accountBeneficiary.getUserId());
            verify(donationRepository).save(any(Donation.class));
            verify(accountRepository).save(accountDonor);


        }

        @Test
        public void create_donation_true_ResourceNotFoundException() throws Exception {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    345347333L, true);

            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("La cuenta indicada no coincide con el usuario logueado en la aplicación"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDto);
            });
            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_InsufficientFundsException() {

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            Exception exception = assertThrows(InsufficientFundsException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
            });

            String expectedMessage = "Tu cuenta no tiene suficientes fondos";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_ResourceNotFoundException_numberAccountBeneficiary() {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    999999999L, true);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(requestDonationDto.numberAccountBeneficiary()))
                    .thenReturn(Optional.empty());

            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDto);
            });

            String expectedMessage = "El numero de cuenta beneficiario no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_ResourceNotFoundException_idBeneficiaryUser() {


            accountBeneficiary.setUserId("invalid-beneficiary-user-id");
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(accountBeneficiary.getAccountNumber())).thenReturn(Optional.of(accountBeneficiary));
            when(userRepository.findById(accountBeneficiary.getUserId()))
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
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(accountBeneficiary.getAccountNumber())).thenReturn(Optional.of(accountBeneficiary));
            when(userRepository.findById("beneficiaryUserId")).thenReturn(Optional.of(userBeneficiary));
            when(donationMapper.requestDtoToDonation(any(RequestDonationDto.class))).thenReturn(donation);
            donation.setAccountIdDonor(accountDonor.getId());
            donation.setAccountIdBeneficiary(userBeneficiary.getId());
            when(donationRepository.save(any(Donation.class))).thenReturn(donation);
            when(accountRepository.save(any(Account.class))).thenReturn(accountDonor);

            // Simulando la conversión de DTOs
            when(donationMapper.donationToResponseDtoFalse(eq(donation), eq(userBeneficiary.getName()), eq(userBeneficiary.getSurname()), eq(accountBeneficiary.getAccountNumber()))).thenReturn(responseDonationDtoFalse);

            // Ejecutando el servicio
            ResponseDonationDtoFalse res = donationServiceImpl.createDonationFalse(requestDonationDtoFalse);

            // Verificaciones
            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(donation.getId());
            assertThat(res.amount()).isEqualTo(donation.getAmount());
            assertThat(res.beneficiaryAccountNumber()).isEqualTo(accountBeneficiary.getAccountNumber());
            assertThat(res.beneficiaryName()).isEqualTo(userBeneficiary.getName());
            assertThat(res.beneficiaryLastName()).isEqualTo(userBeneficiary.getSurname());

            // Verificando que se llamaron los métodos correctos
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).validateTransactionUserFunds(requestDonationDtoFalse.amount());
            verify(accountRepository, times(1)).findAccountByNumberAccount(requestDonationDtoFalse.numberAccountBeneficiary());
            verify(userRepository, times(1)).findById(accountBeneficiary.getUserId());
            verify(donationRepository).save(any(Donation.class));
            verify(accountRepository).save(accountDonor);
        }

        @Test
        public void create_donation_false_ResourceNotFoundException() throws Exception {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    345347333L, false);

            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("La cuenta indicada no coincide con el usuario logueado en la aplicación"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationFalse(requestDonationDto);
            });
            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }


        @Test
        public void create_donation_false_InsufficientFundsException() {

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            Exception exception = assertThrows(InsufficientFundsException.class, () -> {
                donationServiceImpl.createDonationFalse(requestDonationDtoTrue);
            });

            String expectedMessage = "Tu cuenta no tiene suficientes fondos";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_false_ResourceNotFoundException_numberAccountBeneficiary() {
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    999999999L, false);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(requestDonationDto.numberAccountBeneficiary()))
                    .thenReturn(Optional.empty());

            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationFalse(requestDonationDto);
            });

            String expectedMessage = "El numero de cuenta beneficiario no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_false_ResourceNotFoundException_idBeneficiaryUser() {


            accountBeneficiary.setUserId("invalid-beneficiary-user-id");
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(accountBeneficiary.getAccountNumber())).thenReturn(Optional.of(accountBeneficiary));
            when(userRepository.findById(accountBeneficiary.getUserId()))
                    .thenReturn(Optional.empty());


            Exception exception= assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationFalse(requestDonationDtoFalse);
            });

            String expectedMessage = "El id de usuario beneficiario no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class ConfirmationDonationTest {

        @Test
        public void confirmation_ACCEPTED_donation_should_be_Ok() throws Exception {
            var requestDonationConfirmationDto = new RequestDonationConfirmationDto(
                    "c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    TransactionStatus.ACCEPTED);

            var responseDonationConfirmationDto = new ResponseDonationConfirmationDto(
                    "c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    BigDecimal.valueOf(100.0),
                    "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192",
                    TransactionStatus.ACCEPTED);
            var donation = new Donation("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    BigDecimal.valueOf(100.0), TransactionStatus.ACCEPTED, "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192", true, LocalDateTime.now(), LocalDateTime.now());


            // Mockeando el comportamiento de los repositorios y servicios
            when(donationRepository.findById(requestDonationConfirmationDto.id())).thenReturn(Optional.of(donation));
            when(validations.isUserAccountMismatch(donation.getAccountIdBeneficiary())).thenReturn(false);
            when(donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto)).thenReturn(donation);
            when(accountRepository.findById("f89de776-3e64-42c0-b880-8d9e1f5697c8")).thenReturn(Optional.of(accountDonor));

            // Mock para returnAmount
            doNothing().when(donationServiceImplTest).returnAmount(donation.getAccountIdDonor(), donation.getAmount());

            // Simulación de updateBalances
            doNothing().when(generalTransactions).updateBalances(
                    donation.getAccountIdDonor(),
                    donation.getAccountIdBeneficiary(),
                    donation.getAmount());

            // Mock para save
            when(donationRepository.save(any(Donation.class))).thenReturn(donation);

            // Mock para mapper
            when(donationMapper.donationToResponseConfirmationDto(any(Donation.class))).thenReturn(responseDonationConfirmationDto);

            // Ejecutando el servicio
            ResponseDonationConfirmationDto res = donationServiceImpl.confirmationDonation(requestDonationConfirmationDto);

            // Aserciones
            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(responseDonationConfirmationDto.id());
            assertThat(res.amount()).isEqualTo(responseDonationConfirmationDto.amount());
            assertThat(res.accountIdDonor()).isEqualTo(responseDonationConfirmationDto.accountIdDonor());
            assertThat(res.accountIdBeneficiary()).isEqualTo(responseDonationConfirmationDto.accountIdBeneficiary());
            assertThat(res.status()).isEqualTo(responseDonationConfirmationDto.status());

            // Verificando las interacciones
            verify(donationRepository, times(1)).findById(requestDonationConfirmationDto.id());
            verify(validations, times(1)).isUserAccountMismatch(donation.getAccountIdBeneficiary());
            verify(donationMapper, times(1)).requestConfirmationDtoToDonation(requestDonationConfirmationDto);
            verify(donationServiceImplTest, times(1)).returnAmount(donation.getAccountIdDonor(), donation.getAmount());
            verify(generalTransactions, times(1)).updateBalances(
                    donation.getAccountIdDonor(),
                    donation.getAccountIdBeneficiary(),
                    donation.getAmount());
            verify(donationRepository, times(1)).save(any(Donation.class));
            verify(donationMapper, times(1)).donationToResponseConfirmationDto(donation);
        }

    }

}


