package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.donation.RequestDonationConfirmationDto;
import com.igrowker.nativo.dtos.donation.RequestDonationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationConfirmationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationDto;
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

            return donationMapper.donationToResponseDto(donationRepository.save(donationMapper.requestDtoToDonation(requestDonationDto)));

        }


        return null;
    }

    @Override
    public ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto) {
        if (requestDonationConfirmationDto != null) {

            Donation donation = donationRepository.findById(requestDonationConfirmationDto.id())
                    .orElseThrow(() -> new RuntimeException("Donacion no existe"));

            donation.setStatus(requestDonationConfirmationDto.status());


            if (donation.getStatus() == TransactionStatus.ACCEPTED){
                return  donationMapper.donationToResponseConfirmationDto(donationRepository.save(donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto)));
            }else{

                return  donationMapper.donationToResponseConfirmationDto(donationRepository.save(donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto)));
            }

        }

        return null;
    }

}
