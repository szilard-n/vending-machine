package com.challenge.exception.exceptions;

/**
 * Exception class for not found resources
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
