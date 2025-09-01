package com.itt.service.exception;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.UUID;

import javax.naming.AuthenticationException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.annotation.Order;

import com.itt.service.constants.ErrorMessages;
import com.itt.service.dto.ApiResponse;
import com.itt.service.enums.ErrorCode;
import com.itt.service.util.MessageResolver;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for standardized API error responses.
 * 
 * Handles all application exceptions and converts them to user-friendly ApiResponse format.
 * Automatically logs errors with trace IDs for debugging.
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.itt.service.**")
@RequiredArgsConstructor
@Hidden
@Order(1)
public class GlobalExceptionHandler {

    private final MessageResolver messageResolver;

    // ==================== BUSINESS EXCEPTIONS ====================

    /**
     * Handles custom business exceptions with user-friendly messages.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Business error [{}]: {} - Code: {}", traceId, ex.getMessage(), ex.getErrorCode().getCode());

        String resolvedMessage = ex.getMessageArgs() != null
                ? messageResolver.resolveMessage(ex.getUserMessage(), ex.getMessageArgs())
                : ex.getUserMessage();

        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), resolvedMessage, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    /**
     * Handles validation exceptions with field-level error details.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Validation error [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getUserMessage(), ex.getValidationErrors());
        response.setPath(request.getRequestURI());
        response.setTraceId(traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    // ==================== VALIDATION EXCEPTIONS ====================

    // NOTE: MethodArgumentNotValidException is handled by CustomValidationExceptionResolver 
    // with HIGHEST_PRECEDENCE to ensure consistent validation error format

    /**
     * Handles Bean Validation constraint violations - returns simple message.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Validation failed");

        return ResponseEntity.badRequest().body(
            ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build()
        );
    }

    // ==================== SECURITY EXCEPTIONS ====================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Authentication failed [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.UNAUTHORIZED, ErrorMessages.UNAUTHORIZED, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Bad credentials [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_CREDENTIALS, ErrorMessages.INVALID_CREDENTIALS, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getHttpStatus()).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Access denied [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.ACCESS_DENIED, ErrorMessages.ACCESS_DENIED, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getHttpStatus()).body(response);
    }

    // ==================== DATABASE EXCEPTIONS ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Data integrity violation [{}]: {}", traceId, ex.getMessage());

        // Smart error detection for common cases
        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;
        String message = ErrorMessages.DATA_INTEGRITY_VIOLATION;

        if (ex.getMessage() != null && (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("Duplicate entry"))) {
            errorCode = ErrorCode.DUPLICATE_ENTRY;
            message = ErrorMessages.DUPLICATE_ENTRY;
        }

        ApiResponse<Void> response = ApiResponse.error(errorCode, message, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Entity not found [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND, ErrorMessages.ENTITY_NOT_FOUND, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.ENTITY_NOT_FOUND.getHttpStatus()).body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Database error [{}]: {}", traceId, ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.DATABASE_CONNECTION_ERROR, ErrorMessages.DATABASE_CONNECTION_ERROR, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.DATABASE_CONNECTION_ERROR.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Type mismatch [{}]: {} - Expected: {}, Actual: {}", traceId, ex.getName(), ex.getRequiredType(), ex.getValue());

        String message = messageResolver.resolveMessage(ErrorMessages.INVALID_DATA_FORMAT, ex.getName());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_DATA_FORMAT, message, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.badRequest().body(response);
    }







    // ==================== FALLBACK HANDLER ====================

    /**
     * Global fallback for all uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Unexpected error [{}]: {}", traceId, ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Generates unique 8-character trace ID for request correlation.
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Builds request metadata for error context.
     */
    private Map<String, Object> buildRequestMetadata(HttpServletRequest request) {
        return Map.of(
            "method", request.getMethod(),
            "userId", request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
            "userAgent", request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "unknown",
            "remoteAddr", getClientIpAddress(request)
        );
    }

    /**
     * Extracts real client IP considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}