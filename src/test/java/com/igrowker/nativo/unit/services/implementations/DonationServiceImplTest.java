package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.InvalidDataException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.DonationMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.implementation.DonationServiceImpl;
import com.igrowker.nativo.utils.DateFormatter;
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
import java.util.*;

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
    private DateFormatter dateFormatter;
    @InjectMocks
    private DonationServiceImpl donationServiceImpl;

    //esto es para el beforeeach
    private Donation donation;
    private RequestDonationDto requestDonationDtoTrue;
    private RequestDonationDto requestDonationDtoFalse;
    private RequestDonationConfirmationDto requestDonationConfirmationDtoAccepted;
    private RequestDonationConfirmationDto requestDonationConfirmationDtoDenied;
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
        requestDonationConfirmationDtoAccepted = new RequestDonationConfirmationDto(
                "c12e32e4-0e27-438d-8861-cb1aaa619f56",
                TransactionStatus.ACCEPTED);
        requestDonationConfirmationDtoDenied = new RequestDonationConfirmationDto(
                "c12e32e4-0e27-438d-8861-cb1aaa619f56",
                TransactionStatus.DENIED);
        donation = new Donation("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                BigDecimal.valueOf(100.0), TransactionStatus.PENDING, "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                "a63a054d-fbc4-44f4-beaa-084b2c0e0192", true, LocalDateTime.now(), LocalDateTime.now());
        responseDonationDtoFalse = new ResponseDonationDtoFalse("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                BigDecimal.valueOf(100.0), 345347333L, "Mario",
                "Grande", LocalDateTime.now(), "PENDING");

        accountDonor = new Account("f89de776-3e64-42c0-b880-8d9e1f5697c8", 345347343L, BigDecimal.valueOf(1000.0), true, "donorUserId", BigDecimal.ZERO);
        accountBeneficiary = new Account("a63a054d-fbc4-44f4-beaa-084b2c0e0192", 345347333L, BigDecimal.valueOf(500.0), true, "beneficiaryUserId", BigDecimal.ZERO);
        userDonor = new User("donorUserId", 345347343L, "Ulises", "Gadea", "ulises@gmail.com", "password123", "987654321", "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c", LocalDate.of(1995, 3, 22), LocalDateTime.now(), true, null, null, true, true, true);
        userBeneficiary = new User("beneficiaryUserId", 345347333L, "Mario", "Grande", "mario@gmail.com", "password123", "123456789", "348ad942-10aa-42b8-8173-a763c8d9b7e3", LocalDate.of(1990, 5, 15), LocalDateTime.now(), true, null, null, true, true, true);

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

            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("La cuenta indicada no coincide con el usuario logueado en la aplicación"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
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
        public void create_donation_true_IllegalArgumentException_numberAccount() {
            var requestDonationDtoTrue = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    accountDonor.getAccountNumber(), true);  // El beneficiario es el mismo que el donador

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(accountDonor.getAccountNumber()))
                    .thenReturn(Optional.of(accountDonor));  // Beneficiario y donador son la misma cuenta

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                donationServiceImpl.createDonationTrue(requestDonationDtoTrue);
            });

            String expectedMessage = "No puedes donarte a ti mismo";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void create_donation_true_ResourceNotFoundException_idBeneficiaryUser() {

            accountBeneficiary.setUserId("invalid-beneficiary-user-id");  // Usuario inválido

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(accountBeneficiary.getAccountNumber()))
                    .thenReturn(Optional.of(accountBeneficiary));  // Cuenta beneficiario encontrada
            when(userRepository.findById(accountBeneficiary.getUserId()))
                    .thenReturn(Optional.empty());  // Usuario no encontrado

            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
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
        public void create_donation_false_IllegalArgumentException_numberAccount() {
            var requestDonationDtoFalse = new RequestDonationDto(BigDecimal.valueOf(100.0),
                    accountDonor.getAccountNumber(), false);  // El beneficiario es el mismo que el donador

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.validateTransactionUserFunds(any())).thenReturn(true);
            when(accountRepository.findAccountByNumberAccount(accountDonor.getAccountNumber()))
                    .thenReturn(Optional.of(accountDonor));  // Beneficiario y donador son la misma cuenta

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                donationServiceImpl.createDonationFalse(requestDonationDtoFalse);
            });

            String expectedMessage = "No puedes donarte a ti mismo";
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
    class ConfirmationAcceptedDonationTest {

        @Test
        public void confirmation_ACCEPTED_donation_should_be_Ok() throws Exception {

            var responseDonationConfirmationDto = new ResponseDonationConfirmationDto(
                    "c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    BigDecimal.valueOf(100.0),
                    "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192",
                    TransactionStatus.ACCEPTED);

            var donation1 = new Donation("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    BigDecimal.valueOf(100.0),
                    TransactionStatus.ACCEPTED, // Estado actualizado a ACCEPTED
                    "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192",
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now());

            // Mockeando el comportamiento de los repositorios y servicios
            when(donationRepository.findById(requestDonationConfirmationDtoDenied.id()))
                    .thenReturn(Optional.of(donation));
            when(validations.isUserAccountMismatch(donation.getAccountIdBeneficiary()))
                    .thenReturn(false);
            when(donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDtoDenied))
                    .thenReturn(donation1);
            lenient().when(accountRepository.findById(donation1.getAccountIdDonor()))
                    .thenReturn(Optional.of(accountDonor)); // Usamos lenient() ya que puede no ser necesario
            doNothing().when(generalTransactions).updateBalances(
                    donation1.getAccountIdDonor(),
                    donation1.getAccountIdBeneficiary(),
                    donation1.getAmount());
            when(donationRepository.save(any(Donation.class)))
                    .thenReturn(donation1);
            when(donationMapper.donationToResponseConfirmationDto(donation1))
                    .thenReturn(responseDonationConfirmationDto);


            ResponseDonationConfirmationDto res = donationServiceImpl.confirmationDonation(requestDonationConfirmationDtoDenied);

            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(responseDonationConfirmationDto.id());
            assertThat(res.amount()).isEqualTo(responseDonationConfirmationDto.amount());
            assertThat(res.accountIdDonor()).isEqualTo(responseDonationConfirmationDto.accountIdDonor());
            assertThat(res.accountIdBeneficiary()).isEqualTo(responseDonationConfirmationDto.accountIdBeneficiary());
            assertThat(res.status()).isEqualTo(responseDonationConfirmationDto.status());


            verify(donationRepository, times(1)).findById(requestDonationConfirmationDtoDenied.id());
            verify(validations, times(1)).isUserAccountMismatch(donation.getAccountIdBeneficiary());
            verify(donationMapper, times(1)).requestConfirmationDtoToDonation(requestDonationConfirmationDtoDenied);
            verify(generalTransactions, times(1)).updateBalances(
                    donation1.getAccountIdDonor(),
                    donation1.getAccountIdBeneficiary(),
                    donation1.getAmount());
            verify(donationRepository, times(1)).save(any(Donation.class));
            verify(donationMapper, times(1)).donationToResponseConfirmationDto(donation1);
        }

        @Test
        public void confirmation_donation_ACCEPTED_ResourceNotFoundException_idDonation() throws Exception {
            // POR QUE FUNCIONA
            var requestDonationConfirmationDtoAccepted = new RequestDonationConfirmationDto(
                    "invalid-donation-id",
                    TransactionStatus.ACCEPTED);
            when(donationRepository.findById(requestDonationConfirmationDtoAccepted.id()))
                    .thenReturn(Optional.empty());
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.confirmationDonation(requestDonationConfirmationDtoAccepted);
            });
            String expectedMessage = "El id de la donacion no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void confirmation_donation_ACCEPTED_InvalidUserCredentialsException() {

            when(donationRepository.findById(requestDonationConfirmationDtoAccepted.id())).thenReturn(Optional.of(donation));
            when(validations.isUserAccountMismatch(any())).thenReturn(true);
            Exception exception = assertThrows(InvalidUserCredentialsException.class, () -> {
                donationServiceImpl.confirmationDonation(requestDonationConfirmationDtoAccepted);
            });

            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

    }

    @Nested
    class ConfirmationDeniedDonationTest {
        @Test
        public void confirmation_DENIED_donation_should_be_Ok() throws Exception {
            // Datos de prueba
            var requestDonationConfirmationDto = new RequestDonationConfirmationDto(
                    "c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    TransactionStatus.DENIED);

            var responseDonationConfirmationDto = new ResponseDonationConfirmationDto(
                    "c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    BigDecimal.valueOf(100.0),
                    "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192",
                    TransactionStatus.DENIED);

            var donation1 = new Donation("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    BigDecimal.valueOf(100.0),
                    TransactionStatus.DENIED, // Estado actualizado a ACCEPTED
                    "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192",
                    false,
                    LocalDateTime.now(),
                    LocalDateTime.now());

            // Mockeando el comportamiento de los repositorios y servicios
            when(donationRepository.findById(requestDonationConfirmationDto.id()))
                    .thenReturn(Optional.of(donation));
            when(validations.isUserAccountMismatch(donation.getAccountIdBeneficiary()))
                    .thenReturn(false);
            when(donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto))
                    .thenReturn(donation1);
            lenient().when(accountRepository.findById(donation1.getAccountIdDonor()))
                    .thenReturn(Optional.of(accountDonor)); // Usamos lenient() ya que puede no ser necesario
            when(donationRepository.save(any(Donation.class)))
                    .thenReturn(donation1);
            when(donationMapper.donationToResponseConfirmationDto(donation1))
                    .thenReturn(responseDonationConfirmationDto);


            ResponseDonationConfirmationDto res = donationServiceImpl.confirmationDonation(requestDonationConfirmationDto);

            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(responseDonationConfirmationDto.id());
            assertThat(res.amount()).isEqualTo(responseDonationConfirmationDto.amount());
            assertThat(res.accountIdDonor()).isEqualTo(responseDonationConfirmationDto.accountIdDonor());
            assertThat(res.accountIdBeneficiary()).isEqualTo(responseDonationConfirmationDto.accountIdBeneficiary());
            assertThat(res.status()).isEqualTo(responseDonationConfirmationDto.status());


            verify(donationRepository, times(1)).findById(requestDonationConfirmationDto.id());
            verify(validations, times(1)).isUserAccountMismatch(donation.getAccountIdBeneficiary());
            verify(donationMapper, times(1)).requestConfirmationDtoToDonation(requestDonationConfirmationDto);
            verify(donationRepository, times(1)).save(any(Donation.class));
            verify(donationMapper, times(1)).donationToResponseConfirmationDto(donation1);
        }

        @Test
        public void confirmation_donation_DENIED_ResourceNotFoundException_idDonation() throws Exception {
            // POR QUE FUNCIONA
            var requestDonationConfirmationDtoDenied = new RequestDonationConfirmationDto(
                    "invalid-donation-id",
                    TransactionStatus.DENIED);
            when(donationRepository.findById(requestDonationConfirmationDtoDenied.id()))
                    .thenReturn(Optional.empty());
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.confirmationDonation(requestDonationConfirmationDtoDenied);
            });
            String expectedMessage = "El id de la donacion no existe";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void confirmation_donation_DENIED_InvalidUserCredentialsException() {

            when(donationRepository.findById(requestDonationConfirmationDtoDenied.id())).thenReturn(Optional.of(donation));
            when(validations.isUserAccountMismatch(any())).thenReturn(true);
            Exception exception = assertThrows(InvalidUserCredentialsException.class, () -> {
                donationServiceImpl.confirmationDonation(requestDonationConfirmationDtoDenied);
            });

            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }


    @Nested
    class GetDonationBtBetweenDatesOrStatus {

        @Test
        public void get_donations_by_status_should_be_Ok() throws Exception {

            // Crear una donación de prueba
            Donation donation = new Donation("c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    BigDecimal.valueOf(100.0), TransactionStatus.ACCEPTED, "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192", true, LocalDateTime.now(), LocalDateTime.now());

            List<Donation> donationList = new ArrayList<>();
            donationList.add(donation);

            // Crear una respuesta de prueba para la donación
            var responseDonationRecord = new ResponseDonationRecord(donation.getId(), BigDecimal.valueOf(100.0), userDonor.getName(),
                    userDonor.getUsername(),accountDonor.getId(),userBeneficiary.getName(),userBeneficiary.getSurname(),
                    accountBeneficiary.getId(),TransactionStatus.ACCEPTED,donation.getCreatedAt(),
                    donation.getUpdateAt());

            List<ResponseDonationRecord> responseList = new ArrayList<>();
            responseList.add(responseDonationRecord);

            // Simular el comportamiento de los métodos utilizados en el servicio
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(validations.statusConvert(anyString())).thenReturn(TransactionStatus.ACCEPTED);
            when(donationRepository.findDonationsByStatus(any(), any())).thenReturn(donationList);
            when(donationMapper.listDonationToListResponseDonationRecordTwo(donationList)).thenReturn(responseList);

            // Ejecutar el metodo a probar
            var result = donationServiceImpl.getDonationBtBetweenDatesOrStatus(null, null, TransactionStatus.ACCEPTED.toString());

            // Verificar el resultado
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(responseDonationRecord.id());
            assertThat(result.get(0).amount()).isEqualTo(responseDonationRecord.amount());
            assertThat(result.get(0).donorName()).isEqualTo(responseDonationRecord.donorName());
            assertThat(result.get(0).donorLastName()).isEqualTo(responseDonationRecord.donorLastName());
            assertThat(result.get(0).accountIdDonor()).isEqualTo(responseDonationRecord.accountIdDonor());
            assertThat(result.get(0).beneficiaryName()).isEqualTo(responseDonationRecord.beneficiaryName());
            assertThat(result.get(0).beneficiaryLastName()).isEqualTo(responseDonationRecord.beneficiaryLastName());
            assertThat(result.get(0).accountIdBeneficiary()).isEqualTo(responseDonationRecord.accountIdBeneficiary());
            assertThat(result.get(0).status()).isEqualTo(responseDonationRecord.status());
            assertThat(result.get(0).createdAt()).isEqualTo(responseDonationRecord.createdAt());
            assertThat(result.get(0).updateAt()).isEqualTo(responseDonationRecord.updateAt());

            // Verificar que se llamaron los métodos correspondientes
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(anyString());
            verify(donationRepository, times(1)).findDonationsByStatus(any(), any());
            verify(donationMapper, times(1)).listDonationToListResponseDonationRecordTwo(any());
        }


        @Test
        public void get_donations_by_date_range_should_be_Ok() throws Exception {

            // Crear una donación de prueba
            Donation donation = new Donation(
                    "c12e32e4-0e27-438d-8861-cb1aaa619f56",
                    BigDecimal.valueOf(100.0),
                    TransactionStatus.ACCEPTED,
                    "f89de776-3e64-42c0-b880-8d9e1f5697c8",
                    "a63a054d-fbc4-44f4-beaa-084b2c0e0192",
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            List<Donation> donationList = new ArrayList<>();
            donationList.add(donation);

            // Crear una respuesta de prueba para la donación
            ResponseDonationRecord responseDonationRecord = new ResponseDonationRecord(
                    donation.getId(),
                    BigDecimal.valueOf(100.0),
                    userDonor.getName(),
                    userDonor.getUsername(),
                    accountDonor.getId(),
                    userBeneficiary.getName(),
                    userBeneficiary.getSurname(),
                    accountBeneficiary.getId(),
                    TransactionStatus.ACCEPTED,
                    donation.getCreatedAt(),
                    donation.getUpdateAt()
            );

            List<ResponseDonationRecord> responseList = new ArrayList<>();
            responseList.add(responseDonationRecord);

            // Simular el retorno de la lista de fechas
            List<LocalDateTime> dateTimes = Arrays.asList(
                    LocalDateTime.of(2023, 4, 12, 0, 0),
                    LocalDateTime.of(2023, 4, 12, 23, 59)
            );

            // Simular el comportamiento de los métodos utilizados en el servicio
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            when(dateFormatter.getDateFromString("2023-04-12", "2023-04-12")).thenReturn(dateTimes);
            when(donationRepository.findDonationsByDateRange(any(), any(), any())).thenReturn(donationList);
            when(donationMapper.listDonationToListResponseDonationRecordTwo(donationList)).thenReturn(responseList);

            // Ejecutar el método a probar
            var result = donationServiceImpl.getDonationBtBetweenDatesOrStatus("2023-04-12", "2023-04-12", null);

            // Verificar el resultado
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(responseDonationRecord.id());
            assertThat(result.get(0).amount()).isEqualTo(responseDonationRecord.amount());
            assertThat(result.get(0).donorName()).isEqualTo(responseDonationRecord.donorName());
            assertThat(result.get(0).donorLastName()).isEqualTo(responseDonationRecord.donorLastName());
            assertThat(result.get(0).accountIdDonor()).isEqualTo(responseDonationRecord.accountIdDonor());
            assertThat(result.get(0).beneficiaryName()).isEqualTo(responseDonationRecord.beneficiaryName());
            assertThat(result.get(0).beneficiaryLastName()).isEqualTo(responseDonationRecord.beneficiaryLastName());
            assertThat(result.get(0).accountIdBeneficiary()).isEqualTo(responseDonationRecord.accountIdBeneficiary());
            assertThat(result.get(0).status()).isEqualTo(responseDonationRecord.status());
            assertThat(result.get(0).createdAt()).isEqualTo(responseDonationRecord.createdAt());
            assertThat(result.get(0).updateAt()).isEqualTo(responseDonationRecord.updateAt());

            // Verificar que se llamaron los métodos correspondientes
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(dateFormatter, times(1)).getDateFromString(anyString(), anyString());
            verify(donationRepository, times(1)).findDonationsByDateRange(any(), any(), any());
            verify(donationMapper, times(1)).listDonationToListResponseDonationRecordTwo(any());
        }

        @Test
        public void get_donations_by_status_ResourceNotFoundException_second() throws Exception {

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.getDonationBtBetweenDatesOrStatus(null, null, null);
            });
            String expectedMessage = "Se debe de ingresar las fechas de inicio y fin o un status";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }


        @Test
        public void get_donations_ResourceNotFoundException_one() throws Exception {

            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("La cuenta indicada no coincide con el usuario logueado en la aplicación"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.getDonationBtBetweenDatesOrStatus("2023-04-12", "2023-04-12", null);
            });
            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }


        @Test
        public void get_donations_ResourceNotFoundException_second() throws Exception {

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(userDonor, accountDonor));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                donationServiceImpl.getDonationBtBetweenDatesOrStatus(null, null, null);
            });
            String expectedMessage = "Se debe de ingresar las fechas de inicio y fin o un status";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }



    }
}


