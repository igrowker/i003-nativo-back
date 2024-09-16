package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.Microcredit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface MicrocreditMapper {
    //Crear
    Microcredit requestDtoToMicrocredit(RequestMicrocreditDto requestMicrocreditDto);
    ResponseMicrocreditDto responseDtoToMicrocredit(Microcredit microcredit);

    //Get
    @Mapping(target = "amountRest", source = "amount")
    ResponseMicrocreditGetDto responseMicrocreditGet(Microcredit microcredit);
}
