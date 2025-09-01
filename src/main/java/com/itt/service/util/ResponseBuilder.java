package com.itt.service.util;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.itt.service.constants.SuccessMessages;
import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.ApiResponse.ValidationError;
import com.itt.service.enums.ErrorCode;

/**
 * Single utility for building all API responses.
 * Use these static methods in controllers for consistent responses.
 */
public final class ResponseBuilder {

    // ==================== SUCCESS RESPONSES ====================

    /** Standard success response with message only */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    /** Success response with data */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    /** Success response with data and default message */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(SuccessMessages.OPERATION_SUCCESSFUL, data);
    }

    // ==================== CREATED RESPONSES ====================

    /** HTTP 201 for newly created resources */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** HTTP 201 with default message */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return created(SuccessMessages.DATA_SAVED, data);
    }

    // ==================== ERROR RESPONSES ====================

    /** Basic error response */
    public static ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode, String message) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode.getCode())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /** Error with debugging metadata */
    public static ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode, String message, String path, String traceId) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode.getCode())
                .path(path)
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /** Error with full metadata */
    public static ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode, String message, String path, String traceId, Map<String, Object> metadata) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode.getCode())
                .path(path)
                .traceId(traceId)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    // ==================== VALIDATION ERRORS ====================

    /** Validation error with field details */
    public static ResponseEntity<ApiResponse<Void>> validationError(String message, List<ValidationError> validationErrors) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .validationErrors(validationErrors)
                .errorCode(ErrorCode.VALIDATION_FAILED.getCode())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    /** Single field validation error */
    public static ResponseEntity<ApiResponse<Void>> validationError(String message, ValidationError validationError) {
        return validationError(message, Collections.singletonList(validationError));
    }

    // ==================== UTILITY METHODS ====================

    /** HTTP 204 for successful operations without data */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /** 
     * Smart response: HTTP 204 if empty data, HTTP 200 if data exists.
     * Perfect for search/list operations that might return empty results.
     */
    public static <T> ResponseEntity<ApiResponse<T>> dynamicResponse(T data) {
        if (isEmptyData(data)) {
            return ResponseEntity.noContent().build();
        }
        return success(data);
    }

    /** Custom HTTP status response */
    public static <T> ResponseEntity<ApiResponse<T>> customStatus(HttpStatus status, String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(status.is2xxSuccessful())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    /** Custom HTTP status response without data */
    public static ResponseEntity<ApiResponse<Void>> customStatus(HttpStatus status, String message) {
        return customStatus(status, message, null);
    }

    // ==================== PRIVATE HELPERS ====================

    /** Checks if data is empty for dynamic response logic */
    @SuppressWarnings("rawtypes")
    private static boolean isEmptyData(Object data) {
        if (data == null) {
            return true;
        }
        if (data instanceof Collection) {
            return ((Collection) data).isEmpty();
        }
        if (data instanceof Map) {
            return ((Map) data).isEmpty();
        }
        if (data instanceof Object[]) {
            return ((Object[]) data).length == 0;
        }
        // For pagination responses, check if content is empty
        if (data.getClass().getSimpleName().contains("PaginationResponse")) {
            try {
                var method = data.getClass().getMethod("getContent");
                var content = method.invoke(data);
                return content == null || (content instanceof Collection && ((Collection) content).isEmpty());
            } catch (Exception e) {
                // If we can't check, assume it has data
                return false;
            }
        }
        return false;
    }
}