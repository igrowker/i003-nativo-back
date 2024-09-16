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
import com.igrowker.nativo.validations.TransactionStatusConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MicrocreditServiceImpl implements MicrocreditService {
    private final MicrocreditRepository microcreditRepository;
    private final MicrocreditMapper microcreditMapper;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseMicrocreditDto createMicrocredit(RequestMicrocreditDto requestMicrocreditDto) {
        Microcredit microcredit = microcreditRepository.save(microcreditMapper.requestDtoToMicrocredit(requestMicrocreditDto));
        return microcreditMapper.responseDtoToMicrocredit(microcredit);
        //Validaciones pendientes: Que no el usuario no tenga microcréditos adeudados
        //No puede tener más de un microcredito en pendiente
        //Transaccion tiene que seguir pendiente hasta que el monto total se completa
    }

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

    //Falta un list de los microcreditos pendientes
}
/* ●	Generación (de la persona que solicita el dinero):
ítulo, Descripción (razón de necesidad del dinero),
Monto total, Fecha de vencimiento (cuando devolverá el dinero)
Para que sea más simple para back, estamos viendo de poner una
fecha fija a x cantidad de tiempo luego de la creación,
momento en el cual devuelve la suma total a todos los contribuyentes,
 sin ningún tipo de interés agregado.
 */