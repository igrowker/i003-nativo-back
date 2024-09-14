package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.donation.RequestDonationConfirmationDto;
import com.igrowker.nativo.dtos.donation.RequestDonationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationConfirmationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationDtoTrue;
import com.igrowker.nativo.entities.Donation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DonationMapper {

    //CREATE
    Donation requestDtoToDonation(RequestDonationDto requestDonationDto);
    ResponseDonationDtoTrue donationToResponseDto(Donation donation);

    //CONFIRMATION
    Donation requestConfirmationDtoToDonation(RequestDonationConfirmationDto requestDonationDto);
    ResponseDonationConfirmationDto donationToResponseConfirmationDto(Donation donation);
}
