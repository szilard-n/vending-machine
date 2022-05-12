package com.challenge.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring actuator config class
 */
@Configuration
public class ActuatorConfig {

    /**
     * Log each request and response
     */
    @Bean
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository() {
            final Logger logger = LoggerFactory.getLogger(InMemoryHttpTraceRepository.class);

            @Override
            public void add(HttpTrace trace) {
                logger.info(buildLog(trace));
                super.add(trace);
            }

        };
    }

    private String buildLog(HttpTrace trace) {
        return trace.getRequest().getMethod() + " " +
                trace.getRequest().getUri() + " -> " +
                trace.getResponse().getStatus();
    }
}
