package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.entities.Contribution;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContributionMapper {
    //Crear
    Contribution requestDtoToContribution(RequestContributionDto requestContributionDto);
    ResponseContributionDto responseContributionDto(Contribution contribution);
}
