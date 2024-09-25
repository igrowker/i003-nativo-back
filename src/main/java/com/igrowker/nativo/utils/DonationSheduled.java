package com.igrowker.nativo.utils;


import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.services.DonationService;
import com.igrowker.nativo.services.implementation.DonationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DonationSheduled {

    private final DonationRepository donationRepository;

    private final DonationServiceImpl donationService;

    @Scheduled(fixedRate = 60000)
    public void checkPendingDonations() {
        // Buscar todas las donaciones con estado PENDENT
        List<Donation> pendingDonations = donationRepository.findByStatus(TransactionStatus.PENDENT).orElseThrow(()-> new ResourceNotFoundException("No hay donaciones pendientes"));

        // Revisar cada donación pendiente
        for (Donation donation : pendingDonations) {
            // Verificar si ha pasado más de 1 minuto desde la creación
            if (LocalDateTime.now().isAfter(donation.getCreatedAt().plusMinutes(1))) {
                // Cambiar el estado a DENIED
                System.out.println("entro");
                donationService.returnAmount(donation.getAccountIdDonor(), donation.getAmount());
                donation.setStatus(TransactionStatus.DENIED);
                // Guardar el cambio en la base de datos
                donationRepository.save(donation);
            }
        }
    }
}
