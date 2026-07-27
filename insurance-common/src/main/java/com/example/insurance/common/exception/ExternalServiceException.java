
package com.example.insurance.common.exception;

public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(String message) {
        super(503, message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(503, message, cause);
    }
}