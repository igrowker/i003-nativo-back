package com.igrowker.nativo.integration;

import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Payment;
import com.igrowker.nativo.entities.TransactionStatus;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.PaymentRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.security.JwtService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerIntegrationTest {

    private String token; // token real, aquí no se mockea.
    private User savedUser;
    private Account savedAccount;
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

    @BeforeEach
    public void setupTest() { //Limpiamos db para que no interfieran en tests posteriores.
        userRepository.deleteAll();
        accountRepository.deleteAll();
        paymentRepository.deleteAll();
        savedUser = userRepository.save(new User(null, 123456789l, "Name", "Apellido", "email@gmail.com",
                "password123", "123654789", null, LocalDate.of(1990, 12, 31),
                LocalDateTime.now(), true, "123456",
                LocalDateTime.now().plusMonths(1), true, true, true));
        savedAccount = accountRepository.save(new Account(null, savedUser.getDni(), BigDecimal.ZERO,
                true, savedUser.getId(), BigDecimal.ZERO));
        token = "Bearer " + jwtService.generateToken(savedUser);
    }

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
        Payment payment = new Payment(null, savedAccount.getId(), "receiver", BigDecimal.valueOf(250.00),
                LocalDateTime.now(), TransactionStatus.ACCEPTED, "un chicle tutti frutti", "lalalalala-qr-lalalala");
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


/*
    @Test
    public void when_call_save_and_data_ok_should_return_ok() throws Exception {

        String baseURL = "http://localhost:" + port;

        var hero = new HeroModel(0l, "new_hero_man");


        var heroSaved = given().baseUri(baseURL)
                .header("Authorization", token)
                .body(hero)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/hero")
                .then()
                .log()
                .body()
                .assertThat()
                .statusCode(200)
                .body("ok", Matchers.is(true))
                .body("errors", Matchers.hasSize(0))
                .extract()
                .body()
                .jsonPath()
                .getObject("object", HeroModel.class);

        given().baseUri(baseURL)
                .header("Authorization", token)
                .when()
                .get("/api/hero/"+ heroSaved.id())
                .then()
                .log()
                .body()
                .assertThat()
                .statusCode(200)
                .body("ok", Matchers.is(true))
                .body("object.id", Matchers.is(heroSaved.id().intValue()))
                .body("errors", Matchers.hasSize(0));
    }
}

 */