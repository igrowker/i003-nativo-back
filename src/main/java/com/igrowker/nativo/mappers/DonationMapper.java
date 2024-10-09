package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DonationMapper {


    //CREATE
    Donation requestDtoToDonation(RequestDonationDto requestDonationDto);

    ResponseDonationDtoTrue donationToResponseDtoTrue(Donation donation);

    @Mapping(source = "nameBenficiary", target = "beneficiaryName")
    @Mapping(source = "nameLastBenficiary", target = "beneficiaryLastName")
    ResponseDonationDtoFalse donationToResponseDtoFalse(Donation donation, String nameBenficiary, String nameLastBenficiary);

    //CONFIRMATION
    Donation requestConfirmationDtoToDonation(RequestDonationConfirmationDto requestDonationDto);
    ResponseDonationConfirmationDto donationToResponseConfirmationDto(Donation donation);


    //RECORD
    List<ResponseDonationRecord> listDonationToListResponseDonationRecord(List<ResponseDonationRecord> donationsList);
}
