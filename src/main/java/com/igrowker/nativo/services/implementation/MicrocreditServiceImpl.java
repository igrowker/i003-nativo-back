package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.ResponseMicrocreditDto;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.services.MicrocreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MicrocreditServiceImpl implements MicrocreditService {
    private final MicrocreditRepository microcreditRepository;

    @Override
    public ResponseMicrocreditDto applyMicrocredit(RequestMicrocreditDto requestMicrocreditDto) {
        return null;
    }
}
