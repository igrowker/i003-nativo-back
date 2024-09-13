package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.donation.RequestDonationConfirmationDto;
import com.igrowker.nativo.dtos.donation.RequestDonationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationConfirmationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationDto;
import com.igrowker.nativo.services.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/donaciones")
public class DonationController {

    private final DonationService donationService;

    @PostMapping("/crear-donacion")
    public ResponseEntity<ResponseDonationDto> createDonation(@RequestBody @Valid RequestDonationDto requestDonationDto){

        return ResponseEntity.ok(donationService.createDonation(requestDonationDto));
    }

    @PostMapping("/confirmation-donation")
    public ResponseEntity<ResponseDonationConfirmationDto> confirmationDonation(@RequestBody @Valid RequestDonationConfirmationDto requestDonationConfirmationDto){
        return ResponseEntity.ok(donationService.confirmationDonation(requestDonationConfirmationDto));
    }
    
}
