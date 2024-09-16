package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.mappers.MicrocreditMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.MicrocreditService;
import com.igrowker.nativo.validations.AuthenticatedUserAndAccount;
import com.igrowker.nativo.validations.TransactionStatusConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@RequiredArgsConstructor
@Service
public class MicrocreditServiceImpl implements MicrocreditService {
    private final MicrocreditRepository microcreditRepository;
    private final MicrocreditMapper microcreditMapper;
    private final AuthenticatedUserAndAccount authenticatedUserAndAccount;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) {
        AuthenticatedUserAndAccount.UserAccountPair userBorrower = authenticatedUserAndAccount.getAuthenticatedUserAndAccount();
        Microcredit microcredit = microcreditMapper.requestDtoToMicrocredit(requestMicrocreditDto);
        microcredit.setBorrowerAccountId(userBorrower.account.getId());
        microcredit = microcreditRepository.save(microcredit);
        return microcreditMapper.responseDtoToMicrocredit(microcredit);
    }
    //Validaciones pendientes: Que no el usuario no tenga microcréditos adeudados
    //No puede tener más de un microcredito en pendiente
    //Transaccion tiene que seguir pendiente hasta que el monto total se completa


    @Override
    public List<ResponseMicrocreditGetDto> getAll() {
        List<Microcredit> microcredits = microcreditRepository.findAll();
        return microcredits.stream()
                .map(microcreditMapper::responseMicrocreditGet).toList();
    }

    //getPendents
    //mostrar monto total y monto faltante

    @Override
    public List<ResponseMicrocreditGetDto> getMicrocreditsByTransactionStatus(String transactionStatus) {
        TransactionStatusConvert convertValue = new TransactionStatusConvert();
        TransactionStatus enumStatus = convertValue.statusConvert(transactionStatus);

        List<Microcredit> microcredits = microcreditRepository.findByTransactionStatus(enumStatus);

        return microcredits.stream()
                .map(microcreditMapper::responseMicrocreditGet)
                .toList();
    }

    @Override
    public ResponseMicrocreditGetDto getOne(String id) {
        Microcredit microcredit = microcreditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Microcrédito no encontrado con id: " + id));

        return microcreditMapper.responseMicrocreditGet(microcredit);
    }
}