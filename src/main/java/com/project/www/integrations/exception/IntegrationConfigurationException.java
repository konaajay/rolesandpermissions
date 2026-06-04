package com.project.www.integrations.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IntegrationConfigurationException extends RuntimeException {
    private final HttpStatus status;

    public IntegrationConfigurationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public IntegrationConfigurationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
