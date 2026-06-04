package com.project.www.integrations.exception;

import org.springframework.http.HttpStatus;

public class IntegrationNotFoundException extends IntegrationException {

    public IntegrationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
