package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.mappers.DonationMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final DonationMapper donationMapper;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseDonationDtoTrue createDonationTrue(RequestDonationDto requestDonationDto) {

            // Validando cuenta de donador y beneficiario
            Account accountDonor = accountRepository.findById(requestDonationDto.accountIdDonor()).orElseThrow(() -> new RuntimeException("El id del donante no existe"));
            Account accountBeneficiary = accountRepository.findById(requestDonationDto.accountIdBeneficiary()).orElseThrow(() -> new RuntimeException("El id del beneficiario no existe"));

            User donor = userRepository.findById(accountDonor.getUserId()).orElseThrow(() -> new RuntimeException("El donante no existe"));
            User beneficiary = userRepository.findById(accountBeneficiary.getUserId()).orElseThrow(() -> new RuntimeException("El donante no existe"));

            Donation donation =donationRepository.save(donationMapper.requestDtoToDonation(requestDonationDto));

            return new ResponseDonationDtoTrue(
                    donation.getId(),
                    donation.getAmount(),
                    accountDonor.getId(),
                    donor.getName(),
                    donor.getSurname(),
                    accountBeneficiary.getId(),
                    beneficiary.getName(),
                    beneficiary.getSurname(),
                    donation.getCreatedAt(),
                    donation.getStatus().name()
            );
    }

    @Override
    public ResponseDonationDtoFalse createDonationFalse(RequestDonationDto requestDonationDto) {

        // Validando cuenta de donador y beneficiario
        Account accountDonor = accountRepository.findById(requestDonationDto.accountIdDonor()).orElseThrow(() -> new RuntimeException("El id del donante no existe"));
        Account accountBeneficiary = accountRepository.findById(requestDonationDto.accountIdBeneficiary()).orElseThrow(() -> new RuntimeException("El id del beneficiario no existe"));

        User donor = userRepository.findById(accountDonor.getUserId()).orElseThrow(() -> new RuntimeException("El donante no existe"));
        User beneficiary = userRepository.findById(accountBeneficiary.getUserId()).orElseThrow(() -> new RuntimeException("El donante no existe"));

        return donationMapper.donationToResponseDtoFalse(donationRepository.save(donationMapper.requestDtoToDonation(requestDonationDto)));

    }

    @Override
    public ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto) {
        if (requestDonationConfirmationDto != null) {

            Donation donation = donationRepository.findById(requestDonationConfirmationDto.id())
                    .orElseThrow(() -> new RuntimeException("Donacion no existe"));

            donation.setStatus(requestDonationConfirmationDto.status());


            if (donation.getStatus() == TransactionStatus.ACCEPTED){
                // Se agrega el monto al beneficiario
                return  donationMapper.donationToResponseConfirmationDto(donationRepository.save(donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto)));
            }else{
                // Se agrega el monto al donante
                return  donationMapper.donationToResponseConfirmationDto(donationRepository.save(donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto)));
            }

        }

        return null;
    }

}
