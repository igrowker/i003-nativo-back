package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.RequestDonationDto;
import com.igrowker.nativo.dtos.ResponseDonationDto;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.mappers.DonationMapper;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.services.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final DonationMapper donationMapper;

    @Override
    public ResponseDonationDto createDonation(RequestDonationDto requestDonationDto) {

        if (requestDonationDto != null){
            Donation donation = new Donation();
            donation.setAmount(requestDonationDto.amount());
            donation.setDonor(requestDonationDto.donor());
            donation.setBeneficiary(requestDonationDto.beneficiary());
            Donation donation1 = donationRepository.save(donation);
            // return ResponseDonationDto(TransactionStatus.ACCEPTED.name(), Optional.of(donationMapper.requestDtoToDonation()));
        }

        return null;
    }

}
