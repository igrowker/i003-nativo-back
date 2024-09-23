package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditPaymentDto;
import com.igrowker.nativo.entities.Microcredit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;;

@Mapper(componentModel = "spring")
public interface MicrocreditMapper {
    //Crear
    Microcredit requestDtoToMicrocredit(RequestMicrocreditDto requestMicrocreditDto);

    ResponseMicrocreditDto responseDtoToMicrocredit(Microcredit microcredit);

    //Get
    @Mapping(target = "contributions", source = "contributions")
    @Mapping(target = "borrowerAccountId", source = "borrowerAccountId")
    @Mapping(target = "remainingAmount", source = "remainingAmount")
    ResponseMicrocreditGetDto responseMicrocreditGet(Microcredit microcredit);

    ResponseMicrocreditPaymentDto responseMicrocreditPaymentDto(Microcredit microcredit);
}
