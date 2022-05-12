package com.challenge.dto.product;

import com.challenge.validation.constraints.ProductCost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.UUID;

/**
 * DTO class for updating product details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductDto {

    @NotNull
    private UUID id;

    @NotEmpty
    private String productName;

    @PositiveOrZero
    private int amountAvailable;

    @ProductCost
    private int cost;
}
