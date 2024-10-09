package com.igrowker.nativo.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.nativo.controllers.ContributionController;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.ContributionService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ContributionController.class)
@WithMockUser
public class ContributionControllerTest {
    @MockBean
    private ContributionService contributionService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Nested
    class GetAllContributionsByUserTests {
        @Test
        public void getAllContributionByUser_ShouldReturnOk() throws Exception {
            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", "5678",
                    "Test1", "Test2", "1234",
                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.getAllContributionsByUser()).thenReturn(List.of(responseContributionDto));

            mockMvc.perform(get("/api/contribuciones/usuario-logueado"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseContributionDto.id())))
                    .andExpect(jsonPath("$[0].lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                    .andExpect(jsonPath("$[0].lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                    .andExpect(jsonPath("$[0].borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                    .andExpect(jsonPath("$[0].microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseContributionDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));
        }

        @Test
        public void getAll_ShouldReturnNotFound_WhenNoContributions() throws Exception {
            when(contributionService.getAllContributionsByUser()).thenThrow(new ResourceNotFoundException("No se encontraron contribuciones."));

            mockMvc.perform(get("/api/contribuciones/usuario-logueado"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron contribuciones.")));
        }
    }

    @Nested
    class GetAllContributionsByUserByStatusTests {
        @Test
        public void getAllContributionByUserByStatus_ShouldReturnOk() throws Exception {
            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", "5678",
                    "Test1", "Test2", "1234",
                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.getAllContributionsByUserByStatus("ACCEPTED")).thenReturn(List.of(responseContributionDto));

            mockMvc.perform(get("/api/contribuciones/estado/ACCEPTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseContributionDto.id())))
                    .andExpect(jsonPath("$[0].lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                    .andExpect(jsonPath("$[0].lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                    .andExpect(jsonPath("$[0].borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                    .andExpect(jsonPath("$[0].microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseContributionDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));
        }

        @Test
        public void getAllContributionByUserByStatus_ShouldReturnNotFound() throws Exception {
            when(contributionService.getAllContributionsByUserByStatus("ACCEPTED"))
                    .thenThrow(new ResourceNotFoundException("No se encontraron contribuciones para el usuario en el estado especificado."));

            mockMvc.perform(get("/api/contribuciones/estado/ACCEPTED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron contribuciones para " +
                            "el usuario en el estado especificado.")));
        }
    }

    @Nested
    class GetContributionsBetweenDatesTests {
        @Test
        public void getAllContributionsBetweenDates_ShouldReturnOk() throws Exception {
            String fromDate = "2024-09-01";
            String toDate = "2024-10-01";

            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", "5678",
                    "Test1", "Test2", "1234",
                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.getContributionsBetweenDates(fromDate, toDate)).thenReturn(List.of(responseContributionDto));

            mockMvc.perform(get("/api/contribuciones/entrefechas?fromDate=" + fromDate + "&toDate=" + toDate))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseContributionDto.id())))
                    .andExpect(jsonPath("$[0].lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                    .andExpect(jsonPath("$[0].lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                    .andExpect(jsonPath("$[0].borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                    .andExpect(jsonPath("$[0].microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseContributionDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));
        }

        @Test
        public void getAllContributionsBetweenDates_ShouldReturnBadRequest() throws Exception {
            String fromDate = "2023-12-31";
            String toDate = LocalDate.now().toString();

            when(contributionService.getContributionsBetweenDates(fromDate, toDate))
                    .thenThrow(new ValidationException("La fecha final no puede ser menor a la inicial."));

            mockMvc.perform(get("/api/contribuciones/entrefechas?fromDate=" + fromDate + "&toDate=" + toDate))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("La fecha final no puede ser menor a la inicial.")));
        }

        @Test
        public void getAllContributionsBetweenDates_ShouldReturnNotFound() throws Exception {
            String fromDate = LocalDate.now().toString();
            String toDate = "2023-12-31";

            when(contributionService.getContributionsBetweenDates(fromDate, toDate))
                    .thenThrow(new ResourceNotFoundException("No se encontraron contribuciones en el rango de fechas proporcionado."));

            mockMvc.perform(get("/api/contribuciones/entrefechas?fromDate=" + fromDate + "&toDate=" + toDate))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron contribuciones en el rango de fechas proporcionado.")));
        }
    }

    @Nested
    class GetContributionsByDateAndStatusTests {
        @Test
        public void getContributionsByDateAndStatus_ShouldReturnOk() throws Exception {
            String date = "2024-09-17";

            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", "5678",
                    "Test1", "Test2", "1234",
                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.getContributionsByDateAndStatus(date, "ACCEPTED")).thenReturn(List.of(responseContributionDto));

