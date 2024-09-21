package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.*;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.services.MicrocreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/microcreditos")
public class MicrocreditController {
    private final MicrocreditService microcreditService;
    private final ContributionService contributionService;

    @PostMapping("/solicitar")
    public ResponseEntity<?> createMicrocredit(@Valid @RequestBody RequestMicrocreditDto requestMicrocreditDto) {
        try {
            ResponseMicrocreditDto response = microcreditService.createMicrocredit(requestMicrocreditDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            // TODO enviar notificación
        } catch (ValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMicrocreditGetDto> getOne(@PathVariable String id) {
        ResponseMicrocreditGetDto response = microcreditService.getOne(id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getAll() {
        List<ResponseMicrocreditGetDto> response = microcreditService.getAll();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/historial-estados/{status}")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getMicrocreditsByTransactionStatus(@PathVariable String status) {
        List<ResponseMicrocreditGetDto> response = microcreditService.getMicrocreditsByTransactionStatus(status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/contribuir")
    public ResponseEntity<?> createContribution(@Valid @RequestBody RequestContributionDto requestContributionDto) {
        try {
            ResponseContributionDto response = contributionService.createContribution(requestContributionDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            // TODO enviar notificación
        } catch (ValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/estado/{status}")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getBy(@PathVariable String status) {
        List<ResponseMicrocreditGetDto> response = microcreditService.getBy(status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/pagar/{id}")
    public ResponseEntity<?> payMicrocredit(@PathVariable String id) {
        try {
            ResponseMicrocreditPaymentDto response = microcreditService.payMicrocredit(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
            // TODO enviar notificación
        } catch (ValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
