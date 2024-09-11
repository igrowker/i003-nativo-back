package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.RequestPaymentDto;
import com.igrowker.nativo.dtos.ResponsePaymentDto;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto) {
        return null;
    }
}
