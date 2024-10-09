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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/microcreditos")
@Tag(name = "Microcréditos")
public class MicrocreditController {
    private final MicrocreditService microcreditService;
    private final ContributionService contributionService;

    @Operation(summary = "Crear un nuevo microcrédito",
            description = "Endpoint que permite crear un nuevo microcrédito para el usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Microcrédito creado con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMicrocreditDto.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "409", content = @Content)
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseContributionDto.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "401", content = @Content),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "409", content = @Content)
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMicrocreditPaymentDto.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "401", content = @Content),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "409", content = @Content)
    })
    @PostMapping("/pagar/{id}")
    public ResponseEntity<ResponseMicrocreditPaymentDto> payMicrocredit(@Parameter(description = "ID del microcrédito", required = true) @PathVariable String id) throws MessagingException {
        ResponseMicrocreditPaymentDto response = microcreditService.payMicrocredit(id);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener todos los microcréditos del usuario autenticado",
            description = "Endpoint que permite obtener todos los microcréditos del usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de microcréditos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
    })
    @GetMapping("/usuario-logueado")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getAllByUser() {
        List<ResponseMicrocreditGetDto> response = microcreditService.getAllMicrocreditsByUser();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener microcréditos del usuario autenticado por estado",
            description = "Endpoint que permite obtener todos los microcréditos del usuario autenticado filtrados por estado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de microcréditos por estado obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
    })
    @GetMapping("/estado/{status}")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getAllMicrocreditsByUserByStatus(@Parameter(description = "Seleccionar el estado del microcrédito a buscar", required = true,
            schema = @Schema(allowableValues = {"ACCEPTED", "DENIED", "PENDING", "EXPIRED", "COMPLETED"}))
                                                                                            @PathVariable String status) {
        List<ResponseMicrocreditGetDto> response = microcreditService.getAllMicrocreditsByUserByStatus(status);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener microcréditos entre fechas del usuario autenticado",
            description = "Endpoint que permite obtener todos los microcréditos del usuario autenticado dentro de un rango de fechas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de microcréditos, filtrado por fechas, obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
    })
    @GetMapping("/entrefechas")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getMicrocreditsBetweenDates(@Parameter(description = "Desde", required = true, example = "2023-03-21")
                                                                                       @RequestParam String fromDate,
                                                                                       @Parameter(description = "Hasta", required = true, example = "2024-03-21")
                                                                                       @RequestParam String toDate) {

        List<ResponseMicrocreditGetDto> result = microcreditService.getMicrocreditsBetweenDates(fromDate, toDate);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener microcréditos en una fecha y en un estado determinado del usuario autenticado",
            description = "Endpoint que permite obtener todos los microcréditos del usuario autenticado dentro de " +
                    "una fecha y un estado determinado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de microcréditos, filtrado por fecha y el estado " +
                    "de la transacción, obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
    })
    @GetMapping("/buscar-fecha-estado")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getMicrocreditsByDateAndStatus(@Parameter(description = "Fecha",
            required = true, example = "2023-03-21")
                                                                                          @RequestParam String date,
                                                                                          @Parameter(description = "Seleccionar " +
                                                                                                  "el estado del microcrédito a buscar",
                                                                                                  required = true,
                                                                                                  schema = @Schema(allowableValues =
                                                                                                          {"ACCEPTED", "DENIED", "PENDING", "EXPIRED", "COMPLETED"}))
                                                                                          @RequestParam String status) {

        List<ResponseMicrocreditGetDto> result = microcreditService.getMicrocreditsByDateAndStatus(date, status);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener todos los microcréditos",
            description = "Endpoint que permite obtener todos los microcréditos del sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de todos los microcréditos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "404", content = @Content)
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
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMicrocreditGetDto> getOne(@Parameter(description = "ID del microcrédito", required = true) @PathVariable String id) {
        ResponseMicrocreditGetDto response = microcreditService.getOne(id);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener microcréditos por estado de transacción",
            description = "Endpoint que permite obtener todos los microcréditos con un estado de transacción específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de microcréditos obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/historial-estados/{status}")
    public ResponseEntity<List<ResponseMicrocreditGetDto>> getMicrocreditsByTransactionStatus(@Parameter(description = "Seleccionar el estado del microcrédito a buscar", required = true, schema = @Schema(allowableValues = {"ACCEPTED", "DENIED", "PENDING", "EXPIRED", "COMPLETED"})) @PathVariable String status) {
        List<ResponseMicrocreditGetDto> response = microcreditService.getMicrocreditsByTransactionStatus(status);

        return ResponseEntity.ok(response);
    }
}
