package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.RequestProcessPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponsePaymentDto;
import com.igrowker.nativo.dtos.payment.ResponseProcessPaymentDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final PaymentMapper paymentMapper;
    private final QRService qrService;

    @Override
    @Transactional
    public ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto) {
        // Mapeo el DTO a la entidad Payment
        Payment payment = paymentMapper.requestDtoToPayment(requestPaymentDto);

        // Guardo el payment en la base de datos
        Payment savedPayment = paymentRepository.save(payment);

        // Genero el código QR con el ID del pago recién creado
        String qrCode = null;
        try {
            qrCode = qrService.generateQrCode(savedPayment.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Actualizo la entidad Payment con el código QR
        savedPayment.setQr(qrCode);
        Payment withQrPayment = paymentRepository.save(savedPayment);

        // Mapear de nuevo a ResponsePaymentDto para devolverlo al frontend
        return paymentMapper.paymentToResponseDto(withQrPayment);
    }

    @Override
    @Transactional
    public ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto) {

        Payment newData = paymentMapper.requestProcessDtoToPayment(requestProcessPaymentDto);

        // Buscar el pago por ID
        Payment payment = paymentRepository.findById(newData.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        //new PaymentNotFoundException("Payment not found"));

        payment.setSenderAccount(newData.getSenderAccount());
        payment.setTransactionStatus(newData.getTransactionStatus());

        Payment updatedPayment = paymentRepository.save(payment);

        // Validar estado del pago
        if (!updatedPayment.getTransactionStatus().equals(TransactionStatus.ACCEPTED)) {
            // Si el pago fue rechazado
            updatedPayment.setTransactionStatus(TransactionStatus.DENIED);
            var result = paymentRepository.save(updatedPayment);
            return paymentMapper.paymentToResponseProcessDto(result);
        }

        // Aquí iría la lógica para validar los fondos del sender Y validar fondos
        var senderAccountId = updatedPayment.getSenderAccount();
        Account senderAccount = accountRepository.findById(senderAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        var actualSenderAmount = senderAccount.getAmount();
        var paymentAmount = updatedPayment.getAmount();
        if (paymentAmount.compareTo(actualSenderAmount) > 0){
            updatedPayment.setTransactionStatus(TransactionStatus.FAILED);
            Payment result = paymentRepository.save(updatedPayment);
            return paymentMapper.paymentToResponseProcessDto(result);
        }

        // Si los fondos son suficientes restar fondos del sender y Sumar fondos al receiver
        var newSenderAmount = actualSenderAmount.subtract(paymentAmount);
        senderAccount.setAmount(newSenderAmount);

        var receiverAccountId = updatedPayment.getReceiverAccount();
        Account receiverAccount = accountRepository.findById(receiverAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        var actualReceiverAmount = receiverAccount.getAmount();
        var newReceiverAmount = actualReceiverAmount.add(paymentAmount);
        receiverAccount.setAmount(newReceiverAmount);

        Account savedSenderAccount = accountRepository.save(senderAccount);
        Account savedReceiverAccount = accountRepository.save(receiverAccount);

        // Actualizar estado del pago a aceptado
        updatedPayment.setTransactionStatus(TransactionStatus.ACCEPTED);
        Payment savedPayment = paymentRepository.save(updatedPayment);

        // ToDo. Enviar notificaciones a ambos usuarios (esto sería idealmente otro servicio)
        // // que se le envie a ambos de alguna forma el resultado de la transaccion


        // Retornar respuesta final
        return paymentMapper.paymentToResponseProcessDto(savedPayment);
    }
}