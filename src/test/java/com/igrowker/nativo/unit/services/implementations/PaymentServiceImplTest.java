package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.*;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.implementation.PaymentServiceImpl;
import com.igrowker.nativo.services.implementation.QRService;
import com.igrowker.nativo.utils.DateFormatter;
import com.igrowker.nativo.utils.GeneralTransactions;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private Validations validations;
    @Mock
    private DateFormatter dateFormatter;
    @Mock
    private QRService qrService;
    @Mock
    private GeneralTransactions transactions;
    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    @Nested
    class CreateQrTests {
        @Test
        public void create_qr_should_be_Ok() throws Exception {
            var paymentRequestDto = new RequestPaymentDto("receiverId", BigDecimal.valueOf(100.50), "description");
            var payment = new Payment("paymentId", "senderName", "senderSurname", "senderId",
                    "receiverName", "receiverSurname", "receiverId", BigDecimal.valueOf(100.50), LocalDateTime.now(), TransactionStatus.PENDING, "description", "long-long-long-qr");
            var paymentResponseDto = new ResponsePaymentDto("paymentId", "receiverAccount",
                    "receiverName", "receiverSurname", BigDecimal.valueOf(100.50), "description", "long-long-long-qr");
            Account testAccount = new Account();
            testAccount.setAccountNumber(123456789l);

            when(paymentMapper.requestDtoToPayment(any())).thenReturn(payment);
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(new Validations.UserAccountPair(new User(), testAccount));
            when(paymentRepository.save(any())).thenReturn(payment);
            when(qrService.generateQrCode(any())).thenReturn("long-long-long-qr");
            when(paymentMapper.paymentToResponseDto(any(), anyString())).thenReturn(paymentResponseDto);
            var res = paymentServiceImpl.createQr(paymentRequestDto);

            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(paymentResponseDto.id());
            assertThat(res.receiverAccount()).isEqualTo(paymentResponseDto.receiverAccount());
            assertThat(res.description()).isEqualTo(paymentResponseDto.description());
            assertThat(res.amount()).isEqualTo(paymentResponseDto.amount());
            assertThat(res.qr()).isEqualTo(paymentResponseDto.qr());
            verify(paymentRepository, times(2)).save(any());
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(qrService, times(1)).generateQrCode(any());
            verify(paymentMapper, times(1)).requestDtoToPayment(any());
            verify(paymentMapper, times(1)).paymentToResponseDto(any(), anyString());
        }

        @Test
        public void create_qr_should_NOT_be_Ok() throws Exception {
            var paymentRequestDto = new RequestPaymentDto("receiverId", BigDecimal.valueOf(100.50), "description");

            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("La cuenta indicada no coincide con el usuario logueado en la aplicación"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.createQr(paymentRequestDto);
            });
            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class ProcessPaymentTest{

        @Test
        public void process_payment_should_be_ok_due_accepted() throws Exception {
            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");

            Payment initialPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now(), TransactionStatus.PENDING, "Transaction Description", "QRcode");

            Payment finalPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now(), TransactionStatus.ACCEPTED, "Transaction Description", "QRcode");

            ResponseProcessPaymentDto responseProcessPaymentDto = new ResponseProcessPaymentDto("TransactionID","Sender Name",
                    "SenderSurname", "123456789L", "ReceiverName",
                    "ReceiverSurname","987654321L",
                    BigDecimal.valueOf(20.00), TransactionStatus.ACCEPTED, LocalDateTime.now());

            Account senderAccount = new Account("AccountID", 123456789L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String senderAccountNumber = senderAccount.getAccountNumber().toString();

            Account receiverAccount = new Account("AccountID", 987654321L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String receiverAccountNumber = receiverAccount.getAccountNumber().toString();

            var userAccountPair = new Validations.UserAccountPair(new User(), senderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenReturn(initialPayment.getTransactionStatus());
            when(paymentMapper.requestProcessDtoToPayment(any())).thenReturn(initialPayment);
            when(paymentRepository.findById(any())).thenReturn(Optional.of(initialPayment));
            when(accountRepository.findById(initialPayment.getReceiverAccount())).thenReturn(Optional.of(receiverAccount));
            when(paymentRepository.save(any())).thenReturn(finalPayment);
            when(validations.validateTransactionUserFunds(finalPayment.getAmount())).thenReturn(true);
            doNothing().when(transactions).updateBalances(any(), any(), any());
            when(paymentMapper.paymentToResponseProcessDto(finalPayment, senderAccountNumber, receiverAccountNumber)).thenReturn(responseProcessPaymentDto);

            ResponseProcessPaymentDto result = paymentServiceImpl.processPayment(requestProcessPaymentDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(responseProcessPaymentDto.id());
            assertThat(result.senderName()).isEqualTo(responseProcessPaymentDto.senderName());
            assertThat(result.senderSurname()).isEqualTo(responseProcessPaymentDto.senderSurname());
            assertThat(result.senderAccount()).isEqualTo(responseProcessPaymentDto.senderAccount());
            assertThat(result.receiverName()).isEqualTo(responseProcessPaymentDto.receiverName());
            assertThat(result.receiverSurname()).isEqualTo(responseProcessPaymentDto.receiverSurname());
            assertThat(result.receiverAccount()).isEqualTo(responseProcessPaymentDto.receiverAccount());
            assertThat(result.amount()).isEqualTo(responseProcessPaymentDto.amount());
            assertThat(result.transactionStatus()).isEqualTo(responseProcessPaymentDto.transactionStatus());
            assertThat(result.transactionDate()).isEqualTo(responseProcessPaymentDto.transactionDate());

            // Verificamos que se llamaron los métodos correctos el número de veces esperado.
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(any());
            verify(paymentMapper, times(1)).requestProcessDtoToPayment(any());
            verify(paymentRepository, times(1)).findById(any());
            verify(accountRepository, times(1)).findById(any());
            verify(paymentRepository, times(2)).save(any());
            verify(validations, times(1)).validateTransactionUserFunds(any());
            verify(transactions, times(1)).updateBalances(finalPayment.getSenderAccount(), finalPayment.getReceiverAccount(), finalPayment.getAmount());
            verify(paymentMapper, times(1)).paymentToResponseProcessDto(any(), any(), any());
        }

        @Test
        public void process_payment_should_be_ok_due_denied() throws Exception{
            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");

            Payment initialPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now(), TransactionStatus.PENDING, "Transaction Description", "QRcode");

            Payment finalPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now(), TransactionStatus.DENIED, "Transaction Description", "QRcode");

            ResponseProcessPaymentDto responseProcessPaymentDto = new ResponseProcessPaymentDto("TransactionID","Sender Name",
                    "SenderSurname", "123456789L", "ReceiverName",
                    "ReceiverSurname","987654321L",
                    BigDecimal.valueOf(20.00), TransactionStatus.DENIED, LocalDateTime.now());

            Account senderAccount = new Account("AccountID", 123456789L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String senderAccountNumber = senderAccount.getAccountNumber().toString();

            Account receiverAccount = new Account("AccountID", 987654321L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String receiverAccountNumber = receiverAccount.getAccountNumber().toString();

            var userAccountPair = new Validations.UserAccountPair(new User(), senderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenReturn(initialPayment.getTransactionStatus());
            when(paymentMapper.requestProcessDtoToPayment(any())).thenReturn(initialPayment);
            when(paymentRepository.findById(any())).thenReturn(Optional.of(initialPayment));
            when(accountRepository.findById(initialPayment.getReceiverAccount())).thenReturn(Optional.of(receiverAccount));
            when(paymentRepository.save(any())).thenReturn(finalPayment);
            when(paymentMapper.paymentToResponseProcessDto(finalPayment, senderAccountNumber, receiverAccountNumber)).thenReturn(responseProcessPaymentDto);

            ResponseProcessPaymentDto result = paymentServiceImpl.processPayment(requestProcessPaymentDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(responseProcessPaymentDto.id());
            assertThat(result.senderName()).isEqualTo(responseProcessPaymentDto.senderName());
            assertThat(result.senderSurname()).isEqualTo(responseProcessPaymentDto.senderSurname());
            assertThat(result.senderAccount()).isEqualTo(responseProcessPaymentDto.senderAccount());
            assertThat(result.receiverName()).isEqualTo(responseProcessPaymentDto.receiverName());
            assertThat(result.receiverSurname()).isEqualTo(responseProcessPaymentDto.receiverSurname());
            assertThat(result.receiverAccount()).isEqualTo(responseProcessPaymentDto.receiverAccount());
            assertThat(result.amount()).isEqualTo(responseProcessPaymentDto.amount());
            assertThat(result.transactionStatus()).isEqualTo(responseProcessPaymentDto.transactionStatus());
            assertThat(result.transactionDate()).isEqualTo(responseProcessPaymentDto.transactionDate());

            // Verificamos que se llamaron los métodos correctos el número de veces esperado.
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(any());
            verify(paymentMapper, times(1)).requestProcessDtoToPayment(any());
            verify(paymentRepository, times(1)).findById(any());
            verify(accountRepository, times(1)).findById(any());
            verify(paymentRepository, times(2)).save(any());
            verify(paymentMapper, times(1)).paymentToResponseProcessDto(any(), any(), any());
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_credentials() throws Exception{
            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");

            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("La cuenta indicada no coincide con el usuario logueado en la aplicación"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.processPayment(requestProcessPaymentDto);
            });
            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_payment_not_found() throws Exception{
            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");
            Payment initialPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now(), TransactionStatus.PENDING, "Transaction Description", "QRcode");
            Account senderAccount = new Account("AccountID", 123456789L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String senderAccountNumber = senderAccount.getAccountNumber().toString();
            var userAccountPair = new Validations.UserAccountPair(new User(), senderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenReturn(initialPayment.getTransactionStatus());
            when(paymentMapper.requestProcessDtoToPayment(any())).thenReturn(initialPayment);

            when(paymentRepository.findById(any())).thenThrow(new ResourceNotFoundException("El Pago solicitado no fue encontrado"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.processPayment(requestProcessPaymentDto);
            });
            String expectedMessage = "El Pago solicitado no fue encontrado";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_already_processed_payment() throws Exception{
            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");

            Payment initialPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now(), TransactionStatus.ACCEPTED, "Transaction Description", "QRcode");
            Account senderAccount = new Account("AccountID", 123456789L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String senderAccountNumber = senderAccount.getAccountNumber().toString();

            Account receiverAccount = new Account("AccountID", 987654321L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String receiverAccountNumber = receiverAccount.getAccountNumber().toString();

            var userAccountPair = new Validations.UserAccountPair(new User(), senderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenReturn(initialPayment.getTransactionStatus());
            when(paymentMapper.requestProcessDtoToPayment(any())).thenReturn(initialPayment);
            when(paymentRepository.findById(any())).thenReturn(Optional.of(initialPayment));
            when(accountRepository.findById(initialPayment.getReceiverAccount())).thenReturn(Optional.of(receiverAccount));

            Exception exception = assertThrows(ExpiredTransactionException.class, () -> {
                paymentServiceImpl.processPayment(requestProcessPaymentDto);
            });

            // Comprobamos el mensaje de la excepción
            String expectedMessage = "El QR ya fue utilizado.";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));

            // Verificamos que los métodos correctos fueron llamados
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(paymentRepository, times(1)).findById(any());
            verify(accountRepository, times(1)).findById(any());
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_expired_payment() throws Exception{
            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");

            Payment initialPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now().minusMinutes(25), TransactionStatus.PENDING, "Transaction Description", "QRcode");
            Account senderAccount = new Account("AccountID", 123456789L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String senderAccountNumber = senderAccount.getAccountNumber().toString();

            Account receiverAccount = new Account("AccountID", 987654321L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String receiverAccountNumber = receiverAccount.getAccountNumber().toString();

            var userAccountPair = new Validations.UserAccountPair(new User(), senderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenReturn(initialPayment.getTransactionStatus());
            when(paymentMapper.requestProcessDtoToPayment(any())).thenReturn(initialPayment);
            when(paymentRepository.findById(any())).thenReturn(Optional.of(initialPayment));
            when(accountRepository.findById(initialPayment.getReceiverAccount())).thenReturn(Optional.of(receiverAccount));

            Exception exception = assertThrows(ExpiredTransactionException.class, () -> {
                paymentServiceImpl.processPayment(requestProcessPaymentDto);
            });

            String expectedMessage = "El QR no puede ser procesado por exceso en el limite de tiempo. Genere uno nuevo.";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(paymentRepository, times(1)).findById(any());
            verify(accountRepository, times(1)).findById(any());
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_insufficient_funds() throws Exception {
            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");

            Payment initialPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now(), TransactionStatus.PENDING, "Transaction Description", "QRcode");

            Payment finalPayment = new Payment("TransactionID", "senderName","SenderSurname", "123456789L",
                    "ReceiverName", "receiverSurname", "987654321L", BigDecimal.valueOf(20.00),
                    LocalDateTime.now(), TransactionStatus.ACCEPTED, "Transaction Description", "QRcode");

            ResponseProcessPaymentDto responseProcessPaymentDto = new ResponseProcessPaymentDto("TransactionID","Sender Name",
                    "SenderSurname", "123456789L", "ReceiverName",
                    "ReceiverSurname","987654321L",
                    BigDecimal.valueOf(20.00), TransactionStatus.ACCEPTED, LocalDateTime.now());

            Account senderAccount = new Account("AccountID", 123456789L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String senderAccountNumber = senderAccount.getAccountNumber().toString();

            Account receiverAccount = new Account("AccountID", 987654321L, BigDecimal.valueOf(200.00),
                    true, "UserID", BigDecimal.valueOf(10.00));
            String receiverAccountNumber = receiverAccount.getAccountNumber().toString();

            var userAccountPair = new Validations.UserAccountPair(new User(), senderAccount);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenReturn(initialPayment.getTransactionStatus());
            when(paymentMapper.requestProcessDtoToPayment(any())).thenReturn(initialPayment);
            when(paymentRepository.findById(any())).thenReturn(Optional.of(initialPayment));
            when(accountRepository.findById(initialPayment.getReceiverAccount())).thenReturn(Optional.of(receiverAccount));
            when(paymentRepository.save(any())).thenReturn(finalPayment);
            when(validations.validateTransactionUserFunds(finalPayment.getAmount())).thenReturn(false);

            Exception exception = assertThrows(InsufficientFundsException.class, () -> {
                paymentServiceImpl.processPayment(requestProcessPaymentDto);
            });

            String expectedMessage = "Fondos insuficientes para realizar el pago.";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));

            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(paymentRepository, times(1)).findById(any());
            verify(accountRepository, times(1)).findById(any());
            verify(paymentRepository, times(2)).save(any());
            verify(validations, times(1)).validateTransactionUserFunds(any());
        }
    }

    @Nested
    class GetAllPaymentsTests {
        @Test
        public void get_all_payments_should_be_Ok() throws Exception {
            var payment = new Payment("paymentId", "senderName", "senderSurname", "senderId",
                    "receiverName", "receiverSurname", "receiverId",
                    BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.PENDING, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderName", "senderSurname", "senderAccount",
                    "receiverName", "receiverSurname", "receiverAccount",  BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.PENDING);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            Account testAccount = new Account();
            testAccount.setAccountNumber(123456789L);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(paymentRepository.findPaymentsByAccount(any())).thenReturn(paymentList);
            when(accountRepository.findById(any())).thenReturn(Optional.of(testAccount));
            when(paymentMapper.paymentToResponseRecord(any(), anyString(), anyString())).thenReturn(responseRecordPayment);
            var result = paymentServiceImpl.getAllPayments();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(responseRecordPayment.id());
            assertThat(result.get(0).receiverAccount()).isEqualTo(responseRecordPayment.receiverAccount());
            assertThat(result.get(0).amount()).isEqualTo(responseRecordPayment.amount());
            assertThat(result.get(0).description()).isEqualTo(responseRecordPayment.description());
            assertThat(result.get(0).transactionDate()).isEqualTo(responseRecordPayment.transactionDate());
            assertThat(result.get(0).transactionStatus()).isEqualTo(responseRecordPayment.transactionStatus());
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(paymentRepository, times(1)).findPaymentsByAccount(any());
            verify(accountRepository, times(2)).findById(any());
            verify(paymentMapper, times(1)).paymentToResponseRecord(any(), anyString(),anyString());
        }

        @Test
        public void get_all_payments_should_NOT_be_Ok_due_NOT_FOUND() throws Exception {
            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("Cuenta no encontrada para el usuario"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.getAllPayments();
            });
            String expectedMessage = "Cuenta no encontrada para el usuario";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetPaymentsByStatusTests {
        @Test
        public void get_payments_by_status_should_be_Ok() throws Exception {
            var payment = new Payment("paymentId", "senderName", "senderSurname", "senderId",
                    "receiverName", "receiverSurname", "receiverId",
                    BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.DENIED, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderName", "senderSurname", "senderAccount",
                    "receiverName", "receiverSurname", "receiverAccount",  BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.DENIED);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            Account testAccount = new Account();
            testAccount.setAccountNumber(123456789L);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenReturn(TransactionStatus.DENIED);
            when(paymentRepository.findPaymentsByStatus(any(), any())).thenReturn(paymentList);
            when(accountRepository.findById(any())).thenReturn(Optional.of(testAccount));
            when(paymentMapper.paymentToResponseRecord(any(), anyString(), anyString())).thenReturn(responseRecordPayment);
            var result = paymentServiceImpl.getPaymentsByStatus(TransactionStatus.DENIED.toString());

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(responseRecordPayment.id());
            assertThat(result.get(0).receiverAccount()).isEqualTo(responseRecordPayment.receiverAccount());
            assertThat(result.get(0).amount()).isEqualTo(responseRecordPayment.amount());
            assertThat(result.get(0).description()).isEqualTo(responseRecordPayment.description());
            assertThat(result.get(0).transactionDate()).isEqualTo(responseRecordPayment.transactionDate());
            assertThat(result.get(0).transactionStatus()).isEqualTo(responseRecordPayment.transactionStatus());
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(validations, times(1)).statusConvert(any());
            verify(paymentRepository, times(1)).findPaymentsByStatus(any(), any());
            verify(accountRepository, times(2)).findById(any());
            verify(paymentMapper, times(1)).paymentToResponseRecord(any(), anyString(), anyString());
        }

        @Test
        public void get_payments_by_status_should_NOT_be_Ok_due_NOT_FOUND() throws Exception {
            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("Cuenta no encontrada para el usuario"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.getPaymentsByStatus("test");
            });
            String expectedMessage = "Cuenta no encontrada para el usuario";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void get_payments_by_status_should_NOT_be_Ok_due_BAD_REQUEST() throws Exception {
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenThrow(new InvalidDataException("El estado de la transacción no existe: "));
            Exception exception = assertThrows( InvalidDataException.class, () -> {
                paymentServiceImpl.getPaymentsByStatus("test");
            });
            String expectedMessage = "El estado de la transacción no existe: ";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetPaymentsByOneDateTests {
        @Test
        public void get_payments_by_date_should_be_Ok() throws Exception {
            var payment = new Payment("paymentId", "senderName", "senderSurname", "senderId",
                    "receiverName", "receiverSurname", "receiverId",
                    BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.DENIED, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderName", "senderSurname", "senderAccount",
                    "receiverName", "receiverSurname", "receiverAccount", BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.DENIED);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            Account testAccount = new Account();
            testAccount.setAccountNumber(123456789L);
            String todayWithoutHour = LocalDateTime.now().toLocalDate().toString();
            List<LocalDateTime> today24hs = Arrays.asList(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(dateFormatter.getDateFromString(todayWithoutHour )).thenReturn(today24hs);
            when(paymentRepository.findPaymentsByTransactionDate(any(), any(), any())).thenReturn(paymentList);
            when(accountRepository.findById(any())).thenReturn(Optional.of(testAccount));
            when(paymentMapper.paymentToResponseRecord(any(), anyString(), anyString())).thenReturn(responseRecordPayment);
            var result = paymentServiceImpl.getPaymentsByDate(todayWithoutHour);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(responseRecordPayment.id());
            assertThat(result.get(0).receiverAccount()).isEqualTo(responseRecordPayment.receiverAccount());
            assertThat(result.get(0).amount()).isEqualTo(responseRecordPayment.amount());
            assertThat(result.get(0).description()).isEqualTo(responseRecordPayment.description());
            assertThat(result.get(0).transactionDate()).isEqualTo(responseRecordPayment.transactionDate());
            assertThat(result.get(0).transactionStatus()).isEqualTo(responseRecordPayment.transactionStatus());
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(paymentRepository, times(1)).findPaymentsByTransactionDate(any(), any(), any());
            verify(accountRepository, times(2)).findById(any());
            verify(paymentMapper, times(1)).paymentToResponseRecord(any(), anyString(), anyString());
        }

        @Test
        public void get_payments_by_status_should_NOT_be_Ok_due_NOT_FOUND() throws Exception {
            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("Cuenta no encontrada para el usuario"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.getPaymentsByDate("test");
            });
            String expectedMessage = "Cuenta no encontrada para el usuario";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void get_payments_by_status_should_NOT_be_Ok_due_BAD_REQUEST() throws Exception {
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(dateFormatter.getDateFromString(any())).thenThrow(new InvalidDateFormatException("Formato de fecha erroneo. Debe ingresar yyyy-MM-dd"));
            Exception exception = assertThrows(InvalidDateFormatException.class, () -> {
                paymentServiceImpl.getPaymentsByDate("test");
            });
            String expectedMessage = "Formato de fecha erroneo. Debe ingresar yyyy-MM-dd";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetPaymentsBetweenDates{
        @Test
        public void get_payments_between_dates_should_be_ok() throws Exception{
            var payment = new Payment("paymentId", "senderName", "senderSurname", "senderId",
                    "receiverName", "receiverSurname", "receiverId",
                    BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.DENIED, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderName", "senderSurname", "senderAccount",
                    "receiverName", "receiverSurname", "receiverAccount", BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.DENIED);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            Account testAccount = new Account();
            testAccount.setAccountNumber(123456789L);
            String startDate = "2024-10-01";
            String endDate = "2024-10-07";
            List<LocalDateTime> dates = Arrays.asList(LocalDate.now().minusDays(7).atStartOfDay(), LocalDate.now().atStartOfDay());

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(dateFormatter.getDateFromString(startDate, endDate)).thenReturn(dates);
            when(paymentRepository.findPaymentsBetweenDates(any(), any(), any())).thenReturn(paymentList);
            when(accountRepository.findById(any())).thenReturn(Optional.of(testAccount));
            when(paymentMapper.paymentToResponseRecord(any(), anyString(), anyString())).thenReturn(responseRecordPayment);
            var result = paymentServiceImpl.getPaymentsBetweenDates(startDate, endDate);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(responseRecordPayment.id());
            assertThat(result.get(0).receiverAccount()).isEqualTo(responseRecordPayment.receiverAccount());
            assertThat(result.get(0).amount()).isEqualTo(responseRecordPayment.amount());
            assertThat(result.get(0).description()).isEqualTo(responseRecordPayment.description());
            assertThat(result.get(0).transactionDate()).isEqualTo(responseRecordPayment.transactionDate());
            assertThat(result.get(0).transactionStatus()).isEqualTo(responseRecordPayment.transactionStatus());
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(paymentRepository, times(1)).findPaymentsBetweenDates(any(), any(), any());
            verify(accountRepository, times(2)).findById(any());
            verify(paymentMapper, times(1)).paymentToResponseRecord(any(), anyString(), anyString());
        }

        @Test
        public void get_between_dates_should_NOT_be_ok_due_user_not_found() throws Exception{
            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("Cuenta no encontrada para el usuario"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.getPaymentsBetweenDates("fromDate", "toDate");
            });
            String expectedMessage = "Cuenta no encontrada para el usuario";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void get_between_dates_should_NOT_be_ok_due_invalid_date_format() throws Exception{
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(dateFormatter.getDateFromString(any(), any())).thenThrow(new InvalidDateFormatException("Formato de fecha erroneo. Debe ingresar yyyy-MM-dd"));
            Exception exception = assertThrows(InvalidDateFormatException.class, () -> {
                paymentServiceImpl.getPaymentsBetweenDates("fromDate", "toDate");
            });
            String expectedMessage = "Formato de fecha erroneo. Debe ingresar yyyy-MM-dd";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void get_between_dates_should_NOT_be_ok_due_second_date_before() throws Exception{
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            String fromDate = "2024-10-07";
            String toDate = "2024-10-03";
            List<LocalDateTime> dates = Arrays.asList(LocalDate.now().minusDays(7).atStartOfDay(), LocalDate.now().atStartOfDay());
            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(dateFormatter.getDateFromString(any(), any())).thenReturn(dates);
            when(paymentServiceImpl.getPaymentsBetweenDates(fromDate, toDate)).thenThrow(new ValidationException("La fecha final no puede ser menor a la inicial."));
            Exception exception = assertThrows(ValidationException.class, () -> {
                paymentServiceImpl.getPaymentsBetweenDates("fromDate", "toDate");
            });
            String expectedMessage = "La fecha final no puede ser menor a la inicial.";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetPaymentsAsClient {
        @Test
        public void get_as_client_should_be_ok() throws Exception {
            var payment = new Payment("paymentId", "senderName", "senderSurname", "senderId",
                    "receiverName", "receiverSurname", "receiverId",
                    BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.PENDING, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderName", "senderSurname", "senderAccount",
                    "receiverName", "receiverSurname", "receiverAccount",  BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.PENDING);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            Account testAccount = new Account();
            testAccount.setAccountNumber(123456789L);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(paymentRepository.findPaymentsAsClient(any())).thenReturn(paymentList);
            when(accountRepository.findById(any())).thenReturn(Optional.of(testAccount));
            when(paymentMapper.paymentToResponseRecord(any(), anyString(), anyString())).thenReturn(responseRecordPayment);
            var result = paymentServiceImpl.getPaymentsAsClient();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(responseRecordPayment.id());
            assertThat(result.get(0).receiverAccount()).isEqualTo(responseRecordPayment.receiverAccount());
            assertThat(result.get(0).amount()).isEqualTo(responseRecordPayment.amount());
            assertThat(result.get(0).description()).isEqualTo(responseRecordPayment.description());
            assertThat(result.get(0).transactionDate()).isEqualTo(responseRecordPayment.transactionDate());
            assertThat(result.get(0).transactionStatus()).isEqualTo(responseRecordPayment.transactionStatus());
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(paymentRepository, times(1)).findPaymentsAsClient(any());
            verify(accountRepository, times(2)).findById(any());
            verify(paymentMapper, times(1)).paymentToResponseRecord(any(), anyString(),anyString());
        }

        @Test
        public void get_as_client_should_NOT_be_ok() throws Exception{
            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("Cuenta no encontrada para el usuario"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.getPaymentsAsClient();
            });
            String expectedMessage = "Cuenta no encontrada para el usuario";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetPaymentsAsSeller{
        @Test
        public void get_as_seller_should_be_ok() throws Exception {
            var payment = new Payment("paymentId", "senderName", "senderSurname", "senderId",
                    "receiverName", "receiverSurname", "receiverId",
                    BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.PENDING, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderName", "senderSurname", "senderAccount",
                    "receiverName", "receiverSurname", "receiverAccount",  BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.PENDING);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());
            Account testAccount = new Account();
            testAccount.setAccountNumber(123456789L);

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(paymentRepository.findPaymentsAsSeller(any())).thenReturn(paymentList);
            when(accountRepository.findById(any())).thenReturn(Optional.of(testAccount));
            when(paymentMapper.paymentToResponseRecord(any(), anyString(), anyString())).thenReturn(responseRecordPayment);
            var result = paymentServiceImpl.getPaymentsAsSeller();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(responseRecordPayment.id());
            assertThat(result.get(0).receiverAccount()).isEqualTo(responseRecordPayment.receiverAccount());
            assertThat(result.get(0).amount()).isEqualTo(responseRecordPayment.amount());
            assertThat(result.get(0).description()).isEqualTo(responseRecordPayment.description());
            assertThat(result.get(0).transactionDate()).isEqualTo(responseRecordPayment.transactionDate());
            assertThat(result.get(0).transactionStatus()).isEqualTo(responseRecordPayment.transactionStatus());
            verify(validations, times(1)).getAuthenticatedUserAndAccount();
            verify(paymentRepository, times(1)).findPaymentsAsSeller(any());
            verify(accountRepository, times(2)).findById(any());
            verify(paymentMapper, times(1)).paymentToResponseRecord(any(), anyString(),anyString());
        }

        @Test
        public void get_as_seller_should_NOT_be_ok() throws Exception{
            when(validations.getAuthenticatedUserAndAccount()).thenThrow(new ResourceNotFoundException("Cuenta no encontrada para el usuario"));
            Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
                paymentServiceImpl.getPaymentsAsSeller();
            });
            String expectedMessage = "Cuenta no encontrada para el usuario";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }
}
