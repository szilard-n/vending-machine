package com.challenge.exception.exceptions;

/**
 * Exception class for situations where a username already exists
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
