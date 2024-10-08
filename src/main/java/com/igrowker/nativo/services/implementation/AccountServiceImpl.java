package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.dtos.account.ResponseOtherAccountDto;
import com.igrowker.nativo.dtos.account.ResponseSelfAccountDto;
import com.igrowker.nativo.dtos.account.ResponseTransactionDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.AccountMapper;
import com.igrowker.nativo.repositories.*;
import com.igrowker.nativo.services.AccountService;
import com.igrowker.nativo.utils.DateFormatter;
import com.igrowker.nativo.validations.Validations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final Validations validations;
    private final PaymentRepository paymentRepository;
    private final ContributionRepository contributionRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final DateFormatter dateFormatter;

    @Override
    @Transactional
    public ResponseSelfAccountDto addAmount(AddAmountAccountDto addAmountAccountDto) {
        if (validations.isUserAccountMismatch(addAmountAccountDto.id())) {
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
        }
        Account account = accountRepository.findById(addAmountAccountDto.id())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        var previousAmount = account.getAmount();
        account.setAmount(previousAmount.add(addAmountAccountDto.amount()));
        Account savedAccount = accountRepository.save(account);
        return accountMapper.accountToResponseSelfDto(savedAccount);
    }

    @Override
    public ResponseSelfAccountDto readSelfAccount(String id) {
        if (validations.isUserAccountMismatch(id)) {
            throw new InvalidUserCredentialsException("La cuenta indicada no coincide con el usuario logueado en la aplicación");
        }
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return accountMapper.accountToResponseSelfDto(account);
    }

    @Override
    public ResponseOtherAccountDto readOtherAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return accountMapper.accountToResponseOtherDto(account);
    }

    @Override
    public List<ResponseTransactionDto> getAll() {
        var userAndAccount = validations.getAuthenticatedUserAndAccount();
        List<Payment> payments = paymentRepository.findPaymentsByAccount(userAndAccount.user.getAccountId());
        List<Donation> donations = donationRepository.findDonationsByAccount(userAndAccount.user.getAccountId());
        List<Contribution> contributions = contributionRepository.findContributionsByAccount(userAndAccount.user.getAccountId());
        List<ResponseTransactionDto> transactions = new ArrayList<>();

        for (Payment payment : payments) {
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    payment.getId(),
                    "Pago",
                    payment.getAmount(),
                    payment.getSenderName(),
                    payment.getSenderSurname(),
                    payment.getSenderAccount(),
                    payment.getReceiverName(),
                    payment.getReceiverSurname(),
                    payment.getReceiverAccount(),
                    payment.getTransactionDate(),
                    null,
                    payment.getTransactionStatus().toString()
            );
            transactions.add(dto);
        }

        for (Donation donation : donations) {
            User donor = getUserByAccountNumber(donation.getAccountIdDonor());
            User beneficiary = getUserByAccountNumber(donation.getAccountIdBeneficiary());
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    donation.getId(),
                    "Donación",
                    donation.getAmount(),
                    donation.getAnonymousDonation() ? "Anónimo" : donor.getName(),
                    donation.getAnonymousDonation() ? "" : donor.getSurname(),
                    donation.getAccountIdDonor(),
                    beneficiary.getName(),
                    beneficiary.getSurname(),
                    donation.getAccountIdBeneficiary(),
                    donation.getCreatedAt(),
                    donation.getUpdateAt(),
                    donation.getStatus().toString()
            );
            transactions.add(dto);
        }

        for (Contribution contribution : contributions) {
            User lender = getUserByAccountNumber(contribution.getLenderAccountId());
            User borrower = getUserByAccountNumber(contribution.getMicrocredit().getBorrowerAccountId());
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    contribution.getId(),
                    "Microcrédito",
                    contribution.getAmount(),
                    lender.getName(),
                    lender.getSurname(),
                    contribution.getLenderAccountId(),
                    borrower.getName(),
                    borrower.getSurname(),
                    contribution.getMicrocredit().getBorrowerAccountId(),
                    contribution.getCreatedDate(),
                    null, // Si no tienes un campo de endDate
                    contribution.getTransactionStatus().toString()
            );
            transactions.add(dto);

        }

        return transactions;
    }

    @Override
    public List<ResponseTransactionDto> getAllStatus(String status) {
        var userAndAccount = validations.getAuthenticatedUserAndAccount();
        var enumStatus = validations.statusConvert(status);
        List<Payment> payments = paymentRepository.findPaymentsByStatus(userAndAccount.user.getAccountId(), enumStatus);
        List<Donation> donations = donationRepository.findDonationsByStatus(userAndAccount.user.getAccountId(), enumStatus);
        List<Contribution> contributions = contributionRepository.findContributionsByStatus(userAndAccount.user.getAccountId(), enumStatus);
        List<ResponseTransactionDto> transactions = new ArrayList<>();

        for (Payment payment : payments) {
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    payment.getId(),
                    "Pago",
                    payment.getAmount(),
                    payment.getSenderName(),
                    payment.getSenderSurname(),
                    payment.getSenderAccount(),
                    payment.getReceiverName(),
                    payment.getReceiverSurname(),
                    payment.getReceiverAccount(),
                    payment.getTransactionDate(),
                    null,
                    payment.getTransactionStatus().toString()
            );
            transactions.add(dto);
        }

        for (Donation donation : donations) {
            User donor = getUserByAccountNumber(donation.getAccountIdDonor());
            User beneficiary = getUserByAccountNumber(donation.getAccountIdBeneficiary());
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    donation.getId(),
                    "Donación",
                    donation.getAmount(),
                    donation.getAnonymousDonation() ? "Anónimo" : donor.getName(),
                    donation.getAnonymousDonation() ? "" : donor.getSurname(),
                    donation.getAccountIdDonor(),
                    beneficiary.getName(),
                    beneficiary.getSurname(),
                    donation.getAccountIdBeneficiary(),
                    donation.getCreatedAt(),
                    donation.getUpdateAt(),
                    donation.getStatus().toString()
            );
            transactions.add(dto);
        }

        for (Contribution contribution : contributions) {
            User lender = getUserByAccountNumber(contribution.getLenderAccountId());
            User borrower = getUserByAccountNumber(contribution.getMicrocredit().getBorrowerAccountId());
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    contribution.getId(),
                    "Microcrédito",
                    contribution.getAmount(),
                    lender.getName(),
                    lender.getSurname(),
                    contribution.getLenderAccountId(),
                    borrower.getName(),
                    borrower.getSurname(),
                    contribution.getMicrocredit().getBorrowerAccountId(),
                    contribution.getCreatedDate(),
                    null, // Si no tienes un campo de endDate
                    contribution.getTransactionStatus().toString()
            );
            transactions.add(dto);

        }

        return transactions;
    }

    @Override
    public List<ResponseTransactionDto> getAllBetweenDates(String fromDate, String toDate) {
        var userAndAccount = validations.getAuthenticatedUserAndAccount();
        List<LocalDateTime> dates = dateFormatter.getDateFromString(fromDate, toDate);
        List<Payment> payments = paymentRepository.findPaymentsBetweenDates(userAndAccount.user.getAccountId(), dates.get(0), dates.get(1));
        List<Donation> donations = donationRepository.findDonationsByDateRange(userAndAccount.user.getAccountId(), dates.get(0), dates.get(1));
        List<Contribution> contributions = contributionRepository.findContributionsByDateRange(userAndAccount.user.getAccountId(), dates.get(0), dates.get(1));
        List<ResponseTransactionDto> transactions = new ArrayList<>();

        for (Payment payment : payments) {
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    payment.getId(),
                    "Pago",
                    payment.getAmount(),
                    payment.getSenderName(),
                    payment.getSenderSurname(),
                    payment.getSenderAccount(),
                    payment.getReceiverName(),
                    payment.getReceiverSurname(),
                    payment.getReceiverAccount(),
                    payment.getTransactionDate(),
                    null,
                    payment.getTransactionStatus().toString()
            );
            transactions.add(dto);
        }

        for (Donation donation : donations) {
            User donor = getUserByAccountNumber(donation.getAccountIdDonor());
            User beneficiary = getUserByAccountNumber(donation.getAccountIdBeneficiary());
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    donation.getId(),
                    "Donación",
                    donation.getAmount(),
                    donation.getAnonymousDonation() ? "Anónimo" : donor.getName(),
                    donation.getAnonymousDonation() ? "" : donor.getSurname(),
                    donation.getAccountIdDonor(),
                    beneficiary.getName(),
                    beneficiary.getSurname(),
                    donation.getAccountIdBeneficiary(),
                    donation.getCreatedAt(),
                    donation.getUpdateAt(),
                    donation.getStatus().toString()
            );
            transactions.add(dto);
        }

        for (Contribution contribution : contributions) {
            User lender = getUserByAccountNumber(contribution.getLenderAccountId());
            User borrower = getUserByAccountNumber(contribution.getMicrocredit().getBorrowerAccountId());
            ResponseTransactionDto dto = new ResponseTransactionDto(
                    contribution.getId(),
                    "Microcrédito",
                    contribution.getAmount(),
                    lender.getName(),
                    lender.getSurname(),
                    contribution.getLenderAccountId(),
                    borrower.getName(),
                    borrower.getSurname(),
                    contribution.getMicrocredit().getBorrowerAccountId(),
                    contribution.getCreatedDate(),
                    null, // Si no tienes un campo de endDate
                    contribution.getTransactionStatus().toString()
            );
            transactions.add(dto);

        }

        return transactions;
    }

    private User getUserByAccountNumber(String accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        User user = userRepository.findById(account.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user;
        //Wiii
    }

}
