package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.services.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/donaciones")
public class DonationController {

    private final DonationService donationService;

    @PostMapping("/crear-donacion")
    public ResponseEntity<?> createDonation(@RequestBody @Valid RequestDonationDto requestDonationDto){
        if (requestDonationDto.anonymousDonation()) {
            return ResponseEntity.ok(donationService.createDonationTrue(requestDonationDto));
        }else {
            return ResponseEntity.ok(donationService.createDonationFalse(requestDonationDto));
        }
    }

    @PostMapping("/confirmar-donacion")
    public ResponseEntity<ResponseDonationConfirmationDto> confirmationDonation(@RequestBody @Valid RequestDonationConfirmationDto requestDonationConfirmationDto){
        return ResponseEntity.ok(donationService.confirmationDonation(requestDonationConfirmationDto));
    }

    @GetMapping("/historial-donaciones/donador/{id}")
    public ResponseEntity<List<ResponseDonationRecord>> recordDonationDonor(@PathVariable String idDonorAccount){
        return ResponseEntity.ok(donationService.recordDonationDonor(idDonorAccount));
    }

    @GetMapping("/historial-donaciones/beneficiario/{id}")
    public ResponseEntity<List<ResponseDonationRecord>> recordDonationBeneficiary(@PathVariable String idBeneficiaryAccount){
        return ResponseEntity.ok(donationService.recordDonationBeneficiary(idBeneficiaryAccount));
    }
    
}
