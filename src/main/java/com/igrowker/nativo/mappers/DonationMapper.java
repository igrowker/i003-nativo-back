package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.RequestDonationConfirmationDto;
import com.igrowker.nativo.dtos.RequestDonationDto;
import com.igrowker.nativo.entities.Donation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DonationMapper {

    //CREATE
    Donation requestDtoToDonation(RequestDonationDto requestDonationDto);
    RequestDonationDto donationToRequestDto(Donation donation);

    //CONFIRMATION
    Donation requestConfirmationDtoToDonation(RequestDonationConfirmationDto requestDonationDto);
    RequestDonationConfirmationDto donationToRequestConfirmationDto(Donation donation);
}
