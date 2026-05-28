package com.project.www.exception;

public class UserAlreadyExistsException
        extends RuntimeException {

    public UserAlreadyExistsException(
            String message
    ) {
        super(message);
    }
}