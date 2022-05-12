package com.challenge.service;

import com.challenge.entity.AuthEntry;
import com.challenge.exception.ExceptionFactory;
import com.challenge.exception.exceptions.ResourceNotFoundException;
import com.challenge.repository.AuthEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Service class for getting user auth related info.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserAuthenticationService {

    /**
     * Error message keys
     */
    private static final String TOKEN_NOT_FOUND = "exception.resourceNotFound.accessTokenNotFound";

    private final AuthEntryRepository authEntryRepository;

    /**
     * Checks if a username already has an active token
     */
    public boolean isUserAlreadyAuthenticated(String username) {
        return authEntryRepository.findAllActiveAndNotExpired(username).size() != 0;
    }

    /**
     * Saves a new log entry to the database
     */
    @Transactional
    public void saveNewAuthentication(String username, String token, Date expirationDate) {
        AuthEntry authEntry = AuthEntry.builder()
                .username(username)
                .token(token)
                .expirationDate(new Timestamp(expirationDate.getTime()))
                .active(true)
                .build();
        authEntryRepository.save(authEntry);
    }

    /**
     * Checks if the token is still active.
     */
    public boolean isTokenActive(String token) {
        AuthEntry authEntry = authEntryRepository.findByToken(token)
                .orElseThrow(() -> ExceptionFactory.create(ResourceNotFoundException.class, TOKEN_NOT_FOUND));
        return authEntry.isActive();
    }

    @Transactional
    public void deactivateAuthentication(String username) {
        List<AuthEntry> authEntries = authEntryRepository.findAllActiveAndNotExpired(username);
        for (AuthEntry authEntry : authEntries) {
            authEntry.setActive(false);
        }
        log.info("All tokens have been deactivated for user: {}", username);
    }

    /**
     * Scheduled job to clean up db from all expired authentication logs
     */
    @Scheduled(cron = "${cron.authEntry.cleanUp}")
    @Transactional
    public void removeAllExpiredEntries() {
        authEntryRepository.removeAllExpired();
        log.info("Cleanup job for expired tokens ran successfully");
    }
}
