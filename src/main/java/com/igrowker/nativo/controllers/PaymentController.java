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

    @PostMapping("/crearqr")
    public ResponseEntity<ResponsePaymentDto> generateQr(
            @RequestBody @Valid RequestPaymentDto requestPaymentDto){
        ResponsePaymentDto result = paymentService.createQr(requestPaymentDto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pagarqr")
    public ResponseEntity<ResponseProcessPaymentDto> processPayment(
            @RequestBody @Valid RequestProcessPaymentDto requestProcessPaymentDto) {
        ResponseProcessPaymentDto result = paymentService.processPayment(requestProcessPaymentDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<List<ResponseHistoryPayment>> getAllPayments(@PathVariable String id){
        List<ResponseHistoryPayment> result = paymentService.getAllPayments(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/status/{status}")
    public ResponseEntity<List<ResponseHistoryPayment>> getAllPayments(@PathVariable String id,
                                                                       @PathVariable String status){
        List<ResponseHistoryPayment> result = paymentService.getPaymentsByStatus(id, status);
        return ResponseEntity.ok(result);
    }
}