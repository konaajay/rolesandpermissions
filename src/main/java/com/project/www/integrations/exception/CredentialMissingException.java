package com.project.www.integrations.exception;

import org.springframework.http.HttpStatus;

public class CredentialMissingException extends IntegrationException {

    public CredentialMissingException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
