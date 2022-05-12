package com.challenge.dto.transaction;

import com.challenge.dto.product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO class representing the response from a buy transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyTransactionResponseDto {

    private int totalSpent;
    private ProductDto boughtProduct;
    private List<Integer> change;

}
