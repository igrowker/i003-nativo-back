package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.ExpiredTransactionException;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.PaymentService;
import com.igrowker.nativo.utils.DateFormatter;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.validations.Validations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final AccountRepository accountRepository;
    private final QRService qrService;
    private final Validations validations;
    private final GeneralTransactions transactions;
    private final DateFormatter dateFormatter;

    @Override
    @Transactional
    public ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto) {
        var userAndAccount = validations.getAuthenticatedUserAndAccount();
        Payment payment = paymentMapper.requestDtoToPayment(requestPaymentDto);
        payment.setReceiverName(userAndAccount.user.getName());
        payment.setReceiverSurname(userAndAccount.user.getSurname());
        Payment savedPayment = paymentRepository.save(payment);
        String qrCode = qrService.generateQrCode(savedPayment.getId());
        savedPayment.setQr(qrCode);
        Payment withQrPayment = paymentRepository.save(savedPayment);
        return paymentMapper.paymentToResponseDto(withQrPayment, userAndAccount.account.getAccountNumber().toString());
    }

    @Override
    @Transactional
    public ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto) {
        var senderAndAccount = validations.getAuthenticatedUserAndAccount();
        TransactionStatus dtoStatus = validations.statusConvert(requestProcessPaymentDto.transactionStatus());
        Payment newData = paymentMapper.requestProcessDtoToPayment(requestProcessPaymentDto);
        Payment payment = paymentRepository.findById(newData.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El Pago solicitado no fue encontrado"));
        var senderAccount = senderAndAccount.account.getAccountNumber().toString();
        var receiverAccount = accountRepository.findById(payment.getReceiverAccount()).get().getAccountNumber().toString();

        if (!payment.getTransactionStatus().equals(TransactionStatus.PENDING)) {
            throw new ExpiredTransactionException("El QR ya fue utilizado.");
        }
        if(payment.getTransactionDate().plusMinutes(10).isBefore(LocalDateTime.now())){
            payment.setTransactionStatus(TransactionStatus.EXPIRED);
            Payment result = paymentRepository.save(payment);
            throw new ExpiredTransactionException("El QR no puede ser procesado por exceso en el limite de tiempo. Genere uno nuevo.");
        }
        payment.setSenderName(senderAndAccount.user.getName());
        payment.setSenderSurname(senderAndAccount.user.getSurname());
        payment.setSenderAccount(newData.getSenderAccount());
        payment.setTransactionStatus(dtoStatus);
        Payment updatedPayment = paymentRepository.save(payment);
        if (!updatedPayment.getTransactionStatus().equals(TransactionStatus.ACCEPTED)) {
            updatedPayment.setTransactionStatus(TransactionStatus.DENIED);
            var result = paymentRepository.save(updatedPayment);
            return paymentMapper.paymentToResponseProcessDto(result, senderAccount, receiverAccount);
        }
        if(!validations.validateTransactionUserFunds(updatedPayment.getAmount())){
            updatedPayment.setTransactionStatus(TransactionStatus.FAILED);
            Payment result = paymentRepository.save(updatedPayment);
            throw new InsufficientFundsException("Fondos insuficientes para realizar el pago.");
        }
        transactions.updateBalances(payment.getSenderAccount(), payment.getReceiverAccount(), payment.getAmount());
        Payment savedPayment = paymentRepository.save(updatedPayment);
        return paymentMapper.paymentToResponseProcessDto(savedPayment, senderAccount, receiverAccount);
    }

    @Override
    public List<ResponseRecordPayment> getAllPayments() {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        List<Payment> paymentList = paymentRepository.findPaymentsByAccount(accountAndUser.account.getId());
        var result = paymentList.stream().map(this::mapPaymentToRecord).toList();
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsByStatus(String status) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        TransactionStatus statusEnum = validations.statusConvert(status);
        List<Payment> paymentList = paymentRepository.findPaymentsByStatus(accountAndUser.account.getId(), statusEnum);
        var result = paymentList.stream().map(this::mapPaymentToRecord).toList();
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsByDate(String date) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        List<LocalDateTime> elapsedDate = dateFormatter.getDateFromString(date);
        LocalDateTime startDate = elapsedDate.get(0);
        LocalDateTime endDate = elapsedDate.get(1);
        List<Payment> paymentList = paymentRepository.findPaymentsByTransactionDate(
                accountAndUser.account.getId(), startDate, endDate);
        var result = paymentList.stream().map(this::mapPaymentToRecord).toList();
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsBetweenDates(String fromDate, String toDate) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();

        List<LocalDateTime> elapsedDate = dateFormatter.getDateFromString(fromDate, toDate);
        LocalDateTime startDate = elapsedDate.get(0);
        LocalDateTime endDate = elapsedDate.get(1);

        List<Payment> paymentList = paymentRepository.findPaymentsBetweenDates(
                accountAndUser.account.getId(), startDate, endDate);

        var result = paymentList.stream().map(this::mapPaymentToRecord).toList();
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsAsClient() {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        List<Payment> paymentList = paymentRepository.findPaymentsAsClient(
                accountAndUser.account.getId());
        var result = paymentList.stream().map(this::mapPaymentToRecord).toList();
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsAsSeller() {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        List<Payment> paymentList = paymentRepository.findPaymentsAsSeller(
                accountAndUser.account.getId());
        var result = paymentList.stream().map(this::mapPaymentToRecord).toList();
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsByClient(String clientId) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        List<Payment> paymentList = paymentRepository.findPendingPaymentsBySender(accountAndUser.account.getId());
        var result = paymentList.stream().map(this::mapPaymentToRecord).toList();
        return result;
    }

    @Override
    public ResponseRecordPayment getPaymentsById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El Pago solicitado no fue encontrado"));
        return paymentMapper.paymentToResponseRecord(payment, payment.getSenderAccount(), payment.getReceiverAccount());
    }

    private ResponseRecordPayment mapPaymentToRecord(Payment payment){
        var senderAccount = accountRepository.findById(payment.getSenderAccount()).get().getAccountNumber().toString();
        var receiverAccount = accountRepository.findById(payment.getReceiverAccount()).get().getAccountNumber().toString();
        return paymentMapper.paymentToResponseRecord(payment, senderAccount, receiverAccount);
    }

    @Override
    @Transactional
    public ResponsePaymentDto createQrId(DemodayDtoRequestPayment requestPaymentDto) {
        var userAndAccount = validations.getAuthenticatedUserAndAccount();
        Payment payment = paymentMapper.demodayDtoToPayment(requestPaymentDto);
        payment.setReceiverName(userAndAccount.user.getName());
        payment.setReceiverSurname(userAndAccount.user.getSurname());
        Payment savedPayment = paymentRepository.save(payment);
        String qrCode = qrService.generateQrCode(savedPayment.getId());
        savedPayment.setQr(qrCode);
        Payment withQrPayment = paymentRepository.save(savedPayment);
        return paymentMapper.paymentToResponseDto(withQrPayment, userAndAccount.account.getAccountNumber().toString());
    }

}