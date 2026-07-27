
package com.example.insurance.common.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),
    CREATED(201, "created"),
    
    BAD_REQUEST(400, "Bad request"),
    VALIDATION_ERROR(400, "Validation error"),
    
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    
    NOT_FOUND(404, "Resource not found"),
    COMPANY_NOT_FOUND(404, "Insurance company not found"),
    PRODUCT_NOT_FOUND(404, "Insurance product not found"),
    VERSION_NOT_FOUND(404, "Version not found"),
    DOCUMENT_NOT_FOUND(404, "Document not found"),
    PARSE_RESULT_NOT_FOUND(404, "Parse result not found"),
    
    CONFLICT(409, "Conflict"),
    DUPLICATE_COMPANY(409, "Company code already exists"),
    DUPLICATE_PRODUCT(409, "Product name already exists for this company"),
    
    INTERNAL_ERROR(500, "Internal server error"),
    SYNC_ERROR(500, "Sync failed"),
    PARSE_ERROR(500, "Parse failed"),
    
    SERVICE_UNAVAILABLE(503, "Service unavailable"),
    EXTERNAL_SERVICE_ERROR(503, "External service error");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}