package com.itt.service.enums;


import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // General Errors
    INTERNAL_SERVER_ERROR("ERR_001", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("ERR_002", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("ERR_003", HttpStatus.NOT_FOUND),
    ACCESS_DENIED("ERR_004", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("ERR_005", HttpStatus.UNAUTHORIZED),
    
    // Validation Errors
    VALIDATION_FAILED("VAL_001", HttpStatus.BAD_REQUEST),
    CONSTRAINT_VIOLATION("VAL_002", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD("VAL_003", HttpStatus.BAD_REQUEST),
    INVALID_DATA_FORMAT("VAL_004", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT("VAL_005", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT("VAL_006", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_WEAK("VAL_007", HttpStatus.BAD_REQUEST),
    
    // Authentication Errors
    INVALID_CREDENTIALS("AUTH_001", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_002", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH_003", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("AUTH_004", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("AUTH_005", HttpStatus.UNAUTHORIZED),
    
    // Business Logic Errors
    USER_ALREADY_EXISTS("BUS_001", HttpStatus.CONFLICT),
    USER_NOT_FOUND("BUS_002", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_REGISTERED("BUS_003", HttpStatus.CONFLICT),
    INSUFFICIENT_BALANCE("BUS_004", HttpStatus.BAD_REQUEST),
    OPERATION_NOT_ALLOWED("BUS_005", HttpStatus.FORBIDDEN),
    DUPLICATE_ENTRY("BUS_006", HttpStatus.CONFLICT),
    
    // Database Errors
    DATABASE_CONNECTION_ERROR("DB_001", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_INTEGRITY_VIOLATION("DB_002", HttpStatus.CONFLICT),
    ENTITY_NOT_FOUND("DB_003", HttpStatus.NOT_FOUND),
    OPTIMISTIC_LOCK_ERROR("DB_004", HttpStatus.CONFLICT),
    
    // File Operation Errors
    FILE_NOT_FOUND("FILE_001", HttpStatus.NOT_FOUND),
    FILE_SIZE_EXCEEDED("FILE_002", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_FILE_FORMAT("FILE_003", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("FILE_004", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // HTTP Errors
    METHOD_NOT_ALLOWED("HTTP_001", HttpStatus.METHOD_NOT_ALLOWED),
    MEDIA_TYPE_NOT_SUPPORTED("HTTP_002", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    ENDPOINT_NOT_FOUND("HTTP_003", HttpStatus.NOT_FOUND),
    REQUEST_TIMEOUT("HTTP_004", HttpStatus.REQUEST_TIMEOUT),
    
    // External Service Errors
    EXTERNAL_SERVICE_UNAVAILABLE("EXT_001", HttpStatus.SERVICE_UNAVAILABLE),
    EXTERNAL_SERVICE_TIMEOUT("EXT_002", HttpStatus.GATEWAY_TIMEOUT),
    THIRD_PARTY_API_ERROR("EXT_003", HttpStatus.BAD_GATEWAY),
    
    // New Error Codes
    VALIDATION_ERROR("VAL_008", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("INT_001", HttpStatus.INTERNAL_SERVER_ERROR),
    RESPONSE_VALIDATION_ERROR("RES_001", HttpStatus.BAD_REQUEST);
    
    private final String code;
    private final HttpStatus httpStatus;
    
    ErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
