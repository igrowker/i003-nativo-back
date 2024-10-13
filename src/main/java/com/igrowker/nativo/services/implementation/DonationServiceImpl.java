package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.account.ResponseTransactionDto;
import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceAlreadyExistsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.DonationMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.DonationService;
import com.igrowker.nativo.utils.DateFormatter;
import com.igrowker.nativo.utils.GeneralTransactions;
import com.igrowker.nativo.validations.Validations;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final DateFormatter dateFormatter;

    @Override
    public ResponseDonationDtoTrue createDonationTrue(RequestDonationDto requestDonationDto) {

        // Validando cuenta de donador y beneficiario
        var donor = validations.getAuthenticatedUserAndAccount();


        if (validations.validateTransactionUserFunds(requestDonationDto.amount())) {
            Account accountBeneficiary = accountRepository.findAccountByNumberAccount(requestDonationDto.numberAccountBeneficiary()).orElseThrow(() -> new ResourceNotFoundException("El numero de cuenta beneficiario no existe"));
            // IMPORTANTE AGREGUE ESTO
            if (donor.account.getId().equals(accountBeneficiary.getId())){
                throw new IllegalArgumentException("No puedes donarte a ti mismo");
            }

            User beneficiary = userRepository.findById(accountBeneficiary.getUserId()).orElseThrow(() -> new ResourceNotFoundException("El id del usuario beneficiario no existe"));

            Donation donation = returnDonation(donor.account.getId(), accountBeneficiary.getId(), requestDonationDto);

            donor.account.setReservedAmount(donor.account.getReservedAmount().add(requestDonationDto.amount()));
            accountRepository.save(donor.account);

            return new ResponseDonationDtoTrue(
                    donation.getId(),
                    donation.getAmount(),
                    donor.user.getName(),
                    donor.user.getSurname(),
                    accountBeneficiary.getAccountNumber(),
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

        var donor = validations.getAuthenticatedUserAndAccount();

        if (validations.validateTransactionUserFunds(requestDonationDto.amount())){
            Account accountBeneficiary = accountRepository.findAccountByNumberAccount(requestDonationDto.numberAccountBeneficiary()).orElseThrow(() -> new ResourceNotFoundException("El numero de cuenta beneficiario no existe"));
            // IMPORTANTE AGREGUE ESTO
            if (donor.account.getId().equals(accountBeneficiary.getId())){
                throw new IllegalArgumentException("No puedes donarte a ti mismo");
            }

            User beneficiaryAccount = userRepository.findById(accountBeneficiary.getUserId()).orElseThrow(() -> new ResourceNotFoundException("El id de usuario beneficiario no existe"));
            donor.account.setReservedAmount(donor.account.getReservedAmount().add(requestDonationDto.amount()));
            accountRepository.save(donor.account);
            Donation donation = returnDonation(donor.account.getId(), accountBeneficiary.getId(), requestDonationDto);;

            return donationMapper.donationToResponseDtoFalse(donation, beneficiaryAccount.getName(), beneficiaryAccount.getSurname(), accountBeneficiary.getAccountNumber());
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
            donation1.setAccountIdDonor(donation.getAccountIdDonor());
            donation1.setAccountIdBeneficiary(donation.getAccountIdBeneficiary());

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

        if (validations.isUserAccountMismatch(idDonorAccount)) {
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
        }

        var donor = validations.getAuthenticatedUserAndAccount();
        List<Donation> donationList = donationRepository.findAllByAccountIdDonor(donor.account.getId()).orElseThrow(() -> new ResourceNotFoundException("No hay donacion que tenga ese id de cuenta"));

        List<ResponseDonationRecord> donationsb = new ArrayList<>();

        for (Donation donation : donationList) {

                Account accountBeneficiary = accountRepository.findById(donation.getAccountIdBeneficiary()).orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
                User userBeneficiary = userRepository.findById(accountBeneficiary.getUserId()).orElseThrow(() -> new ResourceNotFoundException("La cuenta usuario no existe"));
                ResponseDonationRecord dto = new ResponseDonationRecord(
                        donation.getId(),
                        donation.getAmount(),
                        donation.getAnonymousDonation() ? "Anónimo" : donor.user.getName(),
                        donation.getAnonymousDonation() ? "." : donor.user.getSurname(),
                        donation.getAccountIdDonor(),
                        userBeneficiary.getName(),
                        userBeneficiary.getSurname(),
                        donation.getAccountIdBeneficiary(),
                        donation.getStatus(),
                        donation.getCreatedAt(),
                        donation.getUpdateAt()
                );
                donationsb.add(dto);
            }

        if (donationList.isEmpty()){
            throw  new ResourceNotFoundException("No hay donaciones dadas");
        }else {
            return donationMapper.listDonationToListResponseDonationRecord(donationsb.stream()
                    .sorted(Comparator.comparing(ResponseDonationRecord::updateAt).reversed())
                    .collect(Collectors.toList()));
        }
    }


    @Override
    public List<ResponseDonationRecord> recordDonationBeneficiary(String idBeneficiaryAccount) {

        if (validations.isUserAccountMismatch(idBeneficiaryAccount)) {
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
        }

        var beneficiary = validations.getAuthenticatedUserAndAccount();

        List<Donation> donationList = donationRepository.findAllByAccountIdDonor(beneficiary.account.getId()).orElseThrow(() -> new ResourceNotFoundException("No hay donacion que tenga ese id de cuenta"));

        List<ResponseDonationRecord> donationsb = new ArrayList<>();

        for (Donation donation : donationList) {

            Account accountDonor = accountRepository.findById(donation.getAccountIdDonor()).orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
            User userDonor = userRepository.findById(accountDonor.getUserId()).orElseThrow(() -> new ResourceNotFoundException("La cuenta usuario no existe"));
            ResponseDonationRecord dto = new ResponseDonationRecord(
                    donation.getId(),
                    donation.getAmount(),
                    donation.getAnonymousDonation() ? "Anónimo" : userDonor.getName(),
                    donation.getAnonymousDonation() ? "." : userDonor.getSurname(),
                    donation.getAccountIdDonor(),
                    beneficiary.user.getName(),
                    beneficiary.user.getSurname(),
                    donation.getAccountIdBeneficiary(),
                    donation.getStatus(),
                    donation.getCreatedAt(),
                    donation.getUpdateAt()
            );
            donationsb.add(dto);
        }

        if (donationList.isEmpty()){
            throw  new ResourceNotFoundException("No hay donaciones recibidas");
        }else {
            return donationMapper.listDonationToListResponseDonationRecord(donationsb.stream()
                    .sorted(Comparator.comparing(ResponseDonationRecord::updateAt).reversed())
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public List<ResponseDonationRecord> getDonationBtBetweenDatesOrStatus(String fromDate, String toDate, String status) {
        Validations.UserAccountPair accountAndUser = validations.getAuthenticatedUserAndAccount();

        if (status == null && (fromDate == null || toDate == null)) {
            throw new ResourceNotFoundException("Se debe de ingresar las fechas de inicio y fin o un status");
        }

        List<Donation> donations;

        if (fromDate == null || toDate == null) {
            TransactionStatus transactionStatus = validations.statusConvert(status);
            donations = donationRepository.findDonationsByStatus(accountAndUser.account.getId(), transactionStatus);
        } else {
            List<LocalDateTime> dateTimes = dateFormatter.getDateFromString(fromDate, toDate);
            LocalDateTime startDate = dateTimes.get(0);
            LocalDateTime endDate = dateTimes.get(1);
            donations = donationRepository.findDonationsByDateRange(accountAndUser.account.getId(), startDate, endDate);
        }

        donations.sort(Comparator.comparing(Donation::getUpdateAt).reversed());

        return donationMapper.listDonationToListResponseDonationRecordTwo(donations);
    }


    public void returnAmount(String id, BigDecimal amount){
        Account donorAccount = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La cuenta del donador no existe"));
        donorAccount.setReservedAmount(donorAccount.getReservedAmount().subtract(amount));
        accountRepository.save(donorAccount);
    }

    public Donation returnDonation(String idDonor, String idBeneficiary, RequestDonationDto requestDonationDto){
        Donation donation = donationMapper.requestDtoToDonation(requestDonationDto);
        donation.setAccountIdDonor(idDonor);
        donation.setAccountIdBeneficiary(idBeneficiary);
        return donationRepository.save(donation);
    }
}