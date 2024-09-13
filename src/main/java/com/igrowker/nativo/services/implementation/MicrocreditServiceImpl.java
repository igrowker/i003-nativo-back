package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.mappers.MicrocreditMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.services.MicrocreditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MicrocreditServiceImpl implements MicrocreditService {
    private final MicrocreditRepository microcreditRepository;
    private final MicrocreditMapper microcreditMapper;
    private final AccountRepository accountRepository;

    @Override
    public ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) {
        Microcredit microcredit = microcreditRepository.save(microcreditMapper.requestDtoToMicrocredit(requestMicrocreditDto));

        return microcreditMapper.responseDtoToMicrocredit(microcredit);

        //TODO agregar validaciones
    }

    @Override
    public List<ResponseMicrocreditGetDto> getAll() {
        List<Microcredit> microcredits = microcreditRepository.findAll();
        List<ResponseMicrocreditGetDto> responseMicrocreditGetDtos = new ArrayList<>();

        for (Microcredit microcredit : microcredits) {
            ResponseMicrocreditGetDto responseMicrocreditGetDto = microcreditMapper.responseMicrocreditGet(Optional.ofNullable(microcredit));
            responseMicrocreditGetDtos.add(responseMicrocreditGetDto);
        }

        return responseMicrocreditGetDtos;
    }

    @Override
    public ResponseMicrocreditGetDto getOne(Long id) {
        // Buscamos el microcrédito por ID
        Microcredit microcredit = microcreditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Microcredit not found with ID: " + id));

        // Mapeamos el objeto Microcredit a ResponseMicrocreditGetDto
        ResponseMicrocreditGetDto responseMicrocreditGetDto = microcreditMapper.responseMicrocreditGet(Optional.ofNullable(microcredit));

        return responseMicrocreditGetDto;
    }
}
