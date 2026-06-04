package com.project.www.integrations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.project.www.integrations.dto.ApiResponseDto;
import com.project.www.integrations.exception.IntegrationConfigurationException;

import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.universalsaas.platform.integrations")
public class GlobalIntegrationExceptionHandler {

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIntegration(IntegrationException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponseDto.failure(ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(IntegrationConfigurationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleConfigurationException(IntegrationConfigurationException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponseDto.failure(ex.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponseDto.failure("Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.failure("An unexpected error occurred", ex.getMessage()));
    }
}
