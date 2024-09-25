package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.ExpiredTransactionException;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.PaymentService;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.validations.Validations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final QRService qrService;
    private final Validations validations;
    private final GeneralTransactions transactions;

    @Override
    @Transactional
    public ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto) {
        Payment payment = paymentMapper.requestDtoToPayment(requestPaymentDto);

        if(validations.isUserAccountMismatch(requestPaymentDto.receiverAccount())){
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
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
        TransactionStatus dtoStatus = validations.statusConvert(requestProcessPaymentDto.transactionStatus());
        Payment newData = paymentMapper.requestProcessDtoToPayment(requestProcessPaymentDto);

        if(validations.isUserAccountMismatch(requestProcessPaymentDto.senderAccount())){
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicacion");
        }

        Payment payment = paymentRepository.findById(newData.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El Pago solicitado no fue encontrado"));

        if (!payment.getTransactionStatus().equals(TransactionStatus.PENDING)) {
            throw new ExpiredTransactionException("El QR ya fue utilizado.");
        }


        if(payment.getTransactionDate().plusMinutes(10).isBefore(LocalDateTime.now())){
            payment.setTransactionStatus(TransactionStatus.EXPIRED);
            Payment result = paymentRepository.save(payment);
            throw new ExpiredTransactionException("El QR no puede ser procesado por exceso en el limite de tiempo. Genere uno nuevo.");
        }

        payment.setSenderAccount(newData.getSenderAccount());
        payment.setTransactionStatus(dtoStatus);
        Payment updatedPayment = paymentRepository.save(payment);

        if (!updatedPayment.getTransactionStatus().equals(TransactionStatus.ACCEPTED)) {
            updatedPayment.setTransactionStatus(TransactionStatus.DENIED);
            var result = paymentRepository.save(updatedPayment);
            return paymentMapper.paymentToResponseProcessDto(result);
        }

        if(!validations.validateTransactionUserFunds(updatedPayment.getAmount())){
            updatedPayment.setTransactionStatus(TransactionStatus.FAILED);
            Payment result = paymentRepository.save(updatedPayment);
            throw new InsufficientFundsException("Fondos insuficientes para realizar el pago.");
        }

        transactions.updateBalances(payment.getSenderAccount(), payment.getReceiverAccount(), payment.getAmount());

        updatedPayment.setTransactionStatus(TransactionStatus.ACCEPTED);

        Payment savedPayment = paymentRepository.save(updatedPayment);

        return paymentMapper.paymentToResponseProcessDto(savedPayment);
    }

    @Override
    public List<ResponseRecordPayment> getAllPayments() {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        List<Payment> paymentList = paymentRepository.findPaymentsByAccount(accountAndUser.account.getId());
        var result = paymentMapper.paymentListToResponseRecordList(paymentList);
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsByStatus(String status) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        TransactionStatus statusEnum = validations.statusConvert(status);
        List<Payment> paymentList = paymentRepository.findPaymentsByStatus(accountAndUser.account.getId(), statusEnum);
        var result = paymentMapper.paymentListToResponseRecordList(paymentList);
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsByDate(String date) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate transactionDate = LocalDate.parse(date, formatter);
        LocalDateTime startDate = transactionDate.atStartOfDay();
        LocalDateTime endDate = transactionDate.plusDays(1).atStartOfDay();
        List<Payment> paymentList = paymentRepository.findPaymentsByTransactionDate(
                accountAndUser.account.getId(), startDate, endDate);
        var result = paymentMapper.paymentListToResponseRecordList(paymentList);
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsBetweenDates(String fromDate, String toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate firstDate = LocalDate.parse(fromDate, formatter);
            LocalDate secondDate = LocalDate.parse(toDate, formatter);

            if (firstDate.isAfter(secondDate)) {
                throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
            }

            LocalDateTime startDate = firstDate.atStartOfDay();
            LocalDateTime endDate = secondDate.plusDays(1).atStartOfDay();

            Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();

            List<Payment> paymentList = paymentRepository.findPaymentsBetweenDates(
                    accountAndUser.account.getId(), startDate, endDate);

            return paymentMapper.paymentListToResponseRecordList(paymentList);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("Error al parsear las fechas: " + fromDate + " o " + toDate, fromDate, 0);
        }
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsAsClient() {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        List<Payment> paymentList = paymentRepository.findPaymentsAsClient(
                accountAndUser.account.getId());
        List<ResponseRecordPayment> result = paymentMapper.paymentListToResponseRecordList(paymentList);
        return result;
    }

    @Override
    public List<ResponseRecordPayment> getPaymentsAsSeller() {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();
        List<Payment> paymentList = paymentRepository.findPaymentsAsSeller(
                accountAndUser.account.getId());
        List<ResponseRecordPayment> result = paymentMapper.paymentListToResponseRecordList(paymentList);
        return result;
    }
}