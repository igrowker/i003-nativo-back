package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.services.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    // UNIFICAR
    @GetMapping("/historial-donaciones/donador/{idDonorAccount}")
    public ResponseEntity<List<ResponseDonationRecord>> recordDonationDonor(@PathVariable String idDonorAccount){
        return ResponseEntity.ok(donationService.recordDonationDonor(idDonorAccount));
    }

    @GetMapping("/historial-donaciones/beneficiario/{idBeneficiaryAccount}")
    public ResponseEntity<List<ResponseDonationRecord>> recordDonationBeneficiary(@PathVariable String idBeneficiaryAccount){
        return ResponseEntity.ok(donationService.recordDonationBeneficiary(idBeneficiaryAccount));
    }

    //ENDPOINT QUE BUSQUE POR STATUS
    @Operation(summary = "Obtener donaciones por estado de transacción",
            description = "Endpoint que permite obtener todas las donaciones con un estado de la trsancción en específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de donaciones obtenidas con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDonationRecord.class))),
                            @ApiResponse(responseCode = "403", content = @Content),
                            @ApiResponse(responseCode = "404", content = @Content)
    })

    @GetMapping("/historial-donaciones")
    public ResponseEntity<List<ResponseDonationRecord>> getDonationsBetweenDatesOrStatus(@RequestParam(required = false) String fromDate,
                                                                                         @RequestParam(required = false) String toDate,
                                                                                         @RequestParam(required = false) String status){
        return ResponseEntity.ok(donationService.getDonationBtBetweenDatesOrStatus(fromDate,toDate,status));
    }

}
