package com.igrowker.nativo.controllers;

import com.igrowker.nativo.services.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/donaciones")
public class DonationController {

    private final DonationService donationService;

    
}
