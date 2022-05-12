package com.challenge.exception.exceptions;

/**
 * Exception class for buy transaction issues
 */
public class BuyTransactionException extends RuntimeException {

    public BuyTransactionException(String message) {
        super(message);
    }
}
