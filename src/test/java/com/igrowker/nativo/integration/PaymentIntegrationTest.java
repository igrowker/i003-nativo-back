package com.igrowker.nativo.integration;

import com.igrowker.nativo.dtos.payment.RequestPaymentDto;
import com.igrowker.nativo.dtos.payment.RequestProcessPaymentDto;
import com.igrowker.nativo.dtos.payment.ResponsePaymentDto;
import com.igrowker.nativo.dtos.payment.ResponseProcessPaymentDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.ExpiredTransactionException;
import com.igrowker.nativo.exceptions.InsufficientFundsException;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.security.JwtService;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.igrowker.nativo.entities.TransactionStatus.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentIntegrationTest {

    private String token; // token real, aquí no se mockea.
    private User savedUser;
    private User savedUser2;
    private Account savedAccount;
    private Account savedAccount2;
    @LocalServerPort
    private int port;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    /*
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("nativo")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    public static void setup() { //Levantar el container de la db y cerciorarse que corra.
        postgreSQLContainer.start();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @AfterAll
    public static void teardown() { //Cuando termine, se cierra. :D
        postgreSQLContainer.stop();
    }

     */

    @BeforeEach
    public void setupTest() { //Limpiamos db para que no interfieran en tests posteriores.
        userRepository.deleteAll();
        accountRepository.deleteAll();
        paymentRepository.deleteAll();
        savedUser = userRepository.save(new User(null, 123456789l, "Name", "Apellido", "email@gmail.com",
                "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                LocalDateTime.now(), true, "123456",
                LocalDateTime.now().plusMonths(1), true, true, true));
        savedAccount = accountRepository.save(new Account(null, savedUser.getDni(), BigDecimal.valueOf(2500),
                true, savedUser.getId(), BigDecimal.ZERO));
        savedUser2 = userRepository.save(new User(null, 987654321l, "Name2", "Apellido2", "email2@gmail.com",
                "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                LocalDateTime.now(), true, "123456",
                LocalDateTime.now().plusMonths(1), true, true, true));
        savedAccount2 = accountRepository.save(new Account(null, savedUser2.getDni(), BigDecimal.valueOf(2500),
                true, savedUser2.getId(), BigDecimal.ZERO));
        token = "Bearer " + jwtService.generateToken(savedUser);
    }

    @Nested
    class getAllTest {

        @Test
        public void when_call_getAll_and_no_data_should_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get("/api/pagos/todo")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(0));
        }

        @Test
        public void when_call_getAll_and_one_data_should_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;
            Payment payment = new Payment(null, savedUser.getName(), savedUser.getSurname(), savedAccount.getId(),
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), ACCEPTED, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            paymentRepository.save(payment);

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get("/api/pagos/todo")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1));
        }

        @Test
        public void when_call_getAll_with_wrong_token_should_return_404() throws Exception {
            //La idea aquí es generar un token válido para un user diferente del que hace la petición.
            String baseURL = "http://localhost:" + port;
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));
            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);

            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .when()
                    .get("/api/pagos/todo")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }
    }

    @Nested
    class getByStatusTest{

        @Test
        public void when_call_getStatus_and_no_data_should_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get(String.format("/api/pagos/estado/%s", "ACCEPTED"))
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(0));
        }

        @Test
        public void when_call_getStatus_and_one_data_should_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;
            Payment payment = new Payment(null, savedUser.getName(), savedUser.getSurname(), savedAccount.getId(),
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(),  BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), TransactionStatus.PENDING, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            paymentRepository.save(payment);

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get(String.format("/api/pagos/estado/%s", "PENDING"))
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1));
        }

        @Test
        public void when_call_getStatus_with_wrong_token_should_return_404() throws Exception {
            String baseURL = "http://localhost:" + port;
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));
            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);

            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .when()
                    .get(String.format("/api/pagos/estado/%s", "PENDING"))
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }

        @Test
        public void when_call_getStatus_with_wrong_status_should_return_400() throws Exception{
            String baseURL = "http://localhost:" + port;

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get(String.format("/api/pagos/estado/%s", "PENDINGGGGG"))
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400);
        }
    }

    @Nested
    class getByOneDateTest {

        @Test
        public void when_call_getDate_and_no_data_should_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get(String.format("/api/pagos/fecha/%s", "2024-04-09"))
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(0));
        }

        @Test
        public void when_call_getDate_and_one_data_should_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;
            String now = LocalDateTime.now().toLocalDate().toString();
            Payment payment = new Payment(null, savedUser.getName(), savedUser.getSurname(), savedAccount.getId(),
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), ACCEPTED,
                    "un chicle tutti frutti", "lalalalala-qr-lalalala");
            paymentRepository.save(payment);

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get(String.format("/api/pagos/fecha/%s", now))
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1));
        }

        @Test
        public void when_call_getDate_with_wrong_token_should_return_404() throws Exception {
            //La idea aquí es generar un token válido para un user diferente del que hace la petición.
            String baseURL = "http://localhost:" + port;
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));
            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);

            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .when()
                    .get(String.format("/api/pagos/fecha/%s", "2024-04-03"))
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }

        @Test
        public void when_call_getStatus_with_wrong_date_format_should_return_400() throws Exception{
            String baseURL = "http://localhost:" + port;

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get(String.format("/api/pagos/fecha/%s", "2024-44-99"))
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400);
        }

    }

    @Nested
    class getPaymentsBetweenDates{
        @Test
        public void when_call_get_between_dates_and_no_data_should_return_200() throws Exception{
            String baseURL = "http://localhost:" + port;
            String fromDate = LocalDateTime.now().minusDays(7).toLocalDate().toString();
            String toDate = LocalDateTime.now().minusDays(2).toLocalDate().toString();

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .queryParam("fromDate", fromDate)
                    .queryParam("toDate", toDate)
                    .when()
                    .get("/api/pagos/entrefechas")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(0));
        }

        @Test
        public void when_call_get_between_dates_and_one_data_should_return_200() throws Exception{
            String baseURL = "http://localhost:" + port;
            String fromDate = LocalDateTime.now().minusDays(7).toLocalDate().toString();
            String toDate = LocalDateTime.now().plusDays(1).toLocalDate().toString();
            Payment payment = new Payment(null, savedUser.getName(), savedUser.getSurname(), savedAccount.getId(),
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                   LocalDateTime.now() , ACCEPTED, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            paymentRepository.save(payment);

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .queryParam("fromDate", fromDate)
                    .queryParam("toDate", toDate)
                    .when()
                    .get("/api/pagos/entrefechas")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1));
        }

        @Test
        public void when_call_get_between_dates_whit_wrong_token_should_return_404() throws Exception{
            String baseURL = "http://localhost:" + port;
            String fromDate = LocalDateTime.now().minusDays(7).toLocalDate().toString();
            String toDate = LocalDateTime.now().minusDays(2).toLocalDate().toString();
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));
            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);

            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .queryParam("fromDate", fromDate)
                    .queryParam("toDate", toDate)
                    .when()
                    .get("/api/pagos/entrefechas")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }

        @Test
        public void when_call_get_between_dates_whit_invalid_date_format_should_return_400() throws Exception{
            String baseURL = "http://localhost:" + port;
            String fromDate = "02/10/2024";
            String toDate = "10/10/2024";

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .queryParam("fromDate", fromDate)
                    .queryParam("toDate", toDate)
                    .when()
                    .get("/api/pagos/entrefechas")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400);
        }

        @Test
        public void when_call_get_between_dates_whit_second_date_before_should_return_400() throws Exception{
            String baseURL = "http://localhost:" + port;
            String fromDate = LocalDateTime.now().minusDays(2).toLocalDate().toString();
            String toDate = LocalDateTime.now().minusDays(7).toLocalDate().toString();

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .queryParam("fromDate", fromDate)
                    .queryParam("toDate", toDate)
                    .when()
                    .get("/api/pagos/entrefechas")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400);
        }
    }

    @Nested
    class getPaymentsAsClient{
        @Test
        public void when_call_get_as_client_and_no_data_should_return_200() throws Exception {
            String baseURL = "http://localhost:" + port;

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get("/api/pagos/realizados")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(0));
        }

        @Test
        public void when_call_get_as_client_and_one_data_should_return_200() throws Exception{
            String baseURL = "http://localhost:" + port;
            Payment payment = new Payment(null, savedUser.getName(), savedUser.getSurname(), savedAccount.getId(),
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), ACCEPTED, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            paymentRepository.save(payment);

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get("/api/pagos/realizados")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1));
        }

        @Test
        public void when_call_get_as_client_with_wrong_token_should_return_404() throws Exception{
            String baseURL = "http://localhost:" + port;
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));
            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);

            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .when()
                    .get("/api/pagos/realizados")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }
    }

    @Nested
    class getPaymentsAsSeller{
        @Test
        public void when_call_get_as_seller_and_no_data_should_return_200() throws Exception {
            String baseURL = "http://localhost:" + port;

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get("/api/pagos/recibidos")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(0));
        }

        @Test
        public void when_call_get_as_seller_and_one_data_should_return_200() throws Exception{
            String baseURL = "http://localhost:" + port;
            Payment payment = new Payment(null, savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(),
                    savedUser.getName(), savedUser.getSurname(), savedAccount.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), ACCEPTED, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            paymentRepository.save(payment);

            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .when()
                    .get("/api/pagos/recibidos")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1));
        }

        @Test
        public void when_call_get_as_seller_with_wrong_token_should_return_404() throws Exception{
            String baseURL = "http://localhost:" + port;
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));
            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);

            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .when()
                    .get("/api/pagos/recibidos")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }
    }

    @Nested
    class createQrTest{
        @Test
        public void when_call_createQr_and_data_ok_should_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;
            var paymentRequestDto = new RequestPaymentDto(savedAccount.getId(), BigDecimal.valueOf(100.50),
                    "description");
            var paymentSaved = given().baseUri(baseURL)
                    .header("Authorization", token)
                    .body(paymentRequestDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/crear-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("receiverAccount", Matchers.is(savedAccount.getAccountNumber().toString()))
                    .body("amount", Matchers.is(paymentRequestDto.amount().floatValue()))
                    .body("description", Matchers.is(paymentRequestDto.description()))
                    .body("qr", Matchers.notNullValue())
                    .extract()
                    .body()
                    .jsonPath()
                    .getObject("$", ResponsePaymentDto.class);
        }

        @Test
        public void when_call_createQr_with_wrong_token_should_return_404() throws Exception {
            String baseURL = "http://localhost:" + port;
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));
            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);
            var paymentRequestDto = new RequestPaymentDto(savedAccount.getId(), BigDecimal.valueOf(100.50),
                    "description");

            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .body(paymentRequestDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/crear-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }
    }

    @Nested
    class processPayment{
        @Test
        public void when_call_process_payment_with_accepted_should_return_200() throws Exception {
            String baseURL = "http://localhost:" + port;

            Payment incomingPayment = new Payment(null, null, null, null,
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), PENDING, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            Payment payment = paymentRepository.save(incomingPayment);

            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto(payment.getId(), savedAccount.getId(), "ACCEPTED");

            var response = given().baseUri(baseURL)
                    .header("Authorization", token)
                    .body(requestProcessPaymentDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/pagar-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath()
                    .getObject("$", ResponseProcessPaymentDto.class);

            Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();

            assertThat(response.id()).isEqualTo(updatedPayment.getId());
            assertThat(response.senderName()).isEqualTo(updatedPayment.getSenderName());
            assertThat(response.senderSurname()).isEqualTo(updatedPayment.getSenderSurname());
            assertThat(response.senderAccount()).isEqualTo(savedUser.getDni().toString());
            assertThat(response.receiverName()).isEqualTo(updatedPayment.getReceiverName());
            assertThat(response.receiverSurname()).isEqualTo(updatedPayment.getReceiverSurname());
            assertThat(response.receiverAccount()).isEqualTo(savedUser2.getDni().toString());
            assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
            assertThat(response.transactionStatus()).isEqualTo(ACCEPTED);
            assertThat(response.transactionDate()).isEqualTo(updatedPayment.getTransactionDate());
        }

        @Test
        public void when_call_process_payment_whit_denied_should_return_200() throws Exception{
            String baseURL = "http://localhost:" + port;

            Payment incomingPayment = new Payment(null, null, null, null,
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), PENDING, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            Payment payment = paymentRepository.save(incomingPayment);

            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto(payment.getId(),
                    savedAccount.getId(), "DENIED");

            var response = given().baseUri(baseURL)
                    .header("Authorization", token)
                    .body(requestProcessPaymentDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/pagar-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath()
                    .getObject("$", ResponseProcessPaymentDto.class);

            Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();

            assertThat(response.id()).isEqualTo(updatedPayment.getId());
            assertThat(response.senderName()).isEqualTo(updatedPayment.getSenderName());
            assertThat(response.senderSurname()).isEqualTo(updatedPayment.getSenderSurname());
            assertThat(response.senderAccount()).isEqualTo(savedUser.getDni().toString());
            assertThat(response.receiverName()).isEqualTo(updatedPayment.getReceiverName());
            assertThat(response.receiverSurname()).isEqualTo(updatedPayment.getReceiverSurname());
            assertThat(response.receiverAccount()).isEqualTo(savedUser2.getDni().toString());
            assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
            assertThat(response.transactionStatus()).isEqualTo(DENIED);
            assertThat(response.transactionDate()).isEqualTo(updatedPayment.getTransactionDate());
        }

        @Test
        public void when_call_process_payment_with_wrong_token_should_return_404() throws Exception{
            String baseURL = "http://localhost:" + port;
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));
            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);
            Payment incomingPayment = new Payment(null, null, null, null,
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), PENDING, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            Payment payment = paymentRepository.save(incomingPayment);
            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto(payment.getId(), savedAccount.getId(), "ACCEPTED");

            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .body(requestProcessPaymentDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/pagar-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }

        @Test
        public void when_call_process_payment_with_non_existent_payment_should_return_404() throws Exception{
            String baseURL = "http://localhost:" + port;

            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto("1", savedAccount.getId(), "ACCEPTED");
            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .body(requestProcessPaymentDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/pagar-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }

        @Test
        public void when_call_process_payment_with_already_processed_payment_should_return_400() throws Exception{
            String baseURL = "http://localhost:" + port;

            Payment incomingPayment = new Payment(null, null, null, null,
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), PENDING, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            Payment pendingPayment = paymentRepository.save(incomingPayment);
            pendingPayment.setTransactionStatus(ACCEPTED);
            Payment payment = paymentRepository.save(pendingPayment);

            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto(payment.getId(), savedAccount.getId(), "ACCEPTED");


            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .body(requestProcessPaymentDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/pagar-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400);
        }

        @Test
        public void when_call_process_payment_with_expired_payment_should_return_400() throws Exception{
            String baseURL = "http://localhost:" + port;

            Payment incomingPayment = new Payment(null, null, null, null,
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(250.00),
                    LocalDateTime.now(), PENDING, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            Payment elapsedPayment = paymentRepository.save(incomingPayment);
            LocalDateTime elapsed = LocalDateTime.now().minusMinutes(45);
            elapsedPayment.setTransactionDate(elapsed);
            Payment payment = paymentRepository.save(elapsedPayment);

            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto(payment.getId(), savedAccount.getId(), "ACCEPTED");


            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .body(requestProcessPaymentDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/pagar-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400);
        }

        @Test
        public void when_call_process_payment_with_insufficient_funds_should_return_400() throws Exception{
            String baseURL = "http://localhost:" + port;

            Payment incomingPayment = new Payment(null, null, null, null,
                    savedUser2.getName(), savedUser2.getSurname(), savedAccount2.getId(), BigDecimal.valueOf(15250.00),
                    LocalDateTime.now(), PENDING, "un chicle tutti frutti", "lalalalala-qr-lalalala");
            Payment payment = paymentRepository.save(incomingPayment);

            RequestProcessPaymentDto requestProcessPaymentDto = new RequestProcessPaymentDto(payment.getId(), savedAccount.getId(), "ACCEPTED");
            given().baseUri(baseURL)
                    .header("Authorization", token)
                    .body(requestProcessPaymentDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/pagos/pagar-qr")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400);
        }
    }
}