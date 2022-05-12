package com.challenge.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for authentication response containing tht JWT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    @JsonProperty("message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String responseMessage;

    @JsonProperty("token")
    private String accessToken;

}
