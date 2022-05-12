package com.challenge.service;

import com.challenge.dto.transaction.BuyTransactionResponseDto;
import com.challenge.dto.user.UserDto;
import com.challenge.entity.Product;
import com.challenge.entity.User;
import com.challenge.exception.ExceptionFactory;
import com.challenge.exception.exceptions.BuyTransactionException;
import com.challenge.mapper.ProductMapper;
import com.challenge.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for logic related to a user's deposit account transactions
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    /**
     * Error message keys
     */
    private static final String INSUFFICIENT_FUNDS = "exception.buyTransaction.insufficientFunds";
    private static final String INSUFFICIENT_PRODUCT = "exception.buyTransaction.insufficientProduct";

    @Value("#{'${user.deposit.allowedCoins}'.split(',')}")
    private final Set<Integer> allowedCoins;

    private final UserService userService;
    private final ProductService productService;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    /**
     * Sets the user's deposit to 0.
     */
    @Transactional
    public UserDto resetUserDeposit() {
        User user = userService.getAuthenticatedUser();
        user.setDeposit(0);
        return userMapper.entityToDto(user);
    }

    /**
     * Adds money to the user's deposit if the coin is valid.
     */
    @Transactional
    public UserDto depositCoin(int amount) {
        User user = userService.getAuthenticatedUser();
        user.addToDepositAccount(amount);

        return userMapper.entityToDto(user);
    }

    /**
     * Performs buy transaction by removing the bought amount from seller and
     * removing the spent money from the buyer and adding it to the seller.
     */
    @Transactional
    public BuyTransactionResponseDto performBuyTransaction(UUID productId, int amountToBuy) {
        Product product = productService.getProductByIdLocked(productId);
        User buyer = userService.getAuthenticatedUser();

        int totalCost = product.getCost() * amountToBuy;
        validateBuyTransaction(totalCost, product, buyer, amountToBuy);

        // perform transaction between seller and buyer
        product.subtractFromAmountAvailable(amountToBuy);
        product.getSeller().addToDepositAccount(totalCost);
        int change = buyer.subtractFromDepositAccount(totalCost);

        List<Integer> changeList = getChangeAsValidCoinsList(change);
        return new BuyTransactionResponseDto(totalCost, productMapper.entityToDto(product), changeList);
    }

    /**
     * Validates buy transaction by checking the following conditions, which are not allowed:
     * 1. More products are being bought than are available
     * 2. The buyer doesn't have enough deposit to buy the requested product
     */
    private void validateBuyTransaction(int totalCost, Product productToBuy, User buyer, int amountToBuy) {
        if (productToBuy.getAmountAvailable() < amountToBuy) {
            throw ExceptionFactory.create(BuyTransactionException.class, INSUFFICIENT_PRODUCT);
        } else if (buyer.getDeposit() - totalCost < 0) {
            throw ExceptionFactory.create(BuyTransactionException.class, INSUFFICIENT_FUNDS);
        }
    }

    /**
     * Builds a list of all the valid coins. The sum of all the coins
     * from the list is equal to the change.
     *
     * @param change sum of coins we want to reach
     * @return list of valid coins adding up to the total change.
     */
    private List<Integer> getChangeAsValidCoinsList(int change) {
        if (change != 0) {
            // we order the allowed coins in descending order
            List<Integer> validCoinsDescOrder = allowedCoins.stream()
                    .sorted((v1, v2) -> Integer.compare(v2, v1))
                    .collect(Collectors.toList());

            List<Integer> changeList = new ArrayList<>();
            int changeCounter = 0;

            while (changeCounter != change) {
                // find next valid coin from list and add it to the changeCounter and the changeList
                int validCoin = findNextValidCoin(validCoinsDescOrder, changeCounter, change);
                changeList.add(validCoin);
                changeCounter += validCoin;
            }

            return changeList;
        }

        return Collections.emptyList();
    }

    /**
     * The method iterates through the ordered valid coins list and finds the
     * first coin that meets the following condition: (coin + sumOfValidCoins) <= totalChange.
     *
     * @param validCoins      list of valid coins in descending order
     * @param sumOfValidCoins sum of all valid coins found until current iteration
     * @param totalChange     total change that we want to get after all iterations
     * @return the next valid coin
     */
    private int findNextValidCoin(List<Integer> validCoins, int sumOfValidCoins, int totalChange) {
        return validCoins.stream()
                .filter(coin -> (coin + sumOfValidCoins) <= totalChange)
                .findFirst()
                .orElseThrow(); // exception will not be thrown as there should always be a value
    }
}
