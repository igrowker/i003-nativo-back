package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.mappers.MicrocreditMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.services.MicrocreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return microcredits.stream()
                .map(microcreditMapper::responseMicrocreditGet).toList();
    }

    @Override
    public ResponseMicrocreditGetDto getOne(String id) {
        Microcredit microcredit = microcreditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Microcrédito no encontrado con id: " + id)); // Manejo de excepción si no se encuentra

        return microcreditMapper.responseMicrocreditGet(microcredit); // Mapeo de entidad a DTO
    }
}
