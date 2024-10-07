package com.igrowker.nativo.integration;

import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Contribution;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.ContributionRepository;
import com.igrowker.nativo.repositories.MicrocreditRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.security.JwtService;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContributionIntegrationTest {

    private String borrowerToken;
    private String lenderToken;
    private User borrowerUser;
    private User lenderUser;
    private Account borrowerAccount;
    private Account lenderAccount;
    @LocalServerPort
    private int port;
    @Autowired
    private MicrocreditRepository microcreditRepository;
    @Autowired
    private ContributionRepository contributionRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    private String baseURL;
    private Microcredit microcredit;
    private Contribution contribution;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("nativo")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    public static void setup() {
        postgreSQLContainer.start();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @AfterAll
    public static void teardown() { //Cuando termine, se cierra. :D
        postgreSQLContainer.stop();
    }

    @BeforeEach
    public void setupTest() {
        userRepository.deleteAll();
        accountRepository.deleteAll();
        contributionRepository.deleteAll();
        microcreditRepository.deleteAll();

        borrowerUser = userRepository.save(new User(null, 123456789l, "Borrower", "Test", "borrower.test@gmail.com",
                "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                LocalDateTime.now(), true, "123456",
                LocalDateTime.now().plusMonths(1), true, true, true));
        borrowerAccount = accountRepository.save(new Account(null, borrowerUser.getDni(), BigDecimal.ZERO,
                true, borrowerUser.getId(), BigDecimal.ZERO));
        lenderUser = userRepository.save(new User(null, 987654321l, "Lender", "Test", "lender.test@gmail.com",
                "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                LocalDateTime.now(), true, "123456",
                LocalDateTime.now().plusMonths(1), true, true, true));
        lenderAccount = accountRepository.save(new Account(null, lenderUser.getDni(), BigDecimal.valueOf(100000.00),
                true, lenderUser.getId(), BigDecimal.ZERO));
        borrowerToken = "Bearer " + jwtService.generateToken(borrowerUser);
        lenderToken = "Bearer " + jwtService.generateToken(lenderUser);

        microcredit = new Microcredit(null, borrowerAccount.getId(), BigDecimal.valueOf(5000.00), null, null, null, null, "Test de integración", "Realizando test de integración", null, null, null, null, null, null);
        microcredit = microcreditRepository.save(microcredit);

        contribution = new Contribution(null, lenderAccount.getId(), BigDecimal.valueOf(1000.00), null,
                null, microcredit);

        baseURL = "http://localhost:" + port;
    }

    @Nested
    class getAllContributionTest {

        @Test
        public void getAllContributions_return_200() throws Exception {
            contribution = contributionRepository.save(contribution);

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .when()
                    .get("/api/contribuciones")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1))
                    .body("[0].microcreditId", Matchers.equalTo(microcredit.getId()))
                    .body("[0].amount", Matchers.is(contribution.getAmount().floatValue()));
        }

        @Test
        public void getAllContributions_notFoundMicrocredit_return_404() throws Exception {

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .when()
                    .get("/api/contribuciones")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(404)
                    .body("message", Matchers.comparesEqualTo("No se encontraron contribuciones."));
        }

        @Test
        public void getAllContributionsByUser_return_200() throws Exception {
            contribution = contributionRepository.save(contribution);

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .when()
                    .get("/api/contribuciones/usuario-logueado")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1))
                    .body("[0].microcreditId", Matchers.equalTo(microcredit.getId()))
                    .body("[0].amount", Matchers.is(contribution.getAmount().floatValue()));
        }

        @Test
        public void getAllContributionsByUser_notFoundContributions_return_404() throws Exception {

            given()
                    .baseUri(baseURL)
                    .header("Authorization", borrowerToken)
                    .when()
                    .get("/api/contribuciones/usuario-logueado")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(404)
                    .body("message", Matchers.comparesEqualTo("No se encontraron contribuciones."));
        }
    }

    @Nested
    class getAllContributionsByUserByStatus {
        public void getAllContributions_return_200() throws Exception {
            contribution = contributionRepository.save(contribution);

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .when()
                    .get("/api/contribuciones/%s" + "ACCEPTED")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("$", Matchers.hasSize(1))
                    .body("[0].microcreditId", Matchers.equalTo(microcredit.getId()))
                    .body("[0].amount", Matchers.is(contribution.getAmount().floatValue()));
        }
    }

    @Nested
    class getOneContribution {
        @Test
        public void getOneContributionById_return_200() throws Exception {
            contribution = contributionRepository.save(contribution);

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .when()
                    .get("/api/contribuciones/" + contribution.getId())
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("microcreditId", Matchers.equalTo(microcredit.getId()))
                    .body("amount", Matchers.is(contribution.getAmount().floatValue()))
                    .body("transactionStatus", Matchers.equalTo("ACCEPTED"));
        }

        @Test
        public void getOneContributionById_notFoundCont_return_404() throws Exception {

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .when()
                    .get("/api/contribuciones/" + contribution.getId())
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(404)
                    .body("message", Matchers.comparesEqualTo("Contribución no encontrada con id: " + contribution.getId()));

        }
    }
}

