package com.challenge.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * DTO class for user details update requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto {

    @NotNull
    @Size(min = 6, max = 20)
    private String newUsername;

    @NotNull
    @Pattern(regexp = "(ROLE_[A-Z]+)\\w")
    private String newRole;

}
