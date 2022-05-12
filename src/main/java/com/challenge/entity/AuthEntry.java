package com.challenge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Entity class representing a user's authentication
 */
@Entity
@Table(name = "T_AUTHENTICATION")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthEntry {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(
            name = "ID",
            updatable = false
    )
    private UUID id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "TOKEN")
    private String token;

    @Column(name = "EXPIRATION_DATE")
    private Timestamp expirationDate;

    @Column(name = "ACTIVE")
    private boolean active;
}
