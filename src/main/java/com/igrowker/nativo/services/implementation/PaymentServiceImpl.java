package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.RequestPaymentDto;
import com.igrowker.nativo.dtos.RequestProcessPaymentDto;
import com.igrowker.nativo.dtos.ResponsePaymentDto;
import com.igrowker.nativo.dtos.ResponseProcessPaymentDto;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.mappers.PaymentMapper;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final QRService qrService;

    @Override
    public ResponsePaymentDto createQr(RequestPaymentDto requestPaymentDto) {
        // Mapeo el DTO a la entidad Payment
        Payment payment = paymentMapper.requestDtoToPayment(requestPaymentDto);

        // Guardo el payment en la base de datos
        Payment savedPayment = paymentRepository.save(payment);

        // Genero el código QR con el ID del pago recién creado
        String qrCode = null;
        try {
            qrCode = qrService.generateQrCode(savedPayment.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Actualizo la entidad Payment con el código QR
        savedPayment.setQr(qrCode);
        Payment withQrPayment = paymentRepository.save(savedPayment);

        // Mapear de nuevo a ResponsePaymentDto para devolverlo al frontend
        return paymentMapper.paymentToResponseDto(withQrPayment);
    }

    @Override
    public ResponseProcessPaymentDto processPayment(RequestProcessPaymentDto requestProcessPaymentDto) {
        // Buscar el pago por ID
        Payment payment = paymentRepository.findById(requestProcessPaymentDto.paymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        //new PaymentNotFoundException("Payment not found"));

        // Validar estado del pago
        if (!"accepted".equalsIgnoreCase(requestProcessPaymentDto.status())) {
            // Si el pago fue rechazado
            payment.setTransactionStatus(TransactionStatus.DENIED);
            paymentRepository.save(payment);
            return new ResponseProcessPaymentDto(payment.getId(), requestProcessPaymentDto.senderId(),
                    payment.getReceiver(), payment.getAmount(), "DENIED", "El pago fue rechazado.");
        }

        // Aquí iría la lógica para validar los fondos del sender
        // validar fondos

        // Si los fondos son suficientes (podriamos ponerlo como transaction)
        // Restar fondos del sender
        // Sumar fondos al receiver

        // Actualizar estado del pago a aceptado
        payment.setTransactionStatus(TransactionStatus.ACCEPTED);
        paymentRepository.save(payment);

        // Enviar notificaciones a ambos usuarios (esto sería idealmente otro servicio)
        // // que se le envie a ambos de alguna forma el resultado de la transaccion

        // Retornar respuesta final
        return new ResponseProcessPaymentDto(payment.getId(), requestProcessPaymentDto.senderId(),
                payment.getReceiver(), payment.getAmount(), "ACCEPTED", "El pago fue procesado exitosamente.");
    }

}

