package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.RequestDonationDto;
import com.igrowker.nativo.dtos.ResponseDonationDto;

public interface DonationService {

    ResponseDonationDto createDonation(RequestDonationDto requestDonationDto);

}
