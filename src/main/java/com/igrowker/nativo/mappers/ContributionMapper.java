package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionGetDto;
import com.igrowker.nativo.entities.Contribution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface ContributionMapper {
    //Crear
    Contribution requestDtoToContribution(RequestContributionDto requestContributionDto);

    @Mapping(target = "lenderFullname", source = "lenderFullname")
    @Mapping(target = "borrowerFullname", source = "borrowerFullname")
    ResponseContributionDto responseContributionDto(Contribution contribution, String lenderFullname,
                                                    String borrowerFullname, String microcreditId,
                                                    LocalDate expiredDateMicrocredit);

    @Mapping(target = "lenderFullname", source = "lenderFullname")
    @Mapping(target = "borrowerFullname", source = "borrowerFullname")
    ResponseContributionGetDto responseContributionGetDto(Contribution contribution, String lenderFullname,
                                                          String borrowerFullname, String microcreditId,
                                                          LocalDate expiredDateMicrocredit);

    //Agregar fecha de vencimiento del microcredito
}
