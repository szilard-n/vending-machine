package com.challenge.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * DTO class for error response from the API
 */
@Data
public class ApiErrorDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Timestamp timestamp;
    private int code;
    private String status;
    private Object reason;
    private String path;

    public ApiErrorDto(HttpStatus status, Object reason, String path) {
        this.timestamp = Timestamp.from(Instant.now());
        this.code = status.value();
        this.status = status.name();
        this.reason = reason;
        this.path = path;
    }
}
