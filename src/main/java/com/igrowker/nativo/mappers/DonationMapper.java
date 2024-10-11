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

    @Mapping(source = "nameBeneficiary", target = "beneficiaryName")
    @Mapping(source = "nameLastBeneficiary", target = "beneficiaryLastName")
    @Mapping(source = "numberAccountBeneficiary", target = "beneficiaryAccountNumber")
    ResponseDonationDtoFalse donationToResponseDtoFalse(Donation donation, String nameBeneficiary, String nameLastBeneficiary, Long numberAccountBeneficiary);

    //CONFIRMATION
    Donation requestConfirmationDtoToDonation(RequestDonationConfirmationDto requestDonationDto);
    ResponseDonationConfirmationDto donationToResponseConfirmationDto(Donation donation);


    //RECORD
    List<ResponseDonationRecord> listDonationToListResponseDonationRecord(List<ResponseDonationRecord> donationsList);
}
