package com.challenge.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.UUID;

/**
 * DTO class representing a buy transaction request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyTransactionDto {

    @NotNull
    private UUID productId;

    @Positive
    private int amount;

}
