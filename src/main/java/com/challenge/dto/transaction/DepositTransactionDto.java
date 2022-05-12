package com.challenge.dto.transaction;

import com.challenge.validation.constraints.AllowedCoin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class representing a money deposit transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositTransactionDto {

    @AllowedCoin
    private int amount;

}
