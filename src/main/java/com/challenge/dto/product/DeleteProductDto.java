package com.challenge.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO class for product removal requests
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteProductDto {

    @NotNull
    private UUID id;
}
