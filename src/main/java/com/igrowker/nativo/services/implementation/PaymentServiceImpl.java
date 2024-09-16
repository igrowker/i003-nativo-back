package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.PaymentService;
import com.igrowker.nativo.validations.TransactionStatusConvert;
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

    @Override
    @Transactional
    public ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto) {
        Payment payment = paymentMapper.requestDtoToPayment(requestPaymentDto);

        Payment savedPayment = paymentRepository.save(payment);

        String qrCode = null;
        try {
            qrCode = qrService.generateQrCode(savedPayment.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        savedPayment.setQr(qrCode);
        Payment withQrPayment = paymentRepository.save(savedPayment);

        return paymentMapper.paymentToResponseDto(withQrPayment);
    }

    @Override
    @Transactional
    public ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto) {

        Payment newData = paymentMapper.requestProcessDtoToPayment(requestProcessPaymentDto);

        Payment payment = paymentRepository.findById(newData.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        //new PaymentNotFoundException("Payment not found"));

        payment.setSenderAccount(newData.getSenderAccount());
        payment.setTransactionStatus(newData.getTransactionStatus());

        Payment updatedPayment = paymentRepository.save(payment);

        if (!updatedPayment.getTransactionStatus().equals(TransactionStatus.ACCEPTED)) {
            updatedPayment.setTransactionStatus(TransactionStatus.DENIED);
            var result = paymentRepository.save(updatedPayment);
            return paymentMapper.paymentToResponseProcessDto(result);
        }

        var senderAccountId = updatedPayment.getSenderAccount();
        Account senderAccount = accountRepository.findById(senderAccountId)
                .orElseThrow(() -> new RuntimeException("account not found"));
        var actualSenderAmount = senderAccount.getAmount();
        var paymentAmount = updatedPayment.getAmount();
        if (paymentAmount.compareTo(actualSenderAmount) > 0){
            updatedPayment.setTransactionStatus(TransactionStatus.FAILED);
            Payment result = paymentRepository.save(updatedPayment);
            return paymentMapper.paymentToResponseProcessDto(result);
        }

        var newSenderAmount = actualSenderAmount.subtract(paymentAmount);
        senderAccount.setAmount(newSenderAmount);

        var receiverAccountId = updatedPayment.getReceiverAccount();
        Account receiverAccount = accountRepository.findById(receiverAccountId)
                .orElseThrow(() -> new RuntimeException("account not found"));
        var actualReceiverAmount = receiverAccount.getAmount();
        var newReceiverAmount = actualReceiverAmount.add(paymentAmount);
        receiverAccount.setAmount(newReceiverAmount);

        Account savedSenderAccount = accountRepository.save(senderAccount);
        Account savedReceiverAccount = accountRepository.save(receiverAccount);

        updatedPayment.setTransactionStatus(TransactionStatus.ACCEPTED);
        Payment savedPayment = paymentRepository.save(updatedPayment);

        // ToDo. Enviar notificaciones a ambos usuarios (esto sería idealmente otro servicio)
        // // que se le envie a ambos de alguna forma el resultado de la transaccion

        return paymentMapper.paymentToResponseProcessDto(savedPayment);
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
}