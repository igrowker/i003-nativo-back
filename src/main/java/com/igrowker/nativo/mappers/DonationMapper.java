package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DonationMapper {


    User user= new User();

    //CREATE
    Donation requestDtoToDonation(RequestDonationDto requestDonationDto);

    ResponseDonationDtoTrue donationToResponseDtoTrue(Donation donation);

    ResponseDonationDtoFalse donationToResponseDtoFalse(Donation donation);

    //CONFIRMATION
    Donation requestConfirmationDtoToDonation(RequestDonationConfirmationDto requestDonationDto);
    ResponseDonationConfirmationDto donationToResponseConfirmationDto(Donation donation);
}
