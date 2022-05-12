package com.challenge.exception.exceptions;

/**
 * Exception class for old and new passwords not matching on
 * password update request.
 */
public class PasswordMatchException extends RuntimeException {

    public PasswordMatchException(String message) {
        super(message);
    }
}
