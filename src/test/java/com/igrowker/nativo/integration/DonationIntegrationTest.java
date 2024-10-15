package com.igrowker.nativo.integration;

import com.igrowker.nativo.dtos.donation.*;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Donation;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.DonationRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.security.JwtService;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DonationIntegrationTest {

    private String token;
    private User savedUser;
    private User savedUser2;
    private Account savedAccount;
    private Account savedAccount2;
    private Donation savedDonation;

    @LocalServerPort
    private int port;

    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    // Clear database
    @BeforeEach
    public void setupTest() {
        userRepository.deleteAll();
        accountRepository.deleteAll();
        donationRepository.deleteAll();

        savedUser = userRepository.save(new User(null, 123456789L, "Name", "Apellido", "email@gmail.com",
                "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                LocalDateTime.now(), true, "123456",
                LocalDateTime.now().plusMonths(1), true, true, true));

        savedAccount = accountRepository.save(new Account(null, 20600747701L, BigDecimal.valueOf(300),
                true, savedUser.getId(), BigDecimal.ZERO));

        savedUser2 = userRepository.save(new User(null, 987654321L, "Name2", "Apellido2", "email2@gmail.com",
                "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                LocalDateTime.now(), true, "123456",
                LocalDateTime.now().plusMonths(1), true, true, true));

        savedAccount2 = accountRepository.save(new Account(null, 20600747702L, BigDecimal.valueOf(300),
                true, savedUser2.getId(), BigDecimal.ZERO));


        savedDonation = donationRepository.save(new Donation(null,BigDecimal.valueOf(100.0), null,
                savedAccount.getId(), savedAccount2.getId(),true,null,null));

        token = "Bearer " + jwtService.generateToken(savedUser);
    }


    @Nested
    class getAllTestDonation{

        @Test
        public void when_call_create_donation_true_and_data_return_ok() throws Exception {

            String baseURL = "http://localhost:" + port;
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),savedAccount2.getAccountNumber(),true);

            ResponseDonationDtoTrue response = given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/crear-donacion")
                    .then()
                    .log().body()
                    .assertThat()
                    .statusCode(200)
                    .body("amount", Matchers.is(savedDonation.getAmount().floatValue()))
                    .body("donorName", Matchers.is(savedUser.getName()))
                    .body("donorLastName", Matchers.is(savedUser.getSurname()))
                    .body("beneficiaryAccountNumber", Matchers.is(savedAccount2.getAccountNumber()))
                    .body("beneficiaryName", Matchers.is(savedUser2.getName()))
                    .body("beneficiaryLastName", Matchers.is(savedUser2.getSurname()))
                    .body("status", Matchers.is(TransactionStatus.PENDING.name()))
                    .extract()
                    .body()
                    .jsonPath()
                    .getObject("$", ResponseDonationDtoTrue.class);
        }

        @Test
        public void when_call_create_donation_false_and_data_return_ok() throws Exception {

            String baseURL = "http://localhost:" + port;
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),savedAccount2.getAccountNumber(),false);

            ResponseDonationDtoFalse response = given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/crear-donacion")
                    .then()
                    .log().body()
                    .assertThat()
                    .statusCode(200)
                    .body("amount", Matchers.is(savedDonation.getAmount().floatValue()))
                    .body("beneficiaryAccountNumber", Matchers.is(savedAccount2.getAccountNumber()))
                    .body("beneficiaryName", Matchers.is(savedUser2.getName()))
                    .body("beneficiaryLastName", Matchers.is(savedUser2.getSurname()))
                    .body("status", Matchers.is(TransactionStatus.PENDING.name()))
                    .extract()
                    .body()
                    .jsonPath()
                    .getObject("$", ResponseDonationDtoFalse.class);
        }

        @Test
        public void when_call_create_donation_false_and_data_return_404() throws Exception {

            String baseURL = "http://localhost:" + port;
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),12345L,false);

            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/crear-donacion")
                    .then()
                    .log().body()
                    .assertThat()
                    .statusCode(404);
        }

        @Test
        public void when_call_create_donation_false_with_number_account_invalid() throws Exception {

            String baseURL = "http://localhost:" + port;
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),12345L,false);

            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/crear-donacion")
                    .then()
                    .log().body()
                    .assertThat()
                    .statusCode(404);
        }

        @Test
        public void when_call_create_donation_false_with_anonymous_invalid() throws Exception {

            String baseURL = "http://localhost:" + port;
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),12345L,null);

            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/crear-donacion")
                    .then()
                    .log().body()
                    .assertThat()
                    .statusCode(400);
        }

        @Test
        public void when_call_create_donation_false_with_amount_less_than_one_hundred() throws Exception {

            String baseURL = "http://localhost:" + port;
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(10),savedAccount2.getAccountNumber(),false);
            String message = "amount: El monto tiene que tener un mínimo de $100";

            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/crear-donacion")
                    .then()
                    .log().body()
                    .assertThat()
                    .statusCode(400)
                    .body("message", Matchers.is(message));
        }

        @Test
        public void when_call_confirmation_donation_and_data_return_401() throws Exception{
            String message = "La cuenta indicada no coincide con el usuario logueado en la aplicación";
            String baseURL = "http://localhost:" + port;
            var requestDonationConfirmationDtoDto = new RequestDonationConfirmationDto(savedDonation.getId(),TransactionStatus.ACCEPTED);

            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationConfirmationDtoDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/confirmar-donacion")
                    .then()
                    .log().body()
                    .assertThat()
                    .statusCode(401)
                    .body("message", Matchers.is(message));
        }

        @Test
        public void when_call_record_donation_donor_and_data_return_400_by_token_error() throws Exception {
            User userNotFound = (new User(null, 123456788l, "Name", "Apellido", "notfound@gmail.com",
                    "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                    LocalDateTime.now(), true, "123456",
                    LocalDateTime.now().plusMonths(1), true, true, true));

            var savedUserNotFound = userRepository.save(userNotFound);
            String tokenNotFound = jwtService.generateToken(savedUserNotFound);

            String baseURL = "http://localhost:" + port;
            given().baseUri(baseURL)
                    .header("Authorization", "Bearer " + tokenNotFound)
                    .pathParam("idDonorAccount", savedAccount.getId())
                    .when()
                    .get("/api/donaciones/historial-donaciones/donador/{idDonorAccount}")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404);
        }

        @Test
        public void when_call_record_beneficiary_and_data_return_400_by_beneficiary_account_non_exist() throws Exception {
            String idBeneficiaryAccountNull = "4432232";
            String message = "La cuenta provista no fue encontrada.";
            String baseURL = "http://localhost:" + port;
            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .pathParam("idBeneficiaryAccount", idBeneficiaryAccountNull)
                    .when()
                    .get("/api/donaciones/historial-donaciones/beneficiario/{idBeneficiaryAccount}")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404)
                    .body("message", Matchers.is(message));
        }

        @Test
        public void when_call_record_donor_and_data_return_400_by_beneficiary_account_non_exist() throws Exception {
            String idDonorAccount = "4432232";
            String message = "La cuenta provista no fue encontrada.";
            String baseURL = "http://localhost:" + port;
            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .pathParam("idDonorAccount", idDonorAccount)
                    .when()
                    .get("/api/donaciones/historial-donaciones/donador/{idDonorAccount}")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404)
                    .body("message", Matchers.is(message));
        }

        @Test
        public void when_call_record_by_between_dates_or_status_and_data_return_404() throws Exception {
            String message = "Se debe de ingresar las fechas de inicio y fin o un status";
            String baseURL = "http://localhost:" + port;
            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .when()
                    .get("/api/donaciones/historial-donaciones")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404)
                    .body("message", Matchers.is(message));
        }

        @Test
        public void when_call_record_by_between_dates_or_status_with_dates_invalid_format() throws Exception {
            String message = "Formato de fecha erroneo. Debe ingresar yyyy-MM-dd";
            String fromDate = "2024/02/12", toDate="2024/02/12";
            String baseURL = "http://localhost:" + port;
            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .queryParam("fromDate", fromDate)
                    .queryParam("toDate", toDate)
                    .when()
                    .get("/api/donaciones/historial-donaciones")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400)
                    .body("message", Matchers.is(message));
        }

        @Test
        public void when_call_record_by_between_dates_or_status_with_status_invalid() throws Exception {
            String message = "El estado de la transacción no existe: ACC";
            String status = "ACC";
            String baseURL = "http://localhost:" + port;
            given().baseUri(baseURL)
                    .header("Authorization",token)
                    .queryParam("status", status)
                    .when()
                    .get("/api/donaciones/historial-donaciones")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(400)
                    .body("message", Matchers.is(message));
        }

    }
}