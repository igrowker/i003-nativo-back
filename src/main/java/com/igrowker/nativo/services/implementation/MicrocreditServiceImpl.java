package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.mappers.MicrocreditMapper;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.services.MicrocreditService;
import com.igrowker.nativo.validations.Validations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MicrocreditServiceImpl implements MicrocreditService {
    private final MicrocreditRepository microcreditRepository;
    private final MicrocreditMapper microcreditMapper;
    private final Validations validations;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) {
        Validations.UserAccountPair userBorrower = validations.getAuthenticatedUserAndAccount();

        Microcredit microcredit = microcreditMapper.requestDtoToMicrocredit(requestMicrocreditDto);

        //Validación para evaluar si tiene un microcredito en estado pendiente, vencido o denegado
        isMicrocreditExpiredOrPendentOrDenied(userBorrower.account.getId());

        BigDecimal limite = new BigDecimal("500000");

        if (microcredit.getAmount().compareTo(limite) > 0) {
            throw new ValidationException("El monto del microcrédito tiene que ser igual o menor a: $ " + limite);
        }

        microcredit.setBorrowerAccountId(userBorrower.account.getId());
        microcredit = microcreditRepository.save(microcredit);

        //Agregar fecha de vencimiento del microcredito
        return microcreditMapper.responseDtoToMicrocredit(microcredit);
    }

    @Override
    public List<ResponseMicrocreditGetDto> getAll() {
        List<Microcredit> microcredits = microcreditRepository.findAll();

        return microcredits.stream()
                .map(microcreditMapper::responseMicrocreditGet).toList();
    }

    @Override
    public List<ResponseMicrocreditGetDto> getMicrocreditsByTransactionStatus(String transactionStatus) {
        TransactionStatus enumStatus = validations.statusConvert(transactionStatus);
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

    private void isMicrocreditExpiredOrPendentOrDenied(String borrowerAccountId) {
        Optional<Microcredit> pendingMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.PENDENT);
        if (pendingMicrocredit.isPresent()) {
            throw new ValidationException("Presenta un microcrédito pendiente.");
        }

        Optional<Microcredit> expiredMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.EXPIRED);
        if (expiredMicrocredit.isPresent()) {
            throw new ValidationException("Presenta un microcrédito vencido.");
        }

        Optional<Microcredit> deniedMicrocredit = microcreditRepository.findByBorrowerAccountIdAndTransactionStatus(borrowerAccountId, TransactionStatus.DENIED);
        if (deniedMicrocredit.isPresent()) {
            throw new ValidationException("No puede solicitar un nuevo microcrédito. Debe regularizar el estado de su cuenta.");
        }
    }

    /*
    Listar todos los microcreditos, comparar la fecha de vencimiento con la actual.
    ACCEPTED -- SE COMPLETA EL MONTO TOTAL
    DENIED -- NO CUMPLE CON VENCIMIENTO O PENDIENTES,
    FAILED -- ALGUN PROBLEMA DE SISTEMA
    PENDENT -- AL CREARSE EL MICROCREDITO
    EXPIRED -- SUPERA LA FECHA DE VENCIMIENTO Y NO TIENE FONDOS ENTRA EN DEUDA.
    COMPLETED -- AL CUMPLIRSE CON EL PAGO DEL MICRO EN TERMINO
     */

    //CARGAR TRANSACCIONES A USAR AL SWITCH


}