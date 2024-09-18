package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.DonationMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.DonationService;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.validations.Validations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final DonationMapper donationMapper;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private final GeneralTransactions generalTransactions;
    private final Validations validations;

    @Override
    public ResponseDonationDtoTrue createDonationTrue(RequestDonationDto requestDonationDto) {

            // Validando cuenta de donador y beneficiario
            Account accountDonor = accountRepository.findById(requestDonationDto.accountIdDonor()).orElseThrow(() -> new InsufficientFundsException("El id de la cuenta donante no existe"));

            if(validations.validateTransactionUserFunds(accountDonor.getAmount())){
                Account accountBeneficiary = accountRepository.findById(requestDonationDto.accountIdBeneficiary()).orElseThrow(() -> new InsufficientFundsException("El id de la cuenta beneficiario no existe"));

                User donor = userRepository.findById(accountDonor.getUserId()).orElseThrow(() -> new InsufficientFundsException("El id del usuario donante no existe"));
                User beneficiary = userRepository.findById(accountBeneficiary.getUserId()).orElseThrow(() -> new InsufficientFundsException("El id del usuario beneficiario no existe"));

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
            }else{
                throw new InsufficientFundsException("Tu cuenta no tiene suficientes fondos.");
            }

    }

    @Override
    public ResponseDonationDtoFalse createDonationFalse(RequestDonationDto requestDonationDto) {

        Account accountDonor = accountRepository.findById(requestDonationDto.accountIdDonor()).orElseThrow(() -> new InsufficientFundsException("El id de la cuenta donante no existe"));

        // Validando cuenta de donador y beneficiario
      if (validations.isUserAccountMismatch(requestDonationDto.accountIdBeneficiary())){
          if (validations.validateTransactionUserFunds(accountDonor.getAmount())){
              return donationMapper.donationToResponseDtoFalse(donationRepository.save(donationMapper.requestDtoToDonation(requestDonationDto)));
          }else{
              throw new InsufficientFundsException("Tu cuenta no tiene suficientes fondos.");
          }
        }else{
            throw new InsufficientFundsException("El id de la cuenta beneficiario no existe");
        }


    }

    @Override
    public ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto) {


        Donation donation = donationRepository.findById(requestDonationConfirmationDto.id())
                .orElseThrow(() -> new InsufficientFundsException("El id de la donacion no existe"));

        Donation donation1 = donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto);

        if (validations.isUserAccountMismatch(requestDonationConfirmationDto.accountIdDonor()) && validations.isUserAccountMismatch(requestDonationConfirmationDto.accountIdBeneficiary())) {
                donation1.setAnonymousDonation(donation.getAnonymousDonation());
                donation1.setCreatedAt(donation.getCreatedAt());

                if (donation.getStatus() == TransactionStatus.ACCEPTED) {
                    // Se agrega el monto al beneficiario
                    generalTransactions.updateBalances(
                            requestDonationConfirmationDto.accountIdDonor(),
                            requestDonationConfirmationDto.accountIdBeneficiary(),
                            requestDonationConfirmationDto.amount());

                    return donationMapper.donationToResponseConfirmationDto(donationRepository.save(donation1));
                } else {
                    // Se agrega el monto al donante
                    return donationMapper.donationToResponseConfirmationDto(donationRepository.save(donation1));
                }


        }else{
            throw new InsufficientFundsException("La cuenta seleccionada no existe.");
        }
    }



    @Override
    public List<ResponseDonationRecordBeneficiary> recordDonationDonor(String idAccount) {

        //Validar si la cuenta existe
        Account account =  accountRepository.findById(idAccount).orElseThrow(() -> new InsufficientFundsException("La cuenta no existe"));

        // Obteniedo listado
        //validar si la lista esta vacia, en el caso comunicarlo

        List<Donation> donationList = donationRepository.findAllByAccountIdDonor(account.getId()).orElseThrow(() -> new InsufficientFundsException("No hay donacion que tenga ese id de cuenta"));

        if (donationList.isEmpty()){
            throw  new ResourceNotFoundException("No hay donaciones dadas");
        }else {
            return donationMapper.listDonationToListResponseDonationRecord(donationList);
        }


    }


    @Override
    public List<ResponseDonationRecordBeneficiary> recordDonationBeneficiary(String idAccount) {

        Account account =  accountRepository.findById(idAccount).orElseThrow(() -> new InsufficientFundsException("La cuenta no existe"));

        List<Donation> donationList = donationRepository.findAllByAccountIdBeneficiary(account.getId()).orElseThrow(() -> new InsufficientFundsException("No hay donacion que tenga ese id de cuenta"));

        if (donationList.isEmpty()){
            throw  new ResourceNotFoundException("No hay donaciones recibidas");
        } else {

            donationList.stream()
                    .sorted(Comparator.comparing(Donation::getCreatedAt))
                    .collect(Collectors.toList());

            return donationMapper.listDonationToListResponseDonationRecord(donationList);
        }
    }

}
