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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;
    private final MicrocreditRepository microcreditRepository;
    private final ContributionMapper contributionMapper;

    @Override
    @Transactional
    public ResponseContributionDto createContribution(RequestContributionDto requestContributionDto) {
        Microcredit microcredit = microcreditRepository.findById(requestContributionDto.microcreditId()).orElseThrow(()->
                new ResourceNotFoundException("Microcrédito no encontrado"));

        Contribution contribution = contributionRepository.save(contributionMapper.requestDtoToContribution(requestContributionDto));

        return contributionMapper.responseContributionDto(contribution);
    }
    //verificar que las cuentas sean distintas
    //poner plata
    //verificar que tenga plata en cuenta
    //Que el monto a contribuir no sea mayor al total faltante
    //si esta ok, se descuenta la plata del contribuyente y se suma a la cuenta
    //status contribución ok



    // Listado de contribuciones por contribuyente.

}

/*●	Pantalla de presentación para contribuir (para la persona que
quiere contribuir): debería mostrar un listado de todos los
microcréditos disponibles, aclarando en cada uno el nombre receptor,
título, descripción, monto total a recaudar, fecha de vencimiento
(cuándo recuperará su dinero).
Una vez elegida la opción, debería abrirse espacio para ingresar
el monto a contribuir.*/