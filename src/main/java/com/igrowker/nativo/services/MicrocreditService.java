package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.ResponseMicrocreditDto;

public interface MicrocreditService {
    ResponseMicrocreditDto applyMicrocredit (RequestMicrocreditDto requestMicrocreditDto);
}
