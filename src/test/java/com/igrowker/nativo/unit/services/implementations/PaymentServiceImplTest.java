package com.igrowker.nativo.unit.services.implementations;

import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponsePaymentDto;
import com.igrowker.nativo.dtos.payment.ResponseRecordPayment;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InvalidDataException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.implementation.PaymentServiceImpl;
import com.igrowker.nativo.services.implementation.QRService;
import com.igrowker.nativo.validations.Validations;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private PaymentMapper paymentMapper;
    @Mock
    private Validations validations;
    @Mock
    private QRService qrService;
    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    @Nested
    class CreateQrTests {
        @Test
        public void create_qr_should_be_Ok() throws Exception {
            var paymentRequestDto = new RequestPaymentDto("receiverId", BigDecimal.valueOf(100.50), "description");
            var payment = new Payment("paymentId", "senderId", "receiverId", BigDecimal.valueOf(100.50), LocalDateTime.now(), TransactionStatus.PENDING, "description", "long-long-long-qr");
            var paymentResponseDto = new ResponsePaymentDto("paymentId", "receiver", BigDecimal.valueOf(100.50), "description", "long-long-long-qr");

            when(paymentMapper.requestDtoToPayment(any())).thenReturn(payment);
            when(validations.isUserAccountMismatch(any())).thenReturn(false);
            when(paymentRepository.save(any())).thenReturn(payment);
            when(qrService.generateQrCode(any())).thenReturn("long-long-long-qr");
            when(paymentMapper.paymentToResponseDto(any())).thenReturn(paymentResponseDto);
            var res = paymentServiceImpl.createQr(paymentRequestDto);

            assertThat(res).isNotNull();
            assertThat(res.id()).isEqualTo(paymentResponseDto.id());
            assertThat(res.receiverAccount()).isEqualTo(paymentResponseDto.receiverAccount());
            assertThat(res.description()).isEqualTo(paymentResponseDto.description());
            assertThat(res.amount()).isEqualTo(paymentResponseDto.amount());
            assertThat(res.qr()).isEqualTo(paymentResponseDto.qr());
            verify(paymentRepository, times(2)).save(any());
            verify(validations, times(1)).isUserAccountMismatch(any());
            verify(qrService, times(1)).generateQrCode(any());
            verify(paymentMapper, times(1)).requestDtoToPayment(any());
            verify(paymentMapper, times(1)).paymentToResponseDto(any());
        }

        @Test
        public void create_qr_should_NOT_be_Ok() throws Exception {
            var paymentRequestDto = new RequestPaymentDto("receiverId", BigDecimal.valueOf(100.50), "description");

            when(validations.isUserAccountMismatch(any())).thenReturn(true);
            Exception exception = assertThrows(InvalidUserCredentialsException.class, () -> {
                paymentServiceImpl.createQr(paymentRequestDto);
            });
            String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String actualMessage = exception.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetAllPaymentsTests {
        @Test
        public void get_all_payments_should_be_Ok() throws Exception {
            var payment = new Payment("paymentId", "senderId", "receiverId", BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.PENDING, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderId",
                    "receiverId", BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.PENDING);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(paymentRepository.findPaymentsByAccount(any())).thenReturn(paymentList);
            when(paymentMapper.paymentListToResponseRecordList(paymentList)).thenReturn(responseList);
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
            verify(paymentMapper, times(1)).paymentListToResponseRecordList(paymentList);
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
            var payment = new Payment("paymentId", "senderId", "receiverId", BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.DENIED, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderId",
                    "receiverId", BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.DENIED);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(validations.statusConvert(any())).thenReturn(TransactionStatus.DENIED);
            when(paymentRepository.findPaymentsByStatus(any(), any())).thenReturn(paymentList);
            when(paymentMapper.paymentListToResponseRecordList(any())).thenReturn(responseList);
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
            verify(paymentMapper, times(1)).paymentListToResponseRecordList(any());
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
            var payment = new Payment("paymentId", "senderId", "receiverId", BigDecimal.valueOf(100.50),
                    LocalDateTime.now(), TransactionStatus.DENIED, "description", "qrCode");
            List<Payment> paymentList = List.of(payment);
            var responseRecordPayment = new ResponseRecordPayment("paymentId", "senderId",
                    "receiverId", BigDecimal.valueOf(100.50), "description",
                    LocalDateTime.now(), TransactionStatus.DENIED);
            List<ResponseRecordPayment> responseList = List.of(responseRecordPayment);
            var userAccountPair = new Validations.UserAccountPair(new User(), new Account());

            when(validations.getAuthenticatedUserAndAccount()).thenReturn(userAccountPair);
            when(paymentRepository.findPaymentsByTransactionDate(any(), any(), any())).thenReturn(paymentList);
            when(paymentMapper.paymentListToResponseRecordList(any())).thenReturn(responseList);
            var result = paymentServiceImpl.getPaymentsByDate("2024-07-20");

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
            verify(paymentMapper, times(1)).paymentListToResponseRecordList(any());
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

        //ToDo. If checked exception of data time format is done,
        // another test of that failing should be added, and it should be modified the one that pass.
    }
}
