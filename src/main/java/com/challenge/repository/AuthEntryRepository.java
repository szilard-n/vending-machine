package com.challenge.repository;

import com.challenge.entity.AuthEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link AuthEntry}
 */
@Repository
public interface AuthEntryRepository extends JpaRepository<AuthEntry, UUID> {

    @Query("SELECT entry FROM AuthEntry entry " +
            "WHERE entry.username = :username " +
            "AND entry.expirationDate > NOW() " +
            "AND entry.active = TRUE")
    List<AuthEntry> findAllActiveAndNotExpired(String username);

    @Modifying
    @Query("DELETE FROM AuthEntry entry WHERE entry.expirationDate < NOW()")
    void removeAllExpired();

    Optional<AuthEntry> findByToken(String token);
}
