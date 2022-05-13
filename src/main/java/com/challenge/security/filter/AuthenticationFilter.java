package com.challenge.security.filter;

import com.challenge.dto.auth.AuthResponseDto;
import com.challenge.dto.error.ApiErrorDto;
import com.challenge.dto.user.UserEntryDto;
import com.challenge.entity.User;
import com.challenge.exception.ExceptionFactory;
import com.challenge.service.JWTService;
import com.challenge.service.UserAuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Filter class for authentication requests and JWT generator
 */
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * Error message keys
     */
    private static final String UNABLE_TO_READ_REQUEST = "exception.authentication.unableToReadRequest";
    private static final String ACTIVE_SESSIONS_PRESENT = "authentication.activeSessionsPresent";

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final JWTService jwtService;
    private final UserAuthenticationService userAuthenticationService;
    private final MessageSource messageSource;

    public AuthenticationFilter(AuthenticationManager authenticationManager, ApplicationContext applicationContext) {
        this.authenticationManager = authenticationManager;
        this.objectMapper = applicationContext.getBean(ObjectMapper.class);
        this.jwtService = applicationContext.getBean(JWTService.class);
        this.userAuthenticationService = applicationContext.getBean(UserAuthenticationService.class);
        this.messageSource = applicationContext.getBean(MessageSource.class);
    }

    /**
     * Tries to authenticate the user. This method is called on /login endpoint calls.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        UserEntryDto userEntryDto;
        try {
            userEntryDto = objectMapper.readValue(request.getInputStream(), UserEntryDto.class);
        } catch (IOException ex) {
            throw ExceptionFactory.create(AuthenticationServiceException.class, UNABLE_TO_READ_REQUEST, ex.getMessage());
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userEntryDto.getUsername(), userEntryDto.getPassword());
        return authenticationManager.authenticate(authenticationToken);
    }

    /**
     * Returns the generated JWT on successful authentication
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) throws IOException {

        // get user and generate the access token
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(user.getUsername(), user.getRole().getAuthority(), request.getRequestURL().toString());
        log.info("Authentication for user: {} was successful", user.getUsername());

        // check if user already has active tokens
        boolean hasUserActiveSessions = userAuthenticationService.isUserAlreadyAuthenticated(user.getUsername());
        String responseMessage = hasUserActiveSessions ? messageSource.getMessage(ACTIVE_SESSIONS_PRESENT, null, Locale.ENGLISH) : null;
        AuthResponseDto authResponseDto = new AuthResponseDto(responseMessage, accessToken);

        // create a new authentication entry with the new token and set response
        userAuthenticationService.saveNewAuthentication(user.getUsername(), accessToken, jwtService.getTokenExpiration(accessToken));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(authResponseDto));
    }

    /**
     * Returns a {@link ApiErrorDto} on unsuccessful authentication
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException exception) throws IOException {

        ApiErrorDto errorDto = new ApiErrorDto(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorDto.getCode());
        response.getWriter().write(objectMapper.writeValueAsString(errorDto));

        log.warn("Authentication failed: {}", exception.getMessage());
    }
}
