package com.igrowker.nativo.integration;


import com.igrowker.nativo.dtos.contribution.RequestContributionDto;
import com.igrowker.nativo.dtos.contribution.ResponseContributionDto;
import com.igrowker.nativo.dtos.microcredit.RequestMicrocreditDto;
import com.igrowker.nativo.dtos.microcredit.ResponseMicrocreditDto;
import com.igrowker.nativo.entities.*;
import com.igrowker.nativo.repositories.*;
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
public class MicrocreditIntegrationTest {

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

        contribution = new Contribution(null, lenderAccount.getId(), BigDecimal.valueOf(1000.00), null,
                null, microcredit);

        baseURL = "http://localhost:" + port;
    }

    @Nested
    class CreateTest {

        @Test
        public void createMicrocredit_ShouldReturnOk() throws Exception {
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto(microcredit.getTitle(),
                    microcredit.getDescription(), microcredit.getAmount());

            ResponseMicrocreditDto response = given()
                    .baseUri(baseURL)
                    .header("Authorization", borrowerToken)
                    .body(requestMicrocreditDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/solicitar")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("title", Matchers.is(microcredit.getTitle()))
                    .body("description", Matchers.is(microcredit.getDescription()))
                    .body("amount", Matchers.is(microcredit.getAmount().floatValue()))
                    .extract()
                    .as(ResponseMicrocreditDto.class);

            assertThat(response.title()).isEqualTo(microcredit.getTitle());
            assertThat(response.description()).isEqualTo(microcredit.getDescription());
            assertThat(response.amount()).isEqualByComparingTo(microcredit.getAmount());

        }

        @Test
        public void createMicrocredit_wrong_amount_return_400() throws Exception {
            BigDecimal amountWrong = new BigDecimal(6000000.00);
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto(microcredit.getTitle(),
                    microcredit.getDescription(), amountWrong);

            given()
                    .baseUri(baseURL)
                    .header("Authorization", borrowerToken)
                    .body(requestMicrocreditDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/solicitar")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(400)
                    .body("message", Matchers.comparesEqualTo("El monto del microcrédito tiene que ser igual" +
                            " o menor a: $ 500000"));
        }

        @Test
        public void createMicrocredit_negative_amount_return_400() throws Exception {
            BigDecimal amountWrong = new BigDecimal(-1000.00);
            RequestMicrocreditDto requestMicrocreditDto = new RequestMicrocreditDto(microcredit.getTitle(), microcredit.getDescription(), amountWrong);

            given()
                    .baseUri(baseURL)
                    .header("Authorization", borrowerToken)
                    .body(requestMicrocreditDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/solicitar")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(400)
                    .body("message", Matchers.comparesEqualTo("El monto del microcrédito debe ser mayor a $ 0.00"));
        }
    }

    @Nested
    class CreateContribution {

        @Test
        public void createContribution_ShouldReturnOk() throws Exception {
            microcredit = microcreditRepository.save(microcredit);

            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    contribution.getAmount());

            ResponseContributionDto response = given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .body(requestContributionDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/contribuir")
                    .then()
                    .log()
                    .body()
                    .assertThat()
                    .statusCode(200)
                    .body("microcreditId", Matchers.is(microcredit.getId()))
                    .body("amount", Matchers.is(contribution.getAmount().floatValue()))
                    .extract()
                    .as(ResponseContributionDto.class);

            assertThat(response.microcreditId()).isEqualTo(microcredit.getId());
            assertThat(response.amount()).isEqualByComparingTo(contribution.getAmount());
        }


        @Test
        public void createContribution_wrong_id_return_404() throws Exception {
            String microcreditIdWrong = "12345";

            RequestContributionDto requestContributionDto = new RequestContributionDto(microcreditIdWrong,
                    contribution.getAmount());

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .body(requestContributionDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/contribuir")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(404)
                    .body("message", Matchers.comparesEqualTo("Microcrédito no encontrado"));
        }

        @Test
        public void createContribution_wrong_microcredit_is_accepted_return_409() throws Exception {
            microcreditRepository.save(microcredit);
            microcredit.setTransactionStatus(TransactionStatus.ACCEPTED);
            microcreditRepository.save(microcredit);

            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    contribution.getAmount());

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .body(requestContributionDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/contribuir")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(409)
                    .body("message", Matchers.comparesEqualTo("El microcrédito ya tiene la totalidad del " +
                            "monto solicitado."));
        }

        @Test
        public void createContribution_wrong_microcredit_is_completed_return_409() throws Exception {
            microcreditRepository.save(microcredit);
            microcredit.setTransactionStatus(TransactionStatus.COMPLETED);
            microcreditRepository.save(microcredit);

            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    contribution.getAmount());

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .body(requestContributionDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/contribuir")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(409)
                    .body("message", Matchers.comparesEqualTo("No se puede contribuir a un microcrédito con " +
                            "estado COMPLETED"));
        }

        @Test
        public void createContribution_wrong_microcredit_is_expired_return_409() throws Exception {
            microcreditRepository.save(microcredit);
            microcredit.setTransactionStatus(TransactionStatus.EXPIRED);
            microcreditRepository.save(microcredit);

            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    contribution.getAmount());

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .body(requestContributionDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/contribuir")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(409)
                    .body("message", Matchers.comparesEqualTo("No se puede contribuir a un microcrédito con " +
                            "estado EXPIRED"));
        }

        @Test
        public void createContribution_unauthorized_return_401() throws Exception {
            microcreditRepository.save(microcredit);

            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    contribution.getAmount());

            given()
                    .baseUri(baseURL)
                    .header("Authorization", borrowerToken)
                    .body(requestContributionDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/contribuir")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(401)
                    .body("message", Matchers.comparesEqualTo("El usuario contribuyente no puede ser el mismo" +
                            " que el solicitante del microcrédito."));
        }

        @Test
        public void createContribution_lender_insufficient_fund_return_400() throws Exception {
            microcreditRepository.save(microcredit);
            lenderAccount.setAmount(BigDecimal.valueOf(0.00));
            accountRepository.save(lenderAccount);

            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    contribution.getAmount());

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .body(requestContributionDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/contribuir")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(400)
                    .body("message", Matchers.comparesEqualTo("Fondos insuficientes"));
        }

        @Test
        public void createContribution_lender_has_an_expired_microcredit_return_400() throws Exception {
            Microcredit microcreditTest = new Microcredit(null, lenderAccount.getId(), BigDecimal.valueOf(5000.00),
                    null, null, null, null, "Test de integración",
                    "Realizando test de integración", null, null, null,
                    null, null, null);

            microcreditRepository.save(microcreditTest);

            microcreditTest.setTransactionStatus(TransactionStatus.EXPIRED);

            microcreditRepository.save(microcreditTest);

            microcreditRepository.save(microcredit);

            RequestContributionDto requestContributionDto = new RequestContributionDto(microcredit.getId(),
                    contribution.getAmount());

            given()
                    .baseUri(baseURL)
                    .header("Authorization", lenderToken)
                    .body(requestContributionDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/microcreditos/contribuir")
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(400)
                    .body("message", Matchers.comparesEqualTo("No puede contribuir. Presenta un microcrédito vencido. " +
                            "Debe regularizar su deuda."));
        }
    }
}