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


        // When call at method create Donation
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
        public void when_call_create_donation_false_and_data_return_403() throws Exception {

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
        public void when_call_record_donation_beneficiary_and_data_return_ok() throws Exception {


            var responseRecordDonation = new ResponseDonationRecord("e17efc6c-6d57-4542-8ac1-637251e7662b",
                    BigDecimal.valueOf(100.0),
                    "Pedro", "Pascal", "348ad942-10aa-42b8-8173-a763c8d9b7e3",
                    "Natalia", "Lafourcade", "218d6f62-d5cf-423d-a0ac-4df8d7f1d06c",
                    TransactionStatus.ACCEPTED, LocalDateTime.now().minusDays(2), LocalDateTime.now());

        }


    }
}