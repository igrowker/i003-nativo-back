package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.PaymentService;
import com.igrowker.nativo.validations.TransactionStatusConvert;
import com.igrowker.nativo.validations.TransactionValidations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final PaymentMapper paymentMapper;
    private final QRService qrService;
    private final TransactionStatusConvert transactionStatusConvert;
    private final TransactionValidations transactionValidations;

    @Override
    @Transactional
    public ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto) {
        Payment payment = paymentMapper.requestDtoToPayment(requestPaymentDto);

        if(transactionValidations.isUserAccountMismatch(requestPaymentDto.receiverAccount())){
            throw new ValidationException("La cuenta indicada no coincide con el usuario logeado en la aplicacion");
        }

        Payment savedPayment = paymentRepository.save(payment);

        String qrCode = null;
        try {
            qrCode = qrService.generateQrCode(savedPayment.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el código QR", e);
        }

        savedPayment.setQr(qrCode);
        Payment withQrPayment = paymentRepository.save(savedPayment);

        return paymentMapper.paymentToResponseDto(withQrPayment);
    }

    @Override
    @Transactional
    public ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto) {

        Payment newData = paymentMapper.requestProcessDtoToPayment(requestProcessPaymentDto);

        if(transactionValidations.isUserAccountMismatch(requestProcessPaymentDto.senderAccount())){
            throw new ValidationException("La cuenta indicada no coincide con el usuario logeado en la aplicacion");
        }

        Payment payment = paymentRepository.findById(newData.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pago solicitado no encontrado"));

        payment.setSenderAccount(newData.getSenderAccount());
        payment.setTransactionStatus(newData.getTransactionStatus());

        Payment updatedPayment = paymentRepository.save(payment);

        if (!updatedPayment.getTransactionStatus().equals(TransactionStatus.ACCEPTED)) {
            updatedPayment.setTransactionStatus(TransactionStatus.DENIED);
            var result = paymentRepository.save(updatedPayment);
            return paymentMapper.paymentToResponseProcessDto(result);
        }

        if(!transactionValidations.validateTransactionUserFunds(updatedPayment.getAmount())){
            updatedPayment.setTransactionStatus(TransactionStatus.FAILED);
            Payment result = paymentRepository.save(updatedPayment);
            return paymentMapper.paymentToResponseProcessDto(result);
        }

        Payment savedPayment = this.updateBalancesAndPayment(updatedPayment);

        return paymentMapper.paymentToResponseProcessDto(savedPayment);

        // ToDo. Enviar notificaciones a ambos usuarios (esto sería idealmente otro servicio)
        // // que se le envie a ambos de alguna forma el resultado de la transaccion
    }

    @Override
    public List<ResponseHistoryPayment> getAllPayments(String id) {
        List<Payment> paymentList = paymentRepository.findPaymentsByAccount(id);
        var result = paymentMapper.paymentListToResponseHistoryList(paymentList);
        return result;
    }

    @Override
    public List<ResponseHistoryPayment> getPaymentsByStatus(String id, String status) {
        TransactionStatus statusEnum = transactionStatusConvert.statusConvert(status);
        List<Payment> paymentList = paymentRepository.findPaymentsByStatus(id, statusEnum);
        var result = paymentMapper.paymentListToResponseHistoryList(paymentList);
        return result;
    }

    @Override
    public Payment updateBalancesAndPayment(Payment payment) {
        Account senderAccount = accountRepository.findById(payment.getSenderAccount())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        Account receiverAccount = accountRepository.findById(payment.getReceiverAccount())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        senderAccount.setAmount(senderAccount.getAmount().subtract(payment.getAmount()));
        receiverAccount.setAmount(receiverAccount.getAmount().add(payment.getAmount()));

        Account updatedSenderAccount =  accountRepository.save(senderAccount);
        Account updatedReceiverAccount =  accountRepository.save(receiverAccount);

        payment.setTransactionStatus(TransactionStatus.ACCEPTED);
        Payment savedPayment = paymentRepository.save(payment);

        return savedPayment;
    }
}