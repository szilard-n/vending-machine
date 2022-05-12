package com.challenge.exception.exceptions;

/**
 * Exception class for data input validations
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
