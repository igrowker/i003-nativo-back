package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.RequestContributionDto;
import com.igrowker.nativo.dtos.ResponseContributionDto;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.services.ContributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;

    @Override
    public ResponseContributionDto contributeToMicrocredit(RequestContributionDto requestContributionDto) {
        return null;
    }
}
