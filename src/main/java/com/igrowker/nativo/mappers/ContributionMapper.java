package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.entities.Contribution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContributionMapper {
    Contribution requestDtoToContribution(RequestContributionDto requestContributionDto);

    @Mapping(source = "contribution.microcredit.id", target = "microcreditId")
    @Mapping(source = "contribution.microcredit.expirationDate", target = "expiredDateMicrocredit")
    @Mapping(source = "lenderFullname", target = "lenderFullname")
    @Mapping(source = "borrowerFullname", target = "borrowerFullname")
    ResponseContributionDto responseContributionDto(Contribution contribution, String lenderFullname, String borrowerFullname);

    List<ResponseContributionDto> contributionListToResponseRecordList(List<Contribution> contributionList);
}
