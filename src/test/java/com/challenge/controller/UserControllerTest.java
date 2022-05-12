package com.challenge.controller;

import com.challenge.dto.error.ApiErrorDto;
import com.challenge.dto.user.UpdatePasswordDto;
import com.challenge.dto.user.UpdateUserDto;
import com.challenge.dto.user.UserDto;
import com.challenge.dto.user.UserEntryDto;
import com.challenge.entity.RoleType;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.challenge.testUtil.TestConstants.BUYER_1;
import static com.challenge.testUtil.TestConstants.BUYER_2;
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
public class UserControllerTest {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Before
    public void setup() {
        port = 8080;
    }

    /**
     * CREATE USER
     */
    @Test
    @DisplayName("A new user should be created without auth")
    @ExpectedDataSet(value = "user-controller/createUser-expected.yml",
            ignoreCols = {"id", "password"}, compareOperation = CompareOperation.CONTAINS)
    public void createUser_successfully() {
        var requestBody = new UserEntryDto("testUser", "testPwd", "ROLE_SELLER");

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/user")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(UserDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getDeposit()).isEqualTo(0);
        assertThat(response.getUsername()).isEqualTo(requestBody.getUsername());
        assertThat(response.getRole()).isEqualTo(requestBody.getRole());
    }

    @Test
    @DisplayName("User should not be created if there are invalid fields")
    public void createUser_invalidInput() {
        var requestBody = new UserEntryDto("", null, "invalid_role_patter");

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/user")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(ApiErrorDto.class);

        assertThat(response).isNotNull();
        assertThat(((HashMap) response.getReason()).keySet()).hasSize(3);
    }

    @Test
    @DisplayName("User should not be created if there are invalid fields")
    public void createUser_inexistentRole() {
        var requestBody = new UserEntryDto("newUser", "password", "ROLE_INEXISTENT");

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/user")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(ApiErrorDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getReason()).asString().contains("Invalid role. Available roles: " + Arrays.toString(RoleType.values()));
    }

    @Test
    @DisplayName("User should not be created if the username already exists")
    public void createUser_usernameAlreadyExists() {
        var requestBody = new UserEntryDto(SELLER_1.getUsername(), "password", "ROLE_SELLER");

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/user")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    /**
     * GET USERS
     */
    @Test
    @DisplayName("Get all users successfully")
    public void getAllUsers_successfully() {
        var bearerToken = prepareTokenForRequest(SELLER_1);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .get("/api/user")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(UserDto[].class);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(4);
    }

    @Test
    @DisplayName("Getting users should not work without authentication")
    public void getAllUsers_forbidden() {

        given()
                .contentType(ContentType.JSON)
                .get("/api/user")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    /**
     * UPDATE USER
     */
    @Test
    @DisplayName("Username and role should be updated successfully")
    @ExpectedDataSet(value = "user-controller/updateUsernameAndRole-expected.yml",
            compareOperation = CompareOperation.CONTAINS)
    public void updateUsernameAndRole_successfully() {
        var requestBody = new UpdateUserDto("buyerToSeller", "ROLE_SELLER");
        var bearerToken = prepareTokenForRequest(BUYER_1);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/user")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(UserDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(requestBody.getNewUsername());
        assertThat(response.getRole()).isEqualTo(requestBody.getNewRole());
    }

    @Test
    @DisplayName("User should not be updated without authentication")
    public void updateUsernameAndRole_forbidden() {
        var requestBody = new UpdateUserDto("buyerToSeller", "ROLE_SELLER");

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/api/user")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

    }

    @Test
    @DisplayName("User should not be updated with invalid inputs")
    public void updateUsernameAndRole_invalidInput() {
        var requestBody = new UpdateUserDto(null, "invalid_rol_pattern");
        var bearerToken = prepareTokenForRequest(BUYER_2);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/user")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(ApiErrorDto.class);

        assertThat(response).isNotNull();
        assertThat(((HashMap) response.getReason()).keySet()).hasSize(2);
    }

    @Test
    @DisplayName("User should not be updated if new username is already in use")
    public void updateUsernameAndRole_usernameAlreadyExists() {
        var requestBody = new UpdateUserDto(SELLER_1.getUsername(), "ROLE_SELLER");
        var bearerToken = prepareTokenForRequest(BUYER_2);

        var response = given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/user")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .extract()
                .as(ApiErrorDto.class);

        assertThat(response).isNotNull();
        assertThat(response.getReason()).asString().contains("Username already in use");
    }


    @Test
    @DisplayName("Update user's password successfully")
    public void updatePassword_successfully() {
        var requestBody = new UpdatePasswordDto("password", "newPassword");
        var bearerToken = prepareTokenForRequest(BUYER_1);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/user/password")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("User should not be updated without authentication")
    public void updatePassword_forbidden() {
        var requestBody = new UpdatePasswordDto("password", "newPassword");

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/api/user/password")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

    }

    @Test
    @DisplayName("Update user's password successfully")
    public void updatePassword_oldPasswordDoesNotMatch() {
        var requestBody = new UpdatePasswordDto("pwd123", "newPassword");
        var bearerToken = prepareTokenForRequest(BUYER_1);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(requestBody)
                .put("/api/user/password")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    /**
     * DELETE USER
     */
    @Test
    @DisplayName("User should be deleted successfully")
    @ExpectedDataSet(value = "user-controller/deleteUser-expected.yml")
    public void deleteUser_successfully() {
        var bearerToken = prepareTokenForRequest(SELLER_1);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .delete("/api/user")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deleting users should not work if not logged in")
    public void deleteUser_forbidden() {

        given()
                .contentType(ContentType.JSON)
                .delete("/api/user")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("User should be logged out from all sessions")
    public void logoutAll_successfully() {

        // create 3 sessions
        var tokens = Stream.of(IntStream.range(0, 3))
                .map(i -> prepareTokenForRequest(SELLER_1))
                .collect(Collectors.toList());

        assertThat(userAuthenticationService.isUserAlreadyAuthenticated(SELLER_1.getUsername())).isEqualTo(true);

        // log our from all
        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, tokens.get(0))
                .post("/api/user/logout/all")
                .then()
                .statusCode(HttpStatus.OK.value());

        assertThat(userAuthenticationService.isUserAlreadyAuthenticated(SELLER_1.getUsername())).isEqualTo(false);
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
