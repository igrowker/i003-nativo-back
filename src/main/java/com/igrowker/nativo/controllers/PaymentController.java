package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<ResponseHistoryPayment>> getAllPayments(){
        List<ResponseHistoryPayment> result = paymentService.getAllPayments();
        return ResponseEntity.ok(result);
    }

    @GetMapping("estado/{status}")
    public ResponseEntity<List<ResponseHistoryPayment>> getPaymentsByStatus(@PathVariable String status){
        List<ResponseHistoryPayment> result = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("fecha/{date}")
    public ResponseEntity<List<ResponseHistoryPayment>> getPaymentsByDate(@PathVariable String date){
        List<ResponseHistoryPayment> result = paymentService.getPaymentsByDate(date);
        return ResponseEntity.ok(result);
    }
}