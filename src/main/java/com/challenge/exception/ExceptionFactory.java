package com.challenge.exception;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Factory class for creating exception objects with
 * the error message loaded from properties
 */
@Slf4j
public final class ExceptionFactory {

    /**
     * Properties file base name
     */
    private static final String BASE_NAME = "messages";

    private ExceptionFactory() {
    }

    /**
     * Creates a {@link RuntimeException} instance based on the provided class type. The instance
     * is constructed including the error message.
     *
     * @param exceptionClass exception class type that extends {@link RuntimeException}
     * @param messageKey     key of the error message that will be loaded from properties the file
     * @param args           arguments (if any) that will be inserted in the error message
     * @return a new exception object
     */
    public static RuntimeException create(Class<? extends RuntimeException> exceptionClass, String messageKey, Object... args) {
        String errorMessage = getMessage(messageKey, args);
        try {
            return exceptionClass.getDeclaredConstructor(String.class).newInstance(errorMessage);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Unable to create exception object: {}", e.getMessage());
        }
        return new RuntimeException(errorMessage);
    }

    private static String getMessage(String messageKey, Object... args) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BASE_NAME, Locale.ENGLISH, Thread.currentThread().getContextClassLoader());
        return getMessage(resourceBundle, messageKey, args).orElse(messageKey);
    }

    private static Optional<String> getMessage(ResourceBundle resourceBundle, String messageKey, Object... args) {
        if (resourceBundle.containsKey(messageKey)) {
            String message = resourceBundle.getString(messageKey);
            return Optional.of(args.length > 0 ? MessageFormat.format(message, args) : message);
        }

        log.warn("Message not found for key: {}", messageKey);
        return Optional.empty();
    }
}
