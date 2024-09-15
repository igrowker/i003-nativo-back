package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.mappers.DonationMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.services.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final DonationMapper donationMapper;
    private final AccountRepository accountRepository;

    @Override
    public ResponseDonationDtoTrue createDonationTrue(RequestDonationDto requestDonationDto) {

        if (requestDonationDto != null){

            // Validando cuenta de donador y beneficiario
            Account accountDonor = accountRepository.findById(requestDonationDto.accountIdDonor()).orElseThrow(() -> new RuntimeException("El id del donanto no existe"));
            Account accountBeneficiary = accountRepository.findById(requestDonationDto.accountIdBeneficiary()).orElseThrow(() -> new RuntimeException("El id del beneficiario no existe"));

            Donation donation =donationRepository.save(donationMapper.requestDtoToDonation(requestDonationDto));


            return new ResponseDonationDtoTrue(donation.getId(),
                    donation.getAmount(),
                    accountDonor.getId(),
                    accountDonor.getUser().getName(),
                    accountDonor.getUser().getSurname(),
                    accountBeneficiary.getId(),
                    accountBeneficiary.getUser().getName(),
                    accountBeneficiary.getUser().getSurname(),
                    donation.getCreatedAt(),
                    donation.getStatus().name());
        }

        return null;
    }

    @Override
    public ResponseDonationDtoFalse createDonationFalse(RequestDonationDto requestDonationDto) {
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
