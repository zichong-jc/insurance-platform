
package com.example.insurance.common.exception;

import com.example.insurance.common.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        log.warn("Business exception: {} - {}", e.getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getCode())
                .body(Result.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleResourceNotFoundException(
            ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.notFound(e.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Result<Void>> handleValidationException(
            ValidationException e, HttpServletRequest request) {
        log.warn("Validation error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.badRequest(e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Result<Void>> handleConflictException(
            ConflictException e, HttpServletRequest request) {
        log.warn("Conflict error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.conflict(e.getMessage()));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Result<Void>> handleExternalServiceException(
            ExternalServiceException e, HttpServletRequest request) {
        log.error("External service error: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Result.error(503, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.badRequest(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(
            Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.serverError("Internal server error"));
    }
}