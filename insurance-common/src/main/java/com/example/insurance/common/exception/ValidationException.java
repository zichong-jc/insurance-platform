
package com.example.insurance.common.exception;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(400, message);
    }

    public ValidationException(String field, String message) {
        super(400, String.format("%s: %s", field, message));
    }
}