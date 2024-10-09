package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditGetDto;
import com.igrowker.nativo.services.ContributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/contribuciones")
@Tag(name = "Contribuciones")
public class ContributionController {
    private final ContributionService contributionService;

    @Operation(summary = "Obtener todos las contribuciones del usuario autenticado",
            description = "Endpoint que permite obtener todas las contribuciones del usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de contribuciones obtenidas con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseContributionDto.class))),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
    })
    @GetMapping("/usuario-logueado")
    public ResponseEntity<List<ResponseContributionDto>> getAllByUser() {
        List<ResponseContributionDto> response = contributionService.getAllContributionsByUser();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener contribuciones del usuario autenticado por estado",
            description = "Endpoint que permite obtener todas las contribuciones del usuario autenticado filtrados por estado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de contribuciones por estado obtenidas con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseContributionDto.class))),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
    })
    @GetMapping("/estado/{status}")
    public ResponseEntity<List<ResponseContributionDto>> getAllContributionsByUserByStatus(@Parameter(description = "Seleccionar el estado de la contribución a buscar", required = true,
            schema = @Schema(allowableValues = {"ACCEPTED", "COMPLETED", "DENIED", "FAILED"})) @PathVariable String status) {
        List<ResponseContributionDto> response = contributionService.getAllContributionsByUserByStatus(status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Obtener contribuciones entre fechas del usuario autenticado",
            description = "Endpoint que permite obtener todas las contribuciones del usuario autenticado dentro de un rango de fechas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de contribuciones, filtrado por fechas, obtenidas con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseContributionDto.class))),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
    })
    @GetMapping("/entrefechas")
    public ResponseEntity<List<ResponseContributionDto>> getContributionsBetweenDates(@Parameter(description = "Desde", required = true, example = "2023-03-21")
                                                                                      @RequestParam String fromDate,
                                                                                      @Parameter(description = "Hasta", required = true, example = "2024-03-21")
                                                                                      @RequestParam String toDate) {
        List<ResponseContributionDto> result = contributionService.getContributionsBetweenDates(fromDate, toDate);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener contribuciones en una fecha y en un estado determinado del usuario autenticado",
            description = "Endpoint que permite obtener todas las contribuciones del usuario autenticado dentro de " +
                    "una fecha y un estado determinado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de contribuciones, filtrado por fecha y el estado " +
                    "de la transacción, obtenidos con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMicrocreditGetDto.class))),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
    })
    @GetMapping("/buscar-fecha-estado")
    public ResponseEntity<List<ResponseContributionDto>> getContributionsByDateAndStatus(@Parameter(description = "Fecha", required = true, example = "2023-03-21")
                                                                                         @RequestParam String date,
                                                                                         @Parameter(description = "Seleccionar el estado de la contribución a buscar", required = true,
                                                                                                 schema = @Schema(allowableValues = {"ACCEPTED", "COMPLETED", "DENIED", "FAILED"}))
                                                                                         @RequestParam String status) {

        List<ResponseContributionDto> result = contributionService.getContributionsByDateAndStatus(date, status);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener todas las contribuciones",
            description = "Endpoint que permite obtener todas las contribuciones del sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de todas las contribuciones obtenidas con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseContributionDto.class))),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping()
    public ResponseEntity<List<ResponseContributionDto>> getAll() {
        List<ResponseContributionDto> response = contributionService.getAll();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Obtener una contribución por ID",
            description = "Endpoint que permite obtener una contribución específica por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contribución obtenida con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseContributionDto.class))),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseContributionDto> getOneContribution(@Parameter(description = "ID del microcrédito", required = true) @PathVariable String id) {
        ResponseContributionDto response = contributionService.getOneContribution(id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Obtener contribuciones por estado de transacción",
            description = "Endpoint que permite obtener todas las contribuciones con un estado de transacción específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contribuciones obtenidas con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseContributionDto.class))),
            @ApiResponse(responseCode = "403", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/historial-estados/{status}")
    public ResponseEntity<List<ResponseContributionDto>> getContributionsByTransactionStatus(@Parameter(description = "Seleccionar el estado de la contribución a buscar", required = true,
            schema = @Schema(allowableValues = {"ACCEPTED", "COMPLETED", "DENIED", "FAILED"})) @PathVariable String status) {
        List<ResponseContributionDto> response = contributionService.getContributionsByTransactionStatus(status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
