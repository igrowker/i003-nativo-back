package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pagos")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/crear-qr")
    public ResponseEntity<ResponsePaymentDto> generateQr(
            @RequestBody @Valid RequestPaymentDto requestPaymentDto){
        ResponsePaymentDto result = paymentService.createQr(requestPaymentDto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pagar-qr")
    public ResponseEntity<ResponseProcessPaymentDto> processPayment(
            @RequestBody @Valid RequestProcessPaymentDto requestProcessPaymentDto) {
        ResponseProcessPaymentDto result = paymentService.processPayment(requestProcessPaymentDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/todo")
    public ResponseEntity<List<ResponseRecordPayment>> getAllPayments(){
        List<ResponseRecordPayment> result = paymentService.getAllPayments();
        return ResponseEntity.ok(result);
    }

    @GetMapping("estado/{status}")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsByStatus(@PathVariable String status){
        List<ResponseRecordPayment> result = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("fecha/{date}")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsByDate(@PathVariable String date){
        List<ResponseRecordPayment> result = paymentService.getPaymentsByDate(date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/entrefechas")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsBetweenDates(
            @RequestParam String fromDate,
            @RequestParam String toDate) {

        List<ResponseRecordPayment> result = paymentService.getPaymentsBetweenDates(fromDate, toDate);
        return ResponseEntity.ok(result);

    }

    @GetMapping("realizados")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsAsClient(){
        List<ResponseRecordPayment> result = paymentService.getPaymentsAsClient();
        return ResponseEntity.ok(result);
    }

    @GetMapping("recibidos")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsAsSeller(){
        List<ResponseRecordPayment> result = paymentService.getPaymentsAsSeller();
        return ResponseEntity.ok(result);
    }
}