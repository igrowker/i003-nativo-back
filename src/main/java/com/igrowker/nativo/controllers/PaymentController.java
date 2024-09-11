package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.RequestPaymentDto;
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

    @PostMapping("/qr")
    public ResponseEntity<?> generateQr(
            @RequestBody @Valid RequestPaymentDto requestPaymentDto){
        var result = paymentService.createQr(requestPaymentDto);
        return ResponseEntity.ok(result);
    }
}
