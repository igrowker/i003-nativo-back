package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Contribution;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.ContributionMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.validations.AuthenticatedUserAndAccount;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;
    private final MicrocreditRepository microcreditRepository;
    private final ContributionMapper contributionMapper;
    private final AuthenticatedUserAndAccount authenticatedUserAndAccount;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResponseContributionDto createContribution(RequestContributionDto requestContributionDto) {
        AuthenticatedUserAndAccount.UserAccountPair userLender = authenticatedUserAndAccount.getAuthenticatedUserAndAccount();
        Microcredit microcredit = microcreditRepository.findById(requestContributionDto.microcreditId())
                .orElseThrow(() -> new ResourceNotFoundException("Microcrédito no encontrado"));
        if (microcredit.getBorrowerAccountId().equals(userLender.account.getId())) {
            throw new IllegalArgumentException("El usuario contribuyente no puede ser el mismo que el solicitante del microcrédito.");
        }
        Contribution contribution = contributionMapper.requestDtoToContribution(requestContributionDto);
        contribution.setLenderAccountId(userLender.account.getId());
        contribution.setMicrocredit(microcredit);
        contribution = contributionRepository.save(contribution);
        String lenderFullname = fullname(contribution.getLenderAccountId());
        String borrowerFullname = fullname(microcredit.getBorrowerAccountId());
        return contributionMapper.responseContributionDto(contribution, lenderFullname, borrowerFullname);
    }

    //El microcreditId sale en null en el response, pero se ve en la base de datos

    //poner plata
    //verificar que tenga plata en cuenta
    //Que el monto a contribuir no sea mayor al total faltante
    //si esta ok, se descuenta la plata del contribuyente y se suma a la cuenta
    //status contribución ok

    // Listado de contribuciones por contribuyente.

    private String fullname(String accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() ->
                new ResourceNotFoundException("Cuenta no encontrada"));

        User user = userRepository.findById(account.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException("Usuario no encontrado"));

        return user.getSurname().toUpperCase() + ", " + user.getName();
    }

    /*
        public ResponseMicrocreditGetDto getOne(String id) {
        Microcredit microcredit = microcreditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Microcrédito no encontrado con id: " + id));

        return microcreditMapper.responseMicrocreditGet(microcredit);
    }

     */



}
