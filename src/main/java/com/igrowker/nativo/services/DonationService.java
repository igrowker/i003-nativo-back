package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.donation.*;

public interface DonationService {

    ResponseDonationDtoTrue createDonationTrue(RequestDonationDto requestDonationDto);
    ResponseDonationDtoFalse createDonationFalse(RequestDonationDto requestDonationDto);

    ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto);

}
