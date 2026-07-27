
package com.example.insurance.common.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(404, String.format("%s not found with id: %d", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(404, String.format("%s not found: %s", resourceName, identifier));
    }
}