package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.RequestContributionDto;
import com.igrowker.nativo.dtos.ResponseContributionDto;

public interface ContributionService {
    ResponseContributionDto contributeToMicrocredit (RequestContributionDto requestContributionDto);
}
