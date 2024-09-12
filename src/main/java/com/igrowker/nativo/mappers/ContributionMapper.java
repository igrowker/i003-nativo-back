package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.RequestContributionDto;
import com.igrowker.nativo.entities.Contribution;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContributionMapper {
    Contribution requestDtoToContribution(RequestContributionDto requestContributionDto);
}
