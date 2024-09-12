package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.RequestDonationDto;
import com.igrowker.nativo.entities.Donation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DonationMapper {
    Donation requestDtoToDonation(RequestDonationDto requestDonationDto);
}
