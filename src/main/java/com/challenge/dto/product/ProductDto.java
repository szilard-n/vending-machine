package com.challenge.dto.product;

import com.challenge.dto.user.UserDto;
import com.challenge.validation.constraints.ProductCost;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import java.util.UUID;

/**
 * DTO class for {@link com.challenge.entity.Product}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {

    private UUID id;

    @NotEmpty
    private String productName;

    @PositiveOrZero
    private int amountAvailable;

    @ProductCost
    private int cost;

    private UserDto seller;
}
