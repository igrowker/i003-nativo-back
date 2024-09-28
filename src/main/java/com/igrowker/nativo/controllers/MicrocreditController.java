package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.*;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.services.MicrocreditService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ResponseMicrocreditDto> createMicrocredit(@Valid @RequestBody RequestMicrocreditDto requestMicrocreditDto) throws MessagingException {
            ResponseMicrocreditDto response = microcreditService.createMicrocredit(requestMicrocreditDto);

            return ResponseEntity.ok(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMicrocreditGetDto> getOne(@PathVariable String id) {
        ResponseMicrocreditGetDto response = microcreditService.getOne(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getAll() {
        List<ResponseMicrocreditGetDto> response = microcreditService.getAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/historial-estados/{status}")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getMicrocreditsByTransactionStatus(@PathVariable String status) {
        List<ResponseMicrocreditGetDto> response = microcreditService.getMicrocreditsByTransactionStatus(status);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/contribuir")
    public ResponseEntity<?> createContribution(@Valid @RequestBody RequestContributionDto requestContributionDto) throws MessagingException {
            ResponseContributionDto response = contributionService.createContribution(requestContributionDto);

            return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{status}")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getBy(@PathVariable String status) {
        List<ResponseMicrocreditGetDto> response = microcreditService.getBy(status);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/pagar/{id}")
    public ResponseEntity<ResponseMicrocreditPaymentDto> payMicrocredit(@PathVariable String id) throws MessagingException {
        ResponseMicrocreditPaymentDto response = microcreditService.payMicrocredit(id);

        return ResponseEntity.ok(response);
    }
}
