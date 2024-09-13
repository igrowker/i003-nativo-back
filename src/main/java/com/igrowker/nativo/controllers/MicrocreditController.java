package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.services.MicrocreditService;
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
    public ResponseEntity<ResponseMicrocreditDto> createMicrocredit(@RequestBody RequestMicrocreditDto requestMicrocreditDto) {
        ResponseMicrocreditDto response = microcreditService.createMicrocredit(requestMicrocreditDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
        //TODO enviar notificación
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMicrocreditGetDto> getOne(Long id) {
        ResponseMicrocreditGetDto response = microcreditService.getOne(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getAll() {
        List<ResponseMicrocreditGetDto> response = microcreditService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/contribuir")
    public ResponseEntity<ResponseContributionDto> createContribution(@RequestBody RequestContributionDto requestContributionDto) {
        ResponseContributionDto response = contributionService.createContribution(requestContributionDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
        //TODO enviar notificación
    }
}
