package com.challenge.security;

import com.challenge.dto.error.ApiErrorDto;
import com.challenge.entity.RoleType;
import com.challenge.security.filter.AuthenticationFilter;
import com.challenge.security.filter.AuthorizationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for authentication and authorisation
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/user").permitAll()
                .antMatchers(HttpMethod.GET, "/api/product").authenticated()
                .antMatchers("/api/product").hasAuthority(RoleType.ROLE_SELLER.name())
                .antMatchers("/api/transaction/**").hasAuthority(RoleType.ROLE_BUYER.name())
                .anyRequest().authenticated();

        http.addFilterBefore(authorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilter(authenticationFilter())
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    public AuthenticationFilter authenticationFilter() throws Exception {
        AuthenticationFilter authorizationFilter =
                new AuthenticationFilter(authenticationManagerBean(), getApplicationContext());
        authorizationFilter.setFilterProcessesUrl("/api/login");
        return authorizationFilter;
    }

    public AuthorizationFilter authorizationFilter() {
        return new AuthorizationFilter(getApplicationContext());
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ApiErrorDto errorDto = new ApiErrorDto(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorDto));
        };
    }
}
