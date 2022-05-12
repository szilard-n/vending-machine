package com.challenge.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.challenge.exception.ExceptionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

/**
 * Service class for JWT validation and creation
 */
@Service
public class JWTService {

    private static final String INVALID_TOKEN = "exception.jwtVerification.invalidToken";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Value("${security.jwt.tokenExpirationInHours}")
    private int tokenExpirationInHours;

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.claim}")
    private String claim;

    /**
     * Generates a JWT using the user's username, roles. It uses the HMAC256 algorithm
     * to generate the token.
     */
    public String generateToken(String username, String role, String requestURL) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(getExpirationDate())
                .withIssuer(requestURL)
                .withClaim(claim, role)
                .sign(Algorithm.HMAC256(secret.getBytes()));
    }

    /**
     * Validates a token and returns a pair of username->roles.
     *
     * @return {@link Pair} of username and the user's roles
     */
    public Pair<String, SimpleGrantedAuthority> getUserInfoFromToken(String bearerToken) {
        String token = getPlainToken(bearerToken);
        DecodedJWT decodedJWT = getDecodedJWT(token);

        // get user details from JWT
        String username = decodedJWT.getSubject();
        String role = decodedJWT.getClaim(claim).as(String.class);

        return Pair.of(username, new SimpleGrantedAuthority(role));
    }

    /**
     * Gets a pair of username->expirationDate from a JWT
     */
    public Date getTokenExpiration(String token) {
        DecodedJWT decodedJWT = getDecodedJWT(token);
        return decodedJWT.getExpiresAt();
    }

    /**
     * Check if token is valid and get plain jwt token
     * without the prefix.
     */
    public String getPlainToken(String bearerToken) {
        if (!isTokenValid(bearerToken)) {
            throw ExceptionFactory.create(JWTVerificationException.class, INVALID_TOKEN, bearerToken);
        }
        return bearerToken.substring(TOKEN_PREFIX.length());
    }

    /**
     * Checks if token is not null and if it's starts with the bearer prefix.
     */
    private boolean isTokenValid(String bearerToken) {
        return bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX);
    }

    /**
     * Verify and decode JWT
     */
    private DecodedJWT getDecodedJWT(String token) {
        // build algorithm and verify JWT
        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        return jwtVerifier.verify(token);
    }

    /**
     * Generates the expiration date for the new JWT
     *
     * @return current date + expiration in hours
     */
    private Date getExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, tokenExpirationInHours);
        return calendar.getTime();
    }
}
