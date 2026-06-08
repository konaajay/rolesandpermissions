package com.project.www.vendor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateVendorException extends RuntimeException {
    public DuplicateVendorException(String message) {
        super(message);
    }
}
