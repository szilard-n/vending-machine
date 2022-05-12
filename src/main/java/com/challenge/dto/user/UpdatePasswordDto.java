package com.challenge.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO class for password update requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordDto {

    @NotNull
    @Size(min = 6, max = 30)
    private String oldPassword;

    @NotNull
    @Size(min = 6, max = 30)
    private String newPassword;

}
