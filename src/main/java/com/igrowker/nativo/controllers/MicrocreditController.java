package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.*;
import com.igrowker.nativo.services.ContributionService;
import com.igrowker.nativo.services.MicrocreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/microcreditos")
@Tag(name = "Microcréditos")
public class MicrocreditController {
    private final MicrocreditService microcreditService;
    private final ContributionService contributionService;

//@Parameter(description = "Datos del microcrédito a crear", required = true)

    @Operation(summary = "Crear un nuevo microcrédito",
            description = "Endpoint que permite crear un nuevo microcrédito para el usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Microcrédito creado con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseMicrocreditDto.class))),
            @ApiResponse(responseCode = "400", description = "Error en el monto ingresado"),
            @ApiResponse(responseCode = "403", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "Presenta un microcrédito pendiente"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/solicitar")
    public ResponseEntity<ResponseMicrocreditDto> createMicrocredit(@Valid @RequestBody RequestMicrocreditDto requestMicrocreditDto) throws MessagingException {
            ResponseMicrocreditDto response = microcreditService.createMicrocredit(requestMicrocreditDto);

            return ResponseEntity.ok(response);
    }

    @Operation(summary = "Realizar una contribución a un microcrédito",
            description = "Endpoint que permite realizar una contribución a un microcrédito existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contribución realizada con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseContributionDto.class))),
            @ApiResponse(responseCode = "400", description = "Error en el monto ingresado"),
            @ApiResponse(responseCode = "403", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Microcrédito no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya contribuyó a este microcrédito"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/contribuir")
    public ResponseEntity<?> createContribution(@Valid @RequestBody RequestContributionDto requestContributionDto) throws MessagingException {
        ResponseContributionDto response = contributionService.createContribution(requestContributionDto);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Pagar un microcrédito",
            description = "Endpoint que permite pagar un microcrédito existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Microcrédito pagado con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseMicrocreditPaymentDto.class))),
            @ApiResponse(responseCode = "400", description = "Error en el monto ingresado"),
            @ApiResponse(responseCode = "403", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Microcrédito no encontrado"),
            @ApiResponse(responseCode = "409", description = "El microcrédito ya ha sido pagado o no tiene contribuciones"),
            @ApiResponse(responseCode = "402", description = "Fondos insuficientes"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/pagar/{id}")
    public ResponseEntity<ResponseMicrocreditPaymentDto> payMicrocredit(@Parameter(description = "Datos del microcrédito a crear", required = true) @PathVariable String id) throws MessagingException {
        ResponseMicrocreditPaymentDto response = microcreditService.payMicrocredit(id);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener todos los microcréditos del usuario autenticado",
            description = "Endpoint que permite obtener todos los microcréditos del usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de microcréditos obtenidos con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "403", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No se encontraron microcréditos para el usuario")
    })
    @GetMapping("/usuario-logueado")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getAllByUser() {
        List<ResponseMicrocreditGetDto> response = microcreditService.getAllMicrocreditsByUser();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener microcréditos del usuario autenticado por estado",
            description = "Endpoint que permite obtener todos los microcréditos del usuario autenticado filtrados por estado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de microcréditos obtenidos con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "400", description = "Estado no válido"),
            @ApiResponse(responseCode = "403", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No se encontraron microcréditos para el usuario con el estado especificado")
    })
    @GetMapping("/estado/{status}")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getAllMicrocreditsByUserByStatus(@Parameter(description = "Datos del microcrédito a crear", required = true) @PathVariable String status) {
        List<ResponseMicrocreditGetDto> response = microcreditService.getAllMicrocreditsByUserByStatus(status);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener microcréditos entre fechas",
            description = "Endpoint que permite obtener todos los microcréditos del usuario autenticado dentro de un rango de fechas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de microcréditos obtenidos con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "400", description = "Formato de fecha inválido"),
            @ApiResponse(responseCode = "403", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No se encontraron microcréditos para el usuario en el rango de fechas especificado")
    })
    @GetMapping("/entrefechas")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getMicrocreditsBetweenDates(@Parameter(description = "Datos del microcrédito a crear", required = true)
            @RequestParam String fromDate,
            @RequestParam String toDate) {

        List<ResponseMicrocreditGetDto> result = microcreditService.getMicrocreditsBetweenDates(fromDate, toDate);
        return ResponseEntity.ok(result);

    }

    @Operation(summary = "Obtener todos los microcréditos",
            description = "Endpoint que permite obtener todos los microcréditos del sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de todos los microcréditos obtenidos con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a esta información")
    })
    @GetMapping()
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getAll() {
        List<ResponseMicrocreditGetDto> response = microcreditService.getAll();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un microcrédito por ID",
            description = "Endpoint que permite obtener un microcrédito específico por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Microcrédito obtenido con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a esta información"),
            @ApiResponse(responseCode = "404", description = "Microcrédito no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMicrocreditGetDto> getOne(@Parameter(description = "Datos del microcrédito a crear", required = true) @PathVariable String id) {
        ResponseMicrocreditGetDto response = microcreditService.getOne(id);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener microcréditos por estado de transacción",
            description = "Endpoint que permite obtener todos los microcréditos con un estado de transacción específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de microcréditos obtenidos con éxito",
                    content = @Content(schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "400", description = "Estado de transacción inválido"),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a esta información"),
            @ApiResponse(responseCode = "404", description = "No se encontraron microcréditos con el estado de transacción especificado")
    })
    @GetMapping("/historial-estados/{status}")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getMicrocreditsByTransactionStatus(@Parameter(description = "Datos del microcrédito a crear", required = true) @PathVariable String status) {
        List<ResponseMicrocreditGetDto> response = microcreditService.getMicrocreditsByTransactionStatus(status);

        return ResponseEntity.ok(response);
    }
}
