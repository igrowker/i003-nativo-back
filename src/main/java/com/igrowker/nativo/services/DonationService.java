package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.donation.*;

import java.util.List;

public interface DonationService {

    ResponseDonationDtoTrue createDonationTrue(RequestDonationDto requestDonationDto);
    ResponseDonationDtoFalse createDonationFalse(RequestDonationDto requestDonationDto);

    ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto);

    List<ResponseDonationRecordBeneficiary> recordDonationDonor(String idAccount);

    List<ResponseDonationRecordBeneficiary> recordDonationBeneficiary(String idAccount);
}