            mockMvc.perform(get("/api/contribuciones/buscar-fecha-estado?date=" + date + "&status=" + "ACCEPTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseContributionDto.id())))
                    .andExpect(jsonPath("$[0].lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                    .andExpect(jsonPath("$[0].lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                    .andExpect(jsonPath("$[0].borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                    .andExpect(jsonPath("$[0].microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseContributionDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));

        }

        @Test
        public void getContributionsByDateAndStatus_ShouldReturnNotFound() throws Exception {
            String date = "2023-12-31";

            when(contributionService.getContributionsByDateAndStatus(date, "ACCEPTED"))
                    .thenThrow(new ResourceNotFoundException("No posee contribuciones."));

            mockMvc.perform(get("/api/contribuciones/buscar-fecha-estado?date=" + date + "&status=" + "ACCEPTED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No posee contribuciones.")));
        }
    }

    @Nested
    class GetAllContributionsTests {
        @Test
        public void getAll_ShouldReturnOk() throws Exception {
            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", "5678",
                    "Test1", "Test2", "1234",
                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.getAll()).thenReturn(List.of(responseContributionDto));

            mockMvc.perform(get("/api/contribuciones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseContributionDto.id())))
                    .andExpect(jsonPath("$[0].lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                    .andExpect(jsonPath("$[0].lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                    .andExpect(jsonPath("$[0].borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                    .andExpect(jsonPath("$[0].microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseContributionDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));
        }

        @Test
        public void getAll_ShouldReturnNotFound_WhenNoContributions() throws Exception {
            when(contributionService.getAll()).thenThrow(new ResourceNotFoundException("No se encontraron contribuciones."));

            mockMvc.perform(get("/api/contribuciones"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron contribuciones.")));
        }
    }

    @Nested
    class GetOneContributionTests {
        @Test
        public void getOneContribution_ShouldReturnOk() throws Exception {
            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", "5678",
                    "Test1", "Test2", "1234",
                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.getOneContribution("1111")).thenReturn(responseContributionDto);

            mockMvc.perform(get("/api/contribuciones/1111"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(responseContributionDto.id())))
                    .andExpect(jsonPath("$.lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                    .andExpect(jsonPath("$.lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                    .andExpect(jsonPath("$.borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                    .andExpect(jsonPath("$.microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                    .andExpect(jsonPath("$.amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                    .andExpect(jsonPath("$.createdDate", Matchers.is(responseContributionDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$.expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$.transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));
        }

        @Test
        public void getOneContribution_ShouldNotReturnOk() throws Exception {
            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", "5678",
                    "Test1", "Test2", "1234",
                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.getOneContribution(any())).thenThrow(new ResourceNotFoundException("Contribución no encontrada con id: "
                    + responseContributionDto.id()));

            mockMvc.perform(get("/api/contribuciones/" + responseContributionDto.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("Contribución no encontrada con id: "
                            + responseContributionDto.id())));

        }
    }

    @Nested
    class GetContributionsByTransactionStatusTests {
        @Test
        public void getContributionsByTransactionStatus_ShouldReturnOk() throws Exception {
            ResponseContributionDto responseContributionDto = new ResponseContributionDto("1111", "5678",
                    "Test1", "Test2", "1234",
                    BigDecimal.valueOf(10000.00), LocalDateTime.of(2024, 9, 17, 18, 20),
                    LocalDateTime.of(2024, 10, 17, 18, 20),
                    TransactionStatus.ACCEPTED);

            when(contributionService.getContributionsByTransactionStatus("ACCEPTED"))
                    .thenReturn(List.of(responseContributionDto));

            mockMvc.perform(get("/api/contribuciones/historial-estados/ACCEPTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseContributionDto.id())))
                    .andExpect(jsonPath("$[0].lenderAccountId", Matchers.is(responseContributionDto.lenderAccountId())))
                    .andExpect(jsonPath("$[0].lenderFullname", Matchers.is(responseContributionDto.lenderFullname())))
                    .andExpect(jsonPath("$[0].borrowerFullname", Matchers.is(responseContributionDto.borrowerFullname())))
                    .andExpect(jsonPath("$[0].microcreditId", Matchers.is(responseContributionDto.microcreditId())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseContributionDto.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].createdDate", Matchers.is(responseContributionDto.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].expiredDateMicrocredit", Matchers.is(responseContributionDto.expiredDateMicrocredit().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseContributionDto.transactionStatus().toString())));
        }

        @Test
        public void getContributionsByTransactionStatus_ShouldReturnNotFound() throws Exception {
            String invalidStatus = "INVALID_STATUS";

            when(contributionService.getContributionsByTransactionStatus(invalidStatus))
                    .thenThrow(new ResourceNotFoundException("No se encontraron contribuciones con el estado especificado."));

            mockMvc.perform(get("/api/contribuciones/historial-estados/" + invalidStatus))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("No se encontraron contribuciones con el estado especificado.")));
        }
    }
}
