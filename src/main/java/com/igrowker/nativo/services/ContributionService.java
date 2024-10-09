package com.igrowker.nativo.services;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import jakarta.mail.MessagingException;

import java.util.List;

public interface ContributionService {
    ResponseContributionDto createContribution(RequestContributionDto requestContributionDto) throws MessagingException;

    List<ResponseContributionDto> getAllContributionsByUser();

    List<ResponseContributionDto> getAllContributionsByUserByStatus(String transactionStatus);

    List<ResponseContributionDto> getContributionsBetweenDates(String fromDate, String toDate);

    List<ResponseContributionDto> getContributionsByDateAndStatus(String date, String transactionStatus);

    List<ResponseContributionDto> getAll();

    ResponseContributionDto getOneContribution(String id);

    List<ResponseContributionDto> getContributionsByTransactionStatus(String transactionStatus);
}
