package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.RequestDonationConfirmationDto;
import com.igrowker.nativo.dtos.RequestDonationDto;
import com.igrowker.nativo.dtos.ResponseDonationConfirmationDto;
import com.igrowker.nativo.dtos.ResponseDonationDto;

public interface DonationService {

    ResponseDonationDto createDonation(RequestDonationDto requestDonationDto);

    ResponseDonationConfirmationDto confirmationDonation(RequestDonationConfirmationDto requestDonationConfirmationDto);

}
