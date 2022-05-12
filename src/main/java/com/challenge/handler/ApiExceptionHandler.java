package com.challenge.handler;

import com.challenge.dto.error.ApiErrorDto;
import com.challenge.exception.exceptions.BuyTransactionException;
import com.challenge.exception.exceptions.InvalidInputException;
import com.challenge.exception.exceptions.PasswordMatchException;
import com.challenge.exception.exceptions.ResourceNotFoundException;
import com.challenge.exception.exceptions.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * API Exception handler class
 */
@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ResourceNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorDto> resourceNotFoundExceptionHandler(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(request, status, ex.getMessage());
    }

    @ExceptionHandler(value = InvalidInputException.class)
    public ResponseEntity<ApiErrorDto> invalidInputExceptionHandler(InvalidInputException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(request, status, ex.getMessage());
    }

    @ExceptionHandler(value = {BuyTransactionException.class, UsernameAlreadyExistsException.class, PasswordMatchException.class})
    public ResponseEntity<ApiErrorDto> conflictExceptionHandler(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        return buildResponse(request, status, ex.getMessage());
    }

    private ResponseEntity<ApiErrorDto> buildResponse(WebRequest request, HttpStatus status, String message) {
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiErrorDto apiErrorDto = new ApiErrorDto(status, message, uri);

        return ResponseEntity.status(status).body(apiErrorDto);
    }

}
