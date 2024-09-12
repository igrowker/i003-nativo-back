package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.RequestPaymentDto;
import com.igrowker.nativo.dtos.RequestProcessPaymentDto;
import com.igrowker.nativo.dtos.ResponsePaymentDto;
import com.igrowker.nativo.dtos.ResponseProcessPaymentDto;
import com.igrowker.nativo.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}