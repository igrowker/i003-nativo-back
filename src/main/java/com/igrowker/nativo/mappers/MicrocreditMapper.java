package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.Microcredit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MicrocreditMapper {
    Microcredit requestDtoToMicrocredit(RequestMicrocreditDto requestMicrocreditDto);

    ResponseMicrocreditDto responseDtoToMicrocredit(Microcredit microcredit);

    @Mapping(target = "contributions", source = "contributions")
    ResponseMicrocreditGetDto responseMicrocreditGet(Microcredit microcredit, List<ResponseContributionDto> contributions);

    @Mapping(source = "contribution.microcredit.id", target = "microcreditId")
    @Mapping(source = "contribution.microcredit.expirationDate", target = "expiredDateMicrocredit")
    List<ResponseMicrocreditGetDto> microcreditListToResponseRecordList(List<Microcredit> microcreditList);
}
