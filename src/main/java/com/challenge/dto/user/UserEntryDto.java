package com.challenge.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * DTO class for new user registration and login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntryDto {

    @NotNull
    @Size(min = 6, max = 20)
    private String username;

    @NotNull
    @Size(min = 6, max = 30)
    private String password;

    @NotNull
    @Pattern(regexp = "(ROLE_[A-Z]+)\\w")
    private String role;

}
