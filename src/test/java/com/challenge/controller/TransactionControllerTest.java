package com.challenge.controller;

import com.challenge.dto.error.ApiErrorDto;
import com.challenge.dto.transaction.BuyTransactionDto;
import com.challenge.dto.transaction.BuyTransactionResponseDto;
import com.challenge.dto.transaction.DepositTransactionDto;
import com.challenge.dto.user.UserDto;
import com.challenge.entity.User;
import com.challenge.service.JWTService;
import com.challenge.service.UserAuthenticationService;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.challenge.testUtil.TestConstants.BUYER_1;
import static com.challenge.testUtil.TestConstants.BUYER_2;
import static com.challenge.testUtil.TestConstants.PRODUCT_1;
import static com.challenge.testUtil.TestConstants.PRODUCT_2;
import static com.challenge.testUtil.TestConstants.SELLER_1;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringRunner.class)
@DBRider
@DataSet(
        value = {"database.yml"},
        skipCleaningFor = {"flyway_schema_history"},
        cleanAfter = true,
        cleanBefore = true)
public class TransactionControllerTest {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Before
    public void setup() {
        port = 8080;
    }

    @Test
    @DisplayName("Successfully deposit money to user")
    public void depositMoney_successfully() {
        var requestBody = new DepositTransactionDto(100);
        var bearerToken = prepareTokenForRequest(BUYER_1);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/transaction/deposit")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(UserDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getDeposit()).isEqualTo(100);
    }

    @Test
    @DisplayName("Deposit should not be authorized with ROLE_SELLER")
    public void depositMoney_forbidden() {
        var requestBody = new DepositTransactionDto(100);
        var bearerToken = prepareTokenForRequest(SELLER_1);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/transaction/deposit")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Deposit should not go through because of invalid coin")
    public void depositMoney_invalidInput() {
        var requestBody = new DepositTransactionDto(9);
        var bearerToken = prepareTokenForRequest(BUYER_1);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/transaction/deposit")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Buyer should successfully buy product")
    public void buyProduct_successfully() {
        var requestBody = new BuyTransactionDto(PRODUCT_1.getId(), 2);
        var bearerToken = prepareTokenForRequest(BUYER_2);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/transaction/product/buy")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(BuyTransactionResponseDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getTotalSpent()).isEqualTo(20);
        assertThat(response.getChange()).extracting(Integer::intValue).containsOnly(5, 20, 50);
        assertThat(response.getChange().stream().mapToInt(Integer::intValue).sum()).isEqualTo(75);

        assertThat(response.getBoughtProduct().getId()).isEqualTo(PRODUCT_1.getId());
        assertThat(response.getBoughtProduct().getAmountAvailable()).isEqualTo(98);
    }

    @Test
    @DisplayName("Seller should not be able to buy a product")
    public void buyProduct_forbidden() {
        var requestBody = new BuyTransactionDto(PRODUCT_1.getId(), 2);
        var bearerToken = prepareTokenForRequest(SELLER_1);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/transaction/product/buy")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Buyer should not be able to buy more of a product than available")
    public void buyProduct_notEnoughProductAvailable() {
        var requestBody = new BuyTransactionDto(PRODUCT_1.getId(), 101);
        var bearerToken = prepareTokenForRequest(BUYER_2);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/transaction/product/buy")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .extract()
                .as(ApiErrorDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getReason()).asString().contains("Insufficient product amount available");
    }

    @Test
    @DisplayName("Buyer should not be able spend more money than available")
    public void buyProduct_notEnoughFundsAvailable() {
        var requestBody = new BuyTransactionDto(PRODUCT_2.getId(), 2);
        var bearerToken = prepareTokenForRequest(BUYER_2);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/transaction/product/buy")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .extract()
                .as(ApiErrorDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getReason()).asString().contains("Insufficient funds");
    }

    @Test
    @DisplayName("Bought amount should be a positive number and product id should be present")
    public void buyProduct_invalidInput() {
        var requestBody = new BuyTransactionDto(null, -1);
        var bearerToken = prepareTokenForRequest(BUYER_2);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/transaction/product/buy")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(ApiErrorDto.class);

        assertThat(response).isNotNull();
        assertThat(((HashMap) response.getReason()).keySet()).hasSize(2);
    }

    @Test
    @DisplayName("Multiple concurrent transactions should result to the correct final product and buyer state")
    @ExpectedDataSet(value = "transaction-controller/concurentBuyTransaction-expected.yml",
            compareOperation = CompareOperation.CONTAINS)
    public void performMultipleBuyTransactionsConcurrently_successfully() {
        var requestBody = new BuyTransactionDto(PRODUCT_1.getId(), 1);
        var bearerToken = prepareTokenForRequest(BUYER_2);

        // create 5 async requests
        List<CompletableFuture<BuyTransactionResponseDto>> requests = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            CompletableFuture<BuyTransactionResponseDto> request =
                    CompletableFuture.supplyAsync(() ->
                            given()
                                    .contentType(ContentType.JSON)
                                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                                    .body(requestBody)
                                    .post("/api/transaction/product/buy")
                                    .then()
                                    .statusCode(HttpStatus.OK.value())
                                    .extract()
                                    .as(BuyTransactionResponseDto.class)
                    );
            requests.add(request);
        }

        // send async requests
        CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new)).join();
    }

    /**
     * Generate token for a user and save it to the DB as active.
     */
    private String prepareTokenForRequest(User user) {
        String role = user.getRole().getAuthority();
        String token = jwtService.generateToken(user.getUsername(), role, "/api/login");
        userAuthenticationService.saveNewAuthentication(user.getUsername(), token, jwtService.getTokenExpiration(token));
        return "Bearer " + token;
    }
}
