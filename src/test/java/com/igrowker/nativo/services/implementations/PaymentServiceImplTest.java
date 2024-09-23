package com.igrowker.nativo.services.implementations;

import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponsePaymentDto;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.implementation.PaymentServiceImpl;
import com.igrowker.nativo.services.implementation.QRService;
import com.igrowker.nativo.validations.Validations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Test
    public void create_qr_should_be_Ok() throws Exception {
        var paymentRequestDto = new RequestPaymentDto("receiverId", BigDecimal.valueOf(100.50), "description");
        var payment = new Payment("paymentId", "senderId", "receiverId", BigDecimal.valueOf(100.50), LocalDateTime.now(), TransactionStatus.PENDENT, "description", "long-long-long-qr");
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
            paymentServiceImpl.createQr(paymentRequestDto);});
        String expectedMessage = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

}

