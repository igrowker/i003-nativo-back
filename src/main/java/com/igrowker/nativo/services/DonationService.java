package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.donation.*;

import java.util.List;

public interface DonationService {

    ResponseDonationDtoTrue createDonationTrue(RequestDonationDto requestDonationDto);
    ResponseDonationDtoFalse createDonationFalse(RequestDonationDto requestDonationDto);

    ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto);

    List<ResponseDonationRecord> recordDonationDonor(String idAccount);

    List<ResponseDonationRecord> recordDonationBeneficiary(String idAccount);

    List<ResponseDonationRecord> getDonationBtBetweenDatesOrStatus(String fromDate, String toDate, String status);
}
