package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Pagos")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Crear un nuevo QR de pago.",
            description = "Endpoint que permite crear un QR para ser cobrado por el usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR creado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponsePaymentDto.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PostMapping("/crear-qr")
    public ResponseEntity<ResponsePaymentDto> generateQr(
            @RequestBody @Valid RequestPaymentDto requestPaymentDto) {
        ResponsePaymentDto result = paymentService.createQr(requestPaymentDto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Pagar QR de pago.",
            description = "Endpoint que permite pagar un QR creado por otro usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR pagado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseProcessPaymentDto.class))),
            @ApiResponse(responseCode = "200", description = "Pago de QR denegado por el usuario.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseProcessPaymentDto.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PostMapping("/pagar-qr")
    public ResponseEntity<ResponseProcessPaymentDto> processPayment(
            @RequestBody @Valid RequestProcessPaymentDto requestProcessPaymentDto) {
        ResponseProcessPaymentDto result = paymentService.processPayment(requestProcessPaymentDto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener todos los pagos del usuario autenticado (realizados y recibidos)",
            description = "Endpoint que permite obtener todos los pagos del usuario autenticado (realizados y recibidos).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de pagos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordPayment.class))),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/todo")
    public ResponseEntity<List<ResponseRecordPayment>> getAllPayments() {
        List<ResponseRecordPayment> result = paymentService.getAllPayments();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener los pagos del usuario autenticado segun STATUS definido.",
            description = "Endpoint que permite obtener los pagos del usuario autenticado filtrados por STATUS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de pagos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordPayment.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("estado/{status}")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsByStatus(@PathVariable String status) {
        List<ResponseRecordPayment> result = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener los pagos del usuario autenticado en una fecha especifica.",
            description = "Endpoint que permite obtener los pagos del usuario autenticado para una fecha especifica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de pagos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordPayment.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("fecha/{date}")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsByDate(@PathVariable String date) {
        List<ResponseRecordPayment> result = paymentService.getPaymentsByDate(date);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener los pagos del usuario autenticado entre dos fechas.",
            description = "Endpoint que permite obtener los pagos del usuario autenticado entre dos fechas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de pagos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordPayment.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/entrefechas")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsBetweenDates(
            @RequestParam String fromDate,
            @RequestParam String toDate) {

        List<ResponseRecordPayment> result = paymentService.getPaymentsBetweenDates(fromDate, toDate);
        return ResponseEntity.ok(result);

    }

    @Operation(summary = "Obtener todos los pagos realizados por usuario autenticado.",
            description = "Endpoint que permite obtener todos los pagos realizados por el usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de pagos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordPayment.class))),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("realizados")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsAsClient() {
        List<ResponseRecordPayment> result = paymentService.getPaymentsAsClient();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener todos los pagos recibidos por usuario autenticado.",
            description = "Endpoint que permite obtener todos los pagos recibidos por el usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de pagos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordPayment.class))),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("recibidos")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsAsSeller() {
        List<ResponseRecordPayment> result = paymentService.getPaymentsAsSeller();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cliente/{clientId}")
    public ResponseEntity<List<ResponseRecordPayment>> getPaymentsByClient(@PathVariable String clientId) {
        List<ResponseRecordPayment> result = paymentService.getPaymentsByClient(clientId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ResponseRecordPayment> getPaymentById(@PathVariable String id) {
        ResponseRecordPayment result = paymentService.getPaymentsById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/crear-qr-id")
    public ResponseEntity<ResponsePaymentDto> generateQr(
            @RequestBody @Valid DemodayDtoRequestPayment requestPaymentDto) {
        ResponsePaymentDto result = paymentService.createQrId(requestPaymentDto);
        return ResponseEntity.ok(result);
    }
}