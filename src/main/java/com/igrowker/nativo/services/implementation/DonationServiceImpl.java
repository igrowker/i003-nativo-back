package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.RequestDonationConfirmationDto;
import com.igrowker.nativo.dtos.RequestDonationDto;
import com.igrowker.nativo.dtos.ResponseDonationConfirmationDto;
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

            Donation donation = donationRepository.save(donationMapper.requestDtoToDonation(requestDonationDto));

            return new ResponseDonationDto(TransactionStatus.PENDENT.name(), Optional.of(donationMapper.donationToRequestDto(donation)));
        }
        return null;
    }

    @Override
    public ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto) {
        if (requestDonationConfirmationDto != null) {

            Donation donation = donationRepository.save(donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto));

            return new ResponseDonationConfirmationDto(Optional.of(donationMapper.donationToRequestConfirmationDto(donation)));
        }
        return null;
    }

}
