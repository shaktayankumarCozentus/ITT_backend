package com.itt.service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itt.service.enums.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API response wrapper for all REST endpoints.
 * <p>
 * This class provides a consistent response structure across the entire application,
 * ensuring uniform handling of both successful and error responses. It includes:
 * <ul>
 *   <li><strong>Success responses:</strong> data payload with optional messages</li>
 *   <li><strong>Error responses:</strong> error codes, messages, and validation details</li>
 *   <li><strong>Metadata:</strong> request correlation, timing, and debugging information</li>
 *   <li><strong>Validation errors:</strong> field-level validation failure details</li>
 * </ul>
 * <p>
 * Response structure:
 * <pre>
 * {
 *   "success": true|false,
 *   "message": "Optional message",
 *   "data": { ... },
 *   "errorCode": "ERROR_CODE",
 *   "validationErrors": [ ... ],
 *   "path": "/api/v1/endpoint",
 *   "timestamp": "2025-01-17T10:30:00",
 *   "traceId": "abc123",
 *   "metadata": { ... }
 * }
 * </pre>
 * 
 * @param <T> the type of data payload in successful responses
 * 
 * @see com.itt.service.util.ResponseBuilder
 * @see com.itt.service.exception.GlobalExceptionHandler
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /** Indicates whether the request was processed successfully */
    private boolean success;
    
    /** Optional human-readable message describing the result */
    private String message;
    
    /** The response payload for successful operations */
    private T data;
    
    /** Error code for failed operations (from ErrorCode enum) */
    private String errorCode;
    
    /** List of field-level validation errors */
    private List<ValidationError> validationErrors;
    
    /** The request path that generated this response */
    private String path;
    
    /** Timestamp when the response was generated */
    private LocalDateTime timestamp;
    
    /** Unique trace ID for request correlation */
    private String traceId;
    
    /** Additional metadata for debugging and monitoring */
    private Map<String, Object> metadata;

    // ==========================================
    // SUCCESS RESPONSE BUILDERS
    // ==========================================

    /**
     * Creates a successful response with data payload.
     * 
     * @param <T>  the type of data
     * @param data the response data
     * @return ApiResponse with success=true and timestamp
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with message and data payload.
     * 
     * @param <T>     the type of data
     * @param message descriptive success message
     * @param data    the response data
     * @return ApiResponse with success=true, message, and timestamp
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with only a message (no data payload).
     * 
     * @param message descriptive success message
     * @return ApiResponse with success=true and message
     */
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==========================================
    // ERROR RESPONSE BUILDERS
    // ==========================================

    /**
     * Creates an error response with error code and message.
     * 
     * @param <T>       the type of data (typically null for errors)
     * @param errorCode the error code enum
     * @param message   error description
     * @return ApiResponse with success=false and error details
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with path information.
     * 
     * @param <T>       the type of data (typically null for errors)
     * @param errorCode the error code enum
     * @param message   error description
     * @param path      the request path that caused the error
     * @return ApiResponse with success=false and error details
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with validation errors.
     * 
     * @param <T>              the type of data (typically null for errors)
     * @param errorCode        the error code enum
     * @param message          error description
     * @param validationErrors list of field-level validation failures
     * @return ApiResponse with success=false and validation details
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, 
                                         List<ValidationError> validationErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(message)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with path and trace ID.
     * 
     * @param <T>       the type of data (typically null for errors)
     * @param errorCode the error code enum
     * @param message   error description
     * @param path      the request path that caused the error
     * @param traceId   unique trace ID for correlation
     * @return ApiResponse with success=false and full error context
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, 
                                         String path, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(message)
                .path(path)
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==========================================
    // VALIDATION ERROR CLASS
    // ==========================================

    /**
     * Represents a field-level validation error.
     * <p>
     * Contains details about validation failures including:
     * <ul>
     *   <li>Field name that failed validation</li>
     *   <li>Value that was rejected</li>
     *   <li>Human-readable error message</li>
     *   <li>Validation constraint code</li>
     * </ul>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        
        /** Name of the field that failed validation */
        private String field;
        
        /** The value that was rejected during validation */
        private Object rejectedValue;
        
        /** Human-readable error message */
        private String message;
        
        /** Validation constraint code (e.g., "NotNull", "Size") */
        private String code;
    }
}