package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;

public interface ContributionService {
    ResponseContributionDto createContribution (RequestContributionDto requestContributionDto);
}
