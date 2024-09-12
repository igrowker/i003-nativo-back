package com.igrowker.nativo.mappers;

import com.igrowker.nativo.dtos.RequestMicrocreditDto;
import com.igrowker.nativo.entities.Microcredit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MicrocreditMapper {
    Microcredit requestDtoToMicrocredit(RequestMicrocreditDto requestMicrocreditDto);
}
