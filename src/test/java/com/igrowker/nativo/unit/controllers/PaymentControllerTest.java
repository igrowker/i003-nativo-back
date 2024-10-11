package com.igrowker.nativo.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.nativo.controllers.PaymentController;
import com.igrowker.nativo.dtos.payment.*;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.exceptions.*;
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

            var ResponsePaymentDto = new ResponsePaymentDto("abcPayment", "abcReceiver",
                    "Name", "Surname",
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
    class ProcessPaymentTest{
        @Test
        public void process_payment_should_be_ok_due_accepted() throws Exception{
            //Given
            RequestProcessPaymentDto RequestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");
            ResponseProcessPaymentDto ResponseProcessPaymentDto = new ResponseProcessPaymentDto("TransactionID","Sender Name",
                    "Sender Surname", "SenderAccountNumber", "Receiver Name",
                    "Receiver Surname","ReceiverAccountNumber",
                    BigDecimal.valueOf(20.00), TransactionStatus.ACCEPTED, LocalDateTime.now());

            when(paymentService.processPayment(any())).thenReturn(ResponseProcessPaymentDto);

            //Then
            mockMvc.perform(post("/api/pagos/pagar-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestProcessPaymentDto))
                    )

                    //Assert
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(ResponseProcessPaymentDto.id())))
                    .andExpect(jsonPath("$.senderName", Matchers.is(ResponseProcessPaymentDto.senderName())))
                    .andExpect(jsonPath("$.senderSurname", Matchers.is(ResponseProcessPaymentDto.senderSurname())))
                    .andExpect(jsonPath("$.senderAccount",  Matchers.is(ResponseProcessPaymentDto.senderAccount())))
                    .andExpect(jsonPath("$.receiverName",  Matchers.is(ResponseProcessPaymentDto.receiverName())))
                    .andExpect(jsonPath("$.receiverSurname",  Matchers.is(ResponseProcessPaymentDto.receiverSurname())))
                    .andExpect(jsonPath("$.receiverAccount", Matchers.is(ResponseProcessPaymentDto.receiverAccount())))
                    .andExpect(jsonPath("$.amount", Matchers.is(ResponseProcessPaymentDto.amount().doubleValue())))
                    .andExpect(jsonPath("$.transactionStatus", Matchers.is(ResponseProcessPaymentDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$.transactionDate", Matchers.containsString(ResponseProcessPaymentDto.transactionDate().toLocalDate().toString())));
        }

        @Test
        public void process_payment_should_be_ok_due_denied() throws Exception{
            //Given
            RequestProcessPaymentDto RequestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "DENIED");
            ResponseProcessPaymentDto ResponseProcessPaymentDto = new ResponseProcessPaymentDto("TransactionID","Sender Name",
                    "Sender Surname", "SenderAccountNumber", "Receiver Name",
                    "Receiver Surname","ReceiverAccountNumber",
                    BigDecimal.valueOf(20.00), TransactionStatus.DENIED, LocalDateTime.now());

            when(paymentService.processPayment(any())).thenReturn(ResponseProcessPaymentDto);

            //Then
            mockMvc.perform(post("/api/pagos/pagar-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestProcessPaymentDto))
                    )

                    //Assert
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", Matchers.is(ResponseProcessPaymentDto.id())))
                    .andExpect(jsonPath("$.senderName", Matchers.is(ResponseProcessPaymentDto.senderName())))
                    .andExpect(jsonPath("$.senderSurname", Matchers.is(ResponseProcessPaymentDto.senderSurname())))
                    .andExpect(jsonPath("$.senderAccount",  Matchers.is(ResponseProcessPaymentDto.senderAccount())))
                    .andExpect(jsonPath("$.receiverName",  Matchers.is(ResponseProcessPaymentDto.receiverName())))
                    .andExpect(jsonPath("$.receiverSurname",  Matchers.is(ResponseProcessPaymentDto.receiverSurname())))
                    .andExpect(jsonPath("$.receiverAccount", Matchers.is(ResponseProcessPaymentDto.receiverAccount())))
                    .andExpect(jsonPath("$.amount", Matchers.is(ResponseProcessPaymentDto.amount().doubleValue())))
                    .andExpect(jsonPath("$.transactionStatus", Matchers.is(ResponseProcessPaymentDto.transactionStatus().toString())))
                    .andExpect(jsonPath("$.transactionDate", Matchers.containsString(ResponseProcessPaymentDto.transactionDate().toLocalDate().toString())));
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_credentials() throws Exception{
            //Given
            RequestProcessPaymentDto RequestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");
            when(paymentService.processPayment(any())).thenThrow(new InvalidUserCredentialsException(
                    "La cuenta indicada no coincide con el usuario logueado en la aplicación")
            );
            mockMvc.perform(post("/api/pagos/pagar-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestProcessPaymentDto))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", Matchers.is("La cuenta indicada no coincide con el usuario logueado en la aplicación")));
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_payment_not_found() throws Exception{
            RequestProcessPaymentDto RequestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");
            when(paymentService.processPayment(any())).thenThrow(new ResourceNotFoundException("El Pago solicitado no fue encontrado"));
            mockMvc.perform(post("/api/pagos/pagar-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestProcessPaymentDto))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("El Pago solicitado no fue encontrado")));
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_already_processed_payment() throws Exception{
            RequestProcessPaymentDto RequestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");
            when(paymentService.processPayment(any())).thenThrow(new ExpiredTransactionException("El QR ya fue utilizado."));
            mockMvc.perform(post("/api/pagos/pagar-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestProcessPaymentDto))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("El QR ya fue utilizado.")));
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_expired_payment() throws Exception{
            RequestProcessPaymentDto RequestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");
            when(paymentService.processPayment(any())).thenThrow(new ExpiredTransactionException("El QR no puede ser procesado por exceso en el limite de tiempo. Genere uno nuevo."));
            mockMvc.perform(post("/api/pagos/pagar-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestProcessPaymentDto))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("El QR no puede ser procesado por exceso en el limite de tiempo. Genere uno nuevo.")));
        }

        @Test
        public void process_payment_should_be_NOT_ok_due_insufficient_funds() throws Exception{
            RequestProcessPaymentDto RequestProcessPaymentDto = new RequestProcessPaymentDto("TransactionID",
                    "SenderAccountID", "ACCEPTED");
            when(paymentService.processPayment(any())).thenThrow(new InsufficientFundsException("Fondos insuficientes para realizar el pago."));
            mockMvc.perform(post("/api/pagos/pagar-qr")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(RequestProcessPaymentDto))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("Fondos insuficientes para realizar el pago.")));
        }


    }

    @Nested
    class GetAllPaymentsTests {
        @Test
        public void get_all_should_be_ok() throws Exception {
            var responseRecordPayment = new ResponseRecordPayment("payment1", "name1", "surname1",
                    "sender1", "name2", "surname2", "receiver1",
                    BigDecimal.valueOf(100.5), "un chicle de tutti frutti",
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
            var responseRecordPayment = new ResponseRecordPayment("payment1", "name1", "surname1",
                    "sender1", "name2", "surname2", "receiver1",
                    BigDecimal.valueOf(100.5), "un chicle de tutti frutti",
                    LocalDateTime.of(2024, 9, 20, 12, 05),
                    status);
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
            var responseRecordPayment = new ResponseRecordPayment("payment1", "name1",
                    "surname1", "sender1", "name2", "surname2",
                    "receiver1", BigDecimal.valueOf(100.5), "un chicle de tutti frutti",
                    LocalDateTime.of(2024, 9, 20, 12, 05),
                    TransactionStatus.ACCEPTED);
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

        @Test
        public void get_by_date_should_NOT_be_ok_due_BAD_REQUEST() throws Exception {
            var date = "2024-LALALA-20";
            when(paymentService.getPaymentsByDate(date)).thenThrow(new InvalidDateFormatException("test message"));
            mockMvc.perform(get("/api/pagos/fecha/{date}", date))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetPaymentsBetweenDates{
        @Test
        public void get_between_dates_should_be_ok() throws Exception {
            String fromDate = "2024-10-01";
            String toDate = "2024-10-09";
            ResponseRecordPayment responseRecordPayment = new ResponseRecordPayment("PaymentID", "SenderName",
                    "SenderSurname", "SenderAccountNumber", "ReceiverName", "ReceiverSurname",
                    "ReceiverAccountNumber", BigDecimal.valueOf(100.5), "Transaction Description",
                    LocalDateTime.of(2024, 10, 07, 18, 22),
                    TransactionStatus.ACCEPTED);
            when(paymentService.getPaymentsBetweenDates(fromDate, toDate)).thenReturn(List.of(responseRecordPayment));

            mockMvc.perform(get("/api/pagos/entrefechas")
                    .param("fromDate", fromDate)
                    .param("toDate", toDate))
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
        public void get_between_dates_should_NOT_be_ok_due_user_not_found() throws Exception {
            String fromDate = "2024-10-01";
            String toDate = "2024-10-09";
            when(paymentService.getPaymentsBetweenDates(fromDate, toDate)).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));
            mockMvc.perform(get("/api/pagos/entrefechas")
                    .param("fromDate", fromDate)
                    .param("toDate", toDate))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("Usuario no encontrado")));
        }

        @Test
        public void get_between_dates_should_NOT_be_ok_due_invalid_date_format() throws Exception {
            String fromDate = "01/10/2024";
            String toDate = "09/10/2024";
            when(paymentService.getPaymentsBetweenDates(fromDate, toDate)).thenThrow(new InvalidDateFormatException("Formato de fecha erroneo. Debe ingresar yyyy-MM-dd"));
            mockMvc.perform(get("/api/pagos/entrefechas")
                    .param("fromDate", fromDate)
                    .param("toDate", toDate))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("Formato de fecha erroneo. Debe ingresar yyyy-MM-dd")));
        }

        @Test
        public void get_between_dates_should_NOT_be_ok_due_second_date_before() throws Exception {
            String fromDate = "2024-10-09";
            String toDate = "2024-10-01";
            when(paymentService.getPaymentsBetweenDates(fromDate, toDate)).thenThrow(new ValidationException("La fecha final no puede ser menor a la inicial."));
            mockMvc.perform(get("/api/pagos/entrefechas")
                    .param("fromDate", fromDate)
                    .param("toDate", toDate))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.is("La fecha final no puede ser menor a la inicial.")));
        }

    }

    @Nested
    class GetPaymentsAsClient{
        @Test
        public void get_as_client_should_be_ok() throws Exception {
            ResponseRecordPayment responseRecordPayment = new ResponseRecordPayment("PaymentID", "SenderName",
                    "SenderSurname", "SenderAccountNumber", "ReceiverName", "ReceiverSurname",
                    "ReceiverAccountNumber", BigDecimal.valueOf(100.5), "Transaction Description",
                    LocalDateTime.of(2024, 10, 07, 18, 22),
                    TransactionStatus.ACCEPTED);
            when(paymentService.getPaymentsAsClient()).thenReturn(List.of(responseRecordPayment));

            mockMvc.perform(get("/api/pagos/realizados"))
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
        public void get_as_client_should_NOT_be_ok() throws Exception {
            when(paymentService.getPaymentsAsClient()).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));
            mockMvc.perform(get("/api/pagos/realizados"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("Usuario no encontrado")));
        }
    }

    @Nested
    class GetPaymentsAsSeller {
        @Test
        public void get_as_seller_should_be_ok() throws Exception {
            ResponseRecordPayment responseRecordPayment = new ResponseRecordPayment("PaymentID", "SenderName",
                    "SenderSurname", "SenderAccountNumber", "ReceiverName", "ReceiverSurname",
                    "ReceiverAccountNumber", BigDecimal.valueOf(100.5), "Transaction Description",
                    LocalDateTime.of(2024, 10, 07, 18, 22),
                    TransactionStatus.ACCEPTED);
            when(paymentService.getPaymentsAsSeller()).thenReturn(List.of(responseRecordPayment));

            mockMvc.perform(get("/api/pagos/recibidos"))
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
        public void get_as_seller_should_NOT_be_ok() throws Exception {
            when(paymentService.getPaymentsAsSeller()).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));
            mockMvc.perform(get("/api/pagos/recibidos"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", Matchers.is("Usuario no encontrado")));
        }
    }

}