package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.entities.Contribution;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.ContributionMapper;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.services.ContributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;
    private final MicrocreditRepository microcreditRepository;
    private final ContributionMapper contributionMapper;

    @Override
    public ResponseContributionDto createContribution(RequestContributionDto requestContributionDto) {
        Microcredit microcredit = microcreditRepository.findById(requestContributionDto.microcreditId()).orElseThrow(()->
                new ResourceNotFoundException("Microcrédito no encontrado"));

        Contribution contribution = contributionRepository.save(contributionMapper.requestDtoToContribution(requestContributionDto));

        return contributionMapper.responseContributionDto(contribution);
    }
}
