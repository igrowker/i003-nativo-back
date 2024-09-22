package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionGetDto;
import jakarta.mail.MessagingException;

import java.util.List;

public interface ContributionService {
    ResponseContributionDto createContribution (RequestContributionDto requestContributionDto) throws MessagingException;
    List<ResponseContributionGetDto> getAll();
    List<ResponseContributionGetDto> getContributionsByTransactionStatus(String transactionStatus);
    ResponseContributionGetDto getOneContribution(String id);
}
