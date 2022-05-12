package com.challenge.security.filter;

import com.challenge.dto.error.ApiErrorDto;
import com.challenge.service.JWTService;
import com.challenge.service.UserAuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Filter class that intercepts every request for authorization purpose
 */
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    /**
     * Endpoints that should not be filtered
     */
    public static final Set<String> pathWhiteList = Set.of("/api/login", "/api/user");

    private final JWTService jwtService;
    private final ObjectMapper objectMapper;
    private final UserAuthenticationService userAuthenticationService;

    public AuthorizationFilter(ApplicationContext applicationContext) {
        this.jwtService = applicationContext.getBean(JWTService.class);
        this.objectMapper = applicationContext.getBean(ObjectMapper.class);
        this.userAuthenticationService = applicationContext.getBean(UserAuthenticationService.class);
    }

    /**
     * Checks if the headers contain the JWT token. If the token is present,
     * it is also validated. If the validation passes, the user is authorized.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException, ServletException {

        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            // get username->role pair from JWT
            Pair<String, SimpleGrantedAuthority> usernameAuthoritiesPair = jwtService.getUserInfoFromToken(bearerToken);

            log.info("Authorizing user: {}", usernameAuthoritiesPair.getFirst());

            // check if token is active, else authorization will not go through
            boolean isTokenActive = userAuthenticationService.isTokenActive(jwtService.getPlainToken(bearerToken));
            if (isTokenActive) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(usernameAuthoritiesPair.getFirst(),
                                null, Set.of(usernameAuthoritiesPair.getSecond()));

                // authenticate user
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.info("User: {} with roles {} was successfully authorized",
                        authenticationToken.getPrincipal(), authenticationToken.getAuthorities());

                filterChain.doFilter(request, response);
            } else {
                log.warn("Authorization failed: Token not active");
                setErrorResponse(response, request.getRequestURI(), "Token is not active");
            }

        } catch (RuntimeException ex) {
            log.warn("Authorization failed: {}", ex.getMessage());
            setErrorResponse(response, request.getRequestURI(), ex.getMessage());
        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (request.getMethod().equals(HttpMethod.POST.name())) {
            return pathWhiteList.contains(request.getServletPath());
        }
        return false;
    }

    private void setErrorResponse(HttpServletResponse response, String requestUri, String errorMessage) throws IOException {
        ApiErrorDto errorDto = new ApiErrorDto(HttpStatus.FORBIDDEN, errorMessage, requestUri);
        response.setStatus(errorDto.getCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorDto));
    }
}
