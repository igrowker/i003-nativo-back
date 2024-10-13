package com.igrowker.nativo.integration;

import com.igrowker.nativo.dtos.donation.RequestDonationConfirmationDto;
import com.igrowker.nativo.dtos.donation.RequestDonationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationConfirmationDto;
import com.igrowker.nativo.dtos.donation.ResponseDonationDtoTrue;
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
/*
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

        savedAccount = accountRepository.save(new Account(null, savedUser.getDni(), BigDecimal.ZERO,
                true, savedUser.getId(), BigDecimal.valueOf(300.00)));

        savedUser2 = userRepository.save(new User(null, 987654321L, "Name2", "Apellido2", "email2@gmail.com",
                "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                LocalDateTime.now(), true, "123456",
                LocalDateTime.now().plusMonths(1), true, true, true));

        savedAccount2 = accountRepository.save(new Account(null, savedUser2.getDni(), BigDecimal.ZERO,
                true, savedUser2.getId(), BigDecimal.ZERO));


        savedDonation = donationRepository.save(new Donation(null,BigDecimal.valueOf(100.0), TransactionStatus.PENDING,
                savedAccount.getId(), savedAccount2.getId(),true,LocalDateTime.now(),LocalDateTime.now()));

        // Luego actualizar el BigDecimal
        savedAccount.setAmount(savedAccount.getAmount().add(BigDecimal.valueOf(200)));
        savedAccount = accountRepository.save(savedAccount);

        token = "Bearer " + jwtService.generateToken(savedUser);
    }


    @Nested
    class getAllTestDonation{

        // When call at method create Donation
        @Test
        public void when_call_create_donation_and_data_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;
            var requestDonationDto = new RequestDonationDto(BigDecimal.valueOf(100.0),savedAccount.getId(),savedAccount2.getId(),true);

            var donationSaved = given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/crear-donacion")
                    .then()
                    .log().body()
                    .assertThat().statusCode(200)
                    .body("amount", Matchers.is(savedDonation.getAmount().toString()))
                    .body("accountIdDonor", Matchers.is(savedDonation.getAccountIdDonor()))
                    .body("donorName", Matchers.is(savedUser.getName()))
                    .body("donorLastName", Matchers.is(savedUser.getSurname()))
                    .body("accountIdBeneficiary", Matchers.is(savedDonation.getAccountIdBeneficiary()))
                    .body("beneficiaryName", Matchers.is(savedUser2.getName()))
                    .body("beneficiaryLastName", Matchers.is(savedUser2.getSurname()))
                    .body("createdAt", Matchers.is(savedDonation.getCreatedAt()))
                    .body("status", Matchers.is(savedDonation.getStatus().name()))
                    .extract()
                    .body()
                    .jsonPath()
                    .getObject("$", ResponseDonationDtoTrue.class);
        }

        // When call at method create Donation
        @Test
        public void when_call_confirm_donation_and_data_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;
            var requestDonationConfirmationDto = new RequestDonationConfirmationDto(savedDonation.getId(),savedAccount.getId(),savedAccount2.getId(),TransactionStatus.ACCEPTED);

            var donationSaved = given().baseUri(baseURL)
                    .header("Authorization",token)
                    .body(requestDonationConfirmationDto)
                    .contentType(ContentType.JSON)
                    .when().post("/api/donaciones/confirmar-donacion")
                    .then()
                    .log().body()
                    .assertThat().statusCode(200)
                    .body("amount", Matchers.is(savedDonation.getAmount().toString()))
                    .body("accountIdDonor", Matchers.is(savedDonation.getAccountIdDonor()))
                    .body("accountIdBeneficiary", Matchers.is(savedUser.getName()))
                    .body("status", Matchers.is(TransactionStatus.ACCEPTED.name()))
                    .extract()
                    .body()
                    .jsonPath()
                    .getObject("$", ResponseDonationConfirmationDto.class);
        }

        @Test
        public void when_call_donation_history_donor_and_data_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;

            var donationSaved = given().baseUri(baseURL)
                    .header("Authorization",token)
                    .when().get("/api/donaciones/historial-donaciones/donador/%s",savedAccount.getId())
                    .then()
                    .log().body()
                    .assertThat().statusCode(200)
                    .body("$", Matchers.hasSize(1));
        }

        @Test
        public void when_call_donation_history_donor_and_data_return_error() throws Exception {
            String baseURL = "http://localhost:" + port;

            var donationSaved = given().baseUri(baseURL)
                    .header("Authorization",token)
                    .when().get("/api/donaciones/historial-donaciones/donador/%s","ssssaaa123")
                    .then()
                    .log().body()
                    .assertThat().statusCode(404)
                    .body("message", Matchers.equalTo("La cuenta no existe"))
                    .extract()
                    .body()
                    .asString();
        }

        @Test
        public void when_call_donation_history_beneficiary_and_data_return_ok() throws Exception {
            String baseURL = "http://localhost:" + port;

            var donationSaved = given().baseUri(baseURL)
                    .header("Authorization",token)
                    .when().get("/historial-donaciones/beneficiario/%s",savedAccount2.getId())
                    .then()
                    .log().body()
                    .assertThat().statusCode(404)
                    .body("message", Matchers.equalTo("No hay donaciones recibidas"))
                    .extract()
                    .body()
                    .asString();
        }

        @Test
        public void when_call_donation_history_beneficiary_and_data_return_error() throws Exception {
            String baseURL = "http://localhost:" + port;

            var donationSaved = given().baseUri(baseURL)
                    .header("Authorization",token)
                    .when().get("/historial-donaciones/beneficiario/%s","sssd12333")
                    .then()
                    .log().body()
                    .assertThat().statusCode(404)
                    .body("message", Matchers.equalTo("La cuenta no existe"))
                    .extract()
                    .body()
                    .asString();
        }

    }
}
*/