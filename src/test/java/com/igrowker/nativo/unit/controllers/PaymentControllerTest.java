package com.igrowker.nativo.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.nativo.controllers.PaymentController;
import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponsePaymentDto;
import com.igrowker.nativo.dtos.payment.ResponseRecordPayment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.InvalidDataException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.PaymentService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PaymentController.class)
@WithMockUser
public class PaymentControllerTest {

    @MockBean
    private PaymentService paymentService;
    @MockBean
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper();

    @Nested
    class CreateQrTests {
        @Test
        public void create_qr_should_be_ok() throws Exception {
            //Arrange: preparar las clases de input y output,
            //se puede hacer por fuera del test si son clases compartidas.
            var RequestPaymentDto = new RequestPaymentDto("abcReceiver",
                    BigDecimal.valueOf(100.50), "un chicle tutti frutti");

            var ResponsePaymentDto = new ResponsePaymentDto("abcPayment", "abcSender",
                    BigDecimal.valueOf(100.50), "un chicle tutti frutti", "qrlalalala12");
            when(paymentService.createQr(any())).thenReturn(ResponsePaymentDto);

            //Act: llamada al método que querés probar.
            mockMvc.perform(post("/api/pagos/crear-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestPaymentDto))
                    )

                    //Assert: probar por verdadero o falso distintas aserciones.
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(ResponsePaymentDto.id())))
                    .andExpect(jsonPath("$.receiverAccount", Matchers.is(ResponsePaymentDto.receiverAccount())))
                    .andExpect(jsonPath("$.qr", Matchers.is(ResponsePaymentDto.qr())))
                    .andExpect(jsonPath("$.description", Matchers.is(ResponsePaymentDto.description())))
                    .andExpect(jsonPath("$.amount", Matchers.is(ResponsePaymentDto.amount().doubleValue())));
        }

        @Test
        public void create_qr_should_NOT_be_ok() throws Exception {
            var RequestPaymentDto = new RequestPaymentDto("abcReceiver",
                    BigDecimal.valueOf(100.50), "un chicle tutti frutti");
            when(paymentService.createQr(any())).thenThrow(new InvalidUserCredentialsException(
                    "La cuenta indicada no coincide con el usuario logueado en la aplicación")
            );
            mockMvc.perform(post("/api/pagos/crear-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestPaymentDto))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", Matchers.is("La cuenta indicada no coincide con el usuario logueado en la aplicación")));

        }
    }

    @Nested
    class GetAllPaymentsTests {
        @Test
        public void get_all_should_be_ok() throws Exception {
            var responseRecordPayment = new ResponseRecordPayment("payment1",
                    "sender1", "receiver1", BigDecimal.valueOf(100.5),
                    "un chicle de tutti frutti",
                    LocalDateTime.of(2024, 9, 20, 12, 05),
                    TransactionStatus.ACCEPTED);
            when(paymentService.getAllPayments()).thenReturn(List.of(responseRecordPayment));

            mockMvc.perform(get("/api/pagos/todo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseRecordPayment.id())))
                    .andExpect(jsonPath("$[0].senderAccount", Matchers.is(responseRecordPayment.senderAccount())))
                    .andExpect(jsonPath("$[0].receiverAccount", Matchers.is(responseRecordPayment.receiverAccount())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseRecordPayment.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseRecordPayment.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseRecordPayment.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].transactionDate", Matchers.is(responseRecordPayment.transactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))));
        }

        @Test
        public void get_all_should_NOT_be_ok() throws Exception {
            when(paymentService.getAllPayments()).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));
            mockMvc.perform(get("/api/pagos/todo"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetPaymentsByStatusTests {
        @Test
        public void get_by_status_should_be_ok() throws Exception {
            var status = TransactionStatus.ACCEPTED;
            var responseRecordPayment = new ResponseRecordPayment("payment1",
                    "sender1", "receiver1", BigDecimal.valueOf(100.5),
                    "un chicle de tutti frutti",
                    LocalDateTime.of(2024, 9, 20, 12, 05),
                    status );
            when(paymentService.getPaymentsByStatus(status.toString())).thenReturn(List.of(responseRecordPayment));

            mockMvc.perform(get("/api/pagos/estado/{status}", status.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseRecordPayment.id())))
                    .andExpect(jsonPath("$[0].senderAccount", Matchers.is(responseRecordPayment.senderAccount())))
                    .andExpect(jsonPath("$[0].receiverAccount", Matchers.is(responseRecordPayment.receiverAccount())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseRecordPayment.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseRecordPayment.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseRecordPayment.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].transactionDate", Matchers.is(responseRecordPayment.transactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))));
        }

        @Test
        public void get_by_status_should_NOT_be_ok_due_NOT_FOUND() throws Exception {
            when(paymentService.getPaymentsByStatus(TransactionStatus.ACCEPTED.toString())).
                    thenThrow(new ResourceNotFoundException("Usuario no encontrado"));
            mockMvc.perform(get("/api/pagos/estado/{status}", TransactionStatus.ACCEPTED.toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void get_by_status_should_NOT_be_ok_due_STATUS() throws Exception {
            var status = TransactionStatus.ACCEPTED;
            when(paymentService.getPaymentsByStatus(status.toString())).
                    thenThrow(new InvalidDataException("El estado de la transacción no existe: " + status));
            mockMvc.perform(get("/api/pagos/estado/{status}", status.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetPaymentsByOneDateTests {
        @Test
        public void get_by_date_should_be_ok() throws Exception {
            var date = "2024-09-20";
            var responseRecordPayment = new ResponseRecordPayment("payment1",
                    "sender1", "receiver1", BigDecimal.valueOf(100.5),
                    "un chicle de tutti frutti",
                    LocalDateTime.of(2024, 9, 20, 12, 05),
                    TransactionStatus.ACCEPTED );
            when(paymentService.getPaymentsByDate(date)).thenReturn(List.of(responseRecordPayment));

            mockMvc.perform(get("/api/pagos/fecha/{date}", date))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(jsonPath("$[0].id", Matchers.is(responseRecordPayment.id())))
                    .andExpect(jsonPath("$[0].senderAccount", Matchers.is(responseRecordPayment.senderAccount())))
                    .andExpect(jsonPath("$[0].receiverAccount", Matchers.is(responseRecordPayment.receiverAccount())))
                    .andExpect(jsonPath("$[0].amount", Matchers.is(responseRecordPayment.amount().doubleValue())))
                    .andExpect(jsonPath("$[0].description", Matchers.is(responseRecordPayment.description())))
                    .andExpect(jsonPath("$[0].transactionStatus", Matchers.is(responseRecordPayment.transactionStatus().toString())))
                    .andExpect(jsonPath("$[0].transactionDate", Matchers.is(responseRecordPayment.transactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))));
        }

        @Test
        public void get_by_date_should_NOT_be_ok_due_NOT_FOUND() throws Exception {
            var date = "2024-09-20";
            when(paymentService.getPaymentsByDate(date)).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));
            mockMvc.perform(get("/api/pagos/fecha/{date}", date))
                    .andExpect(status().isNotFound());
        }

        //TODO. Add other test when date validation is coded.
    }


}
