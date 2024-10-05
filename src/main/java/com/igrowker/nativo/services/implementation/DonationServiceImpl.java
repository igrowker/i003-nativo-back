package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceAlreadyExistsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.DonationMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.DonationService;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.validations.Validations;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        Account accountDonor = accountRepository.findById(requestDonationDto.accountIdDonor()).orElseThrow(() -> new ResourceNotFoundException("El id de la cuenta donante no existe"));

        if (validations.isUserAccountMismatch(requestDonationDto.accountIdDonor())) {
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
        }

        if (validations.validateTransactionUserFunds(requestDonationDto.amount())) {
            Account accountBeneficiary = accountRepository.findById(requestDonationDto.accountIdBeneficiary()).orElseThrow(() -> new ResourceNotFoundException("El id de la cuenta beneficiario no existe"));

            User donor = userRepository.findById(accountDonor.getUserId()).orElseThrow(() -> new ResourceNotFoundException("El id del usuario donante no existe"));
            User beneficiary = userRepository.findById(accountBeneficiary.getUserId()).orElseThrow(() -> new ResourceNotFoundException("El id del usuario beneficiario no existe"));

            Donation donation = donationRepository.save(donationMapper.requestDtoToDonation(requestDonationDto));

            accountDonor.setReservedAmount(accountDonor.getReservedAmount().add(requestDonationDto.amount()));
            accountRepository.save(accountDonor);

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
            throw new InsufficientFundsException("Tu cuenta no tiene suficientes fondos");
        }

    }

    @Override
    public ResponseDonationDtoFalse createDonationFalse(RequestDonationDto requestDonationDto) {

        // Validando cuenta de donador y beneficiario

        if (validations.isUserAccountMismatch(requestDonationDto.accountIdDonor())) {
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
        }

        if (validations.validateTransactionUserFunds(requestDonationDto.amount())){
            Account donorAccount = accountRepository.findById(requestDonationDto.accountIdDonor()).orElseThrow(() -> new ResourceNotFoundException("El id de la cuenta donante no existe"));
            donorAccount.setReservedAmount(donorAccount.getReservedAmount().add(requestDonationDto.amount()));
            accountRepository.save(donorAccount);
            return donationMapper.donationToResponseDtoFalse(donationRepository.save(donationMapper.requestDtoToDonation(requestDonationDto)));
        }else{
            throw new InsufficientFundsException("Tu cuenta no tiene suficientes fondos.");
        }
    }


    @Override
    public ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto) {

        Donation donation = donationRepository.findById(requestDonationConfirmationDto.id())
                .orElseThrow(() -> new ResourceNotFoundException("El id de la donacion no existe"));

        if (donation.getStatus() == TransactionStatus.PENDING){
            if (validations.isUserAccountMismatch(donation.getAccountIdBeneficiary())) {
                throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
            }

            // Recibo el request y lo convierto a donation
            Donation donation1 = donationMapper.requestConfirmationDtoToDonation(requestDonationConfirmationDto);

            donation1.setAnonymousDonation(donation.getAnonymousDonation());
            donation1.setAmount(donation.getAmount());
            donation1.setCreatedAt(donation.getCreatedAt());

            returnAmount(donation1.getAccountIdDonor(), donation.getAmount());

            if (donation1.getStatus() == TransactionStatus.ACCEPTED) {
                // Se agrega el monto al beneficiario y se descuenta de la cuenta de reserva del donador
                generalTransactions.updateBalances(
                        donation1.getAccountIdDonor(),
                        donation1.getAccountIdBeneficiary(),
                        donation1.getAmount());
            }
            // Se agrega el monto al donantea
            return donationMapper.donationToResponseConfirmationDto(donationRepository.save(donation1));


        }

        throw new ResourceAlreadyExistsException("Esta donacion ya fue finalizada");
    }


    @Override
    public List<ResponseDonationRecord> recordDonationDonor(String idDonorAccount) {

        Account account =  accountRepository.findById(idDonorAccount).orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));

        List<Donation> donationList = donationRepository.findAllByAccountIdDonor(account.getId()).orElseThrow(() -> new ResourceNotFoundException("No hay donacion que tenga ese id de cuenta"));

        if (donationList.isEmpty()){
            throw  new ResourceNotFoundException("No hay donaciones dadas");
        }else {
            return donationMapper.listDonationToListResponseDonationRecord(donationList.stream()
                    .sorted(Comparator.comparing(Donation::getCreatedAt).reversed())
                    .collect(Collectors.toList()));
        }
    }


    @Override
    public List<ResponseDonationRecord> recordDonationBeneficiary(String idBeneficiaryAccount) {

        Account account =  accountRepository.findById(idBeneficiaryAccount).orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));

        List<Donation> donationList = donationRepository.findAllByAccountIdBeneficiary(account.getId()).orElseThrow(() -> new ResourceNotFoundException("No hay donacion que tenga ese id de cuenta"));

        if (donationList.isEmpty()){
            throw  new ResourceNotFoundException("No hay donaciones recibidas");
        } else {
            return donationMapper.listDonationToListResponseDonationRecord(donationList.stream()
                    .sorted(Comparator.comparing(Donation::getCreatedAt).reversed())
                    .collect(Collectors.toList()));
        }
    }


    public void returnAmount(String id, BigDecimal amount){
        Account donorAccount = accountRepository.findById(id).orElseThrow(() -> new InsufficientFundsException("La cuenta del donador no existe"));
        donorAccount.setReservedAmount(donorAccount.getReservedAmount().subtract(amount));
        accountRepository.save(donorAccount);
    }
}