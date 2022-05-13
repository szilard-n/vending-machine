package com.challenge.controller;

import com.challenge.dto.error.ApiErrorDto;
import com.challenge.dto.product.DeleteProductDto;
import com.challenge.dto.product.ProductDto;
import com.challenge.dto.product.UpdateProductDto;
import com.challenge.entity.User;
import com.challenge.service.JWTService;
import com.challenge.service.UserAuthenticationService;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

import static com.challenge.testUtil.TestConstants.BUYER_1;
import static com.challenge.testUtil.TestConstants.PRODUCT_1;
import static com.challenge.testUtil.TestConstants.PRODUCT_4;
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
public class ProductControllerTest {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Before
    public void setup() {
        port = 8080;
    }


    /**
     * CREATE PRODUCT
     */
    @Test
    @DisplayName("New product should be created successfully")
    @ExpectedDataSet(value = "product-controller/createProduct-expected.yml",
            compareOperation = CompareOperation.CONTAINS, ignoreCols = "id")
    public void createProduct_successfully() {
        var bearerToken = prepareTokenForRequest(SELLER_1);
        var requestBody = ProductDto.builder()
                .amountAvailable(10)
                .cost(25)
                .productName("New Product")
                .build();

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/product")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("Product should not be created if there are invalid fields")
    public void createProduct_invalidInput() {
        var bearerToken = prepareTokenForRequest(SELLER_1);
        var requestBody = ProductDto.builder()
                .amountAvailable(-1)
                .cost(9)
                .productName(StringUtils.EMPTY)
                .build();

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/product")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(ApiErrorDto.class);

        assertThat(response).isNotNull();
        assertThat(((HashMap) response.getReason()).keySet()).hasSize(3);
    }

    @Test
    @DisplayName("Product should not be created by users with ROLE_BUYER")
    public void createProduct_forbidden() {
        var bearerToken = prepareTokenForRequest(BUYER_1);
        var requestBody = ProductDto.builder()
                .amountAvailable(100)
                .cost(5)
                .productName("New Product")
                .build();

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .post("/api/product")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    /**
     * READ PRODUCT
     */
    @Test
    @DisplayName("All products should be returned for any role")
    public void getProducts_successfully() {
        var bearerToken = prepareTokenForRequest(SELLER_1);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .get("/api/product")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ProductDto[].class);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(3);
    }

    /**
     * UPDATE PRODUCT
     */
    @Test
    @DisplayName("Product should be updated successfully")
    @ExpectedDataSet(value = "product-controller/updateProduct-expected.yml",
            compareOperation = CompareOperation.CONTAINS)
    public void updateProduct_successfully() {
        var bearerToken = prepareTokenForRequest(SELLER_1);
        var requestBody = new UpdateProductDto(PRODUCT_1.getId(),
                "Updated Product", 80, 15);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/product")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("A seller should not be able to update other seller's product")
    public void updateProduct_productNotFound() {
        var bearerToken = prepareTokenForRequest(SELLER_1);
        var requestBody = new UpdateProductDto(PRODUCT_4.getId(),
                "Updated Product", 80, 15);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/product")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Product should not be updated if an id is not provided")
    public void updateProduct_nullProductId() {
        var bearerToken = prepareTokenForRequest(SELLER_1);
        var requestBody = new UpdateProductDto(null, "Updated Product", 80, 15);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/product")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * DELETE PRODUCT
     */
    @Test
    @DisplayName("Product should successfully be deleted")
    @ExpectedDataSet(value = "product-controller/deleteProduct-expected.yml")
    public void deleteProduct_successfully() {
        var bearerToken = prepareTokenForRequest(SELLER_1);
        var requestBody = new DeleteProductDto(PRODUCT_1.getId());

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .delete("/api/product")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("A seller should not be able to delete other seller's product")
    public void deleteProduct_notFound() {
        var bearerToken = prepareTokenForRequest(SELLER_1);
        var requestBody = new DeleteProductDto(PRODUCT_4.getId());

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .delete("/api/product")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
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
