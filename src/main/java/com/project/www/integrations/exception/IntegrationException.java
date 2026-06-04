package com.project.www.integrations.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IntegrationException extends RuntimeException {

    private final HttpStatus status;

    public IntegrationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public IntegrationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
