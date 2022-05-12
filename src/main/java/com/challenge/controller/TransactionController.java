package com.challenge.controller;

import com.challenge.dto.transaction.BuyTransactionDto;
import com.challenge.dto.transaction.BuyTransactionResponseDto;
import com.challenge.dto.transaction.DepositTransactionDto;
import com.challenge.dto.user.UserDto;
import com.challenge.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST Controller containing endpoints related to buyer's transactions
 */
@RestController
@RequestMapping(value = "/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/product/buy")
    public ResponseEntity<BuyTransactionResponseDto> performBuyTransaction(@Valid @RequestBody BuyTransactionDto buyTransaction) {
        BuyTransactionResponseDto responseDto = transactionService.performBuyTransaction(buyTransaction.getProductId(), buyTransaction.getAmount());
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/deposit/reset")
    public ResponseEntity<UserDto> resetDeposit() {
        return ResponseEntity.ok(transactionService.resetUserDeposit());
    }

    @PutMapping("/deposit")
    public ResponseEntity<UserDto> makeDeposit(@Valid @RequestBody DepositTransactionDto deposit) {
        UserDto user = transactionService.depositCoin(deposit.getAmount());
        return ResponseEntity.ok(user);
    }

}
