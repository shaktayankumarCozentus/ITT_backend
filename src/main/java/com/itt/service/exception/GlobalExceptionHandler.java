package com.itt.service.exception;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.itt.service.constants.ErrorMessages;
import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.ApiResponse.ValidationError;
import com.itt.service.enums.ErrorCode;
import com.itt.service.util.MessageResolver;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the Service application.
 * 
 * <p>This class provides centralized exception handling across all REST controllers
 * using Spring's {@code @RestControllerAdvice}. It captures various types of exceptions
 * and converts them into standardized {@link ApiResponse} objects with appropriate
 * HTTP status codes and error metadata.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Centralized Error Handling:</strong> Single point for managing all application exceptions</li>
 *   <li><strong>Standardized Response Format:</strong> Consistent error response structure across all endpoints</li>
 *   <li><strong>Trace ID Generation:</strong> Unique identifiers for request tracking and debugging</li>
 *   <li><strong>Internationalization Support:</strong> Message resolution for multiple languages</li>
 *   <li><strong>Security-Aware:</strong> Handles authentication and authorization exceptions</li>
 *   <li><strong>Validation Error Details:</strong> Comprehensive field-level validation error reporting</li>
 * </ul>
 * 
 * <h3>Exception Hierarchy Handling:</h3>
 * <ul>
 *   <li><strong>Business Exceptions:</strong> {@link CustomException}, {@link ValidationException}, {@link BusinessException}</li>
 *   <li><strong>Security Exceptions:</strong> {@link AuthenticationException}, {@link BadCredentialsException}, {@link AccessDeniedException}</li>
 *   <li><strong>Data Layer Exceptions:</strong> {@link DataIntegrityViolationException}, {@link EntityNotFoundException}, {@link DataAccessException}</li>
 *   <li><strong>Web Layer Exceptions:</strong> {@link MethodArgumentNotValidException}, {@link ConstraintViolationException}</li>
 *   <li><strong>HTTP Exceptions:</strong> {@link HttpRequestMethodNotSupportedException}, {@link HttpMediaTypeNotSupportedException}</li>
 *   <li><strong>Generic Exceptions:</strong> {@link RuntimeException}, {@link Exception}</li>
 * </ul>
 * 
 * <h3>Response Metadata:</h3>
 * Each error response includes:
 * <ul>
 *   <li>Unique trace ID for request correlation</li>
 *   <li>Request path and HTTP method</li>
 *   <li>User context (if authenticated)</li>
 *   <li>Timestamp for audit purposes</li>
 *   <li>Detailed validation errors (where applicable)</li>
 * </ul>
 * 
 * <h3>Logging Strategy:</h3>
 * <ul>
 *   <li><strong>ERROR Level:</strong> System errors, database issues, unexpected exceptions</li>
 *   <li><strong>WARN Level:</strong> Business rule violations, validation failures, authentication issues</li>
 *   <li><strong>DEBUG Level:</strong> Request details for troubleshooting (if enabled)</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Custom business exception
 * throw new CustomException(ErrorCode.ROLE_NOT_FOUND, "Role with ID 123 not found");
 * 
 * // Validation exception with field errors
 * throw new ValidationException(ErrorCode.VALIDATION_FAILED, 
 *     "Invalid input data", fieldErrors);
 * 
 * // Response will be automatically formatted and returned to client
 * }</pre>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see ApiResponse
 * @see ErrorCode
 * @see CustomException
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.itt.service.**")
@RequiredArgsConstructor
@Hidden
public class GlobalExceptionHandler {

    /**
     * Message resolver for internationalization support.
     * Resolves error messages based on locale and message parameters.
     */
    private final MessageResolver messageResolver;

    // ==================== BUSINESS EXCEPTION HANDLERS ====================

    /**
     * Handles custom business exceptions thrown by the application.
     * 
     * <p>This handler processes {@link CustomException} instances which represent
     * known business rule violations or application-specific errors. These exceptions
     * include predefined error codes and user-friendly messages.</p>
     * 
     * <h4>Handled Scenarios:</h4>
     * <ul>
     *   <li>Resource not found errors (e.g., role, user, company not found)</li>
     *   <li>Business rule violations (e.g., insufficient permissions)</li>
     *   <li>State conflicts (e.g., attempting to delete active resource)</li>
     *   <li>Configuration errors (e.g., invalid feature toggle states)</li>
     * </ul>
     * 
     * <h4>Response Features:</h4>
     * <ul>
     *   <li>Resolves internationalized messages with parameters</li>
     *   <li>Includes request metadata for audit trails</li>
     *   <li>Generates unique trace ID for error correlation</li>
     *   <li>Maps error code to appropriate HTTP status</li>
     * </ul>
     * 
     * @param ex the custom exception containing error details
     * @param request the HTTP request that triggered the exception
     * @return standardized error response with business error details
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Custom exception [{}]: {} - ErrorCode: {}", traceId, ex.getMessage(), ex.getErrorCode().getCode());

        String resolvedMessage = ex.getMessageArgs() != null
                ? messageResolver.resolveMessage(ex.getUserMessage(), ex.getMessageArgs())
                : ex.getUserMessage();

        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), resolvedMessage, request.getRequestURI(),
                traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    /**
     * Handles validation exceptions with detailed field-level errors.
     * 
     * <p>This handler processes {@link ValidationException} instances which contain
     * comprehensive validation error information including field names, rejected values,
     * and constraint violation details.</p>
     * 
     * <h4>Validation Error Details Include:</h4>
     * <ul>
     *   <li>Field name that failed validation</li>
     *   <li>Rejected value that caused the violation</li>
     *   <li>Human-readable error message</li>
     *   <li>Constraint violation code for client-side handling</li>
     * </ul>
     * 
     * <h4>Common Use Cases:</h4>
     * <ul>
     *   <li>Complex business validation rules</li>
     *   <li>Cross-field validation dependencies</li>
     *   <li>Database constraint violations</li>
     *   <li>Custom annotation-based validations</li>
     * </ul>
     * 
     * @param ex the validation exception with field-level errors
     * @param request the HTTP request that triggered the validation
     * @return detailed validation error response with field breakdowns
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Validation exception [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getUserMessage(),
                ex.getValidationErrors());
        response.setPath(request.getRequestURI());
        response.setTraceId(traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    // ==================== SPRING VALIDATION HANDLERS ====================

    /**
     * Handles Spring's method argument validation failures.
     * 
     * <p>This handler processes {@link MethodArgumentNotValidException} which occurs
     * when {@code @Valid} or {@code @Validated} annotations fail on request bodies,
     * form data, or method parameters.</p>
     * 
     * <h4>Validation Sources:</h4>
     * <ul>
     *   <li>{@code @RequestBody} validation failures</li>
     *   <li>{@code @ModelAttribute} form validation errors</li>
     *   <li>Bean Validation API (JSR-303) constraint violations</li>
     *   <li>Custom validator implementations</li>
     * </ul>
     * 
     * <h4>Error Processing:</h4>
     * <ul>
     *   <li>Extracts field errors from Spring's BindingResult</li>
     *   <li>Converts to standardized ValidationError format</li>
     *   <li>Preserves original constraint codes for client processing</li>
     *   <li>Includes rejected values for debugging (sanitized for security)</li>
     * </ul>
     * 
     * @param ex the method argument validation exception
     * @param request the HTTP request with invalid arguments
     * @return structured response with field-level validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Validation failed [{}]: {}", traceId, ex.getMessage());

        List<ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField())
                        .rejectedValue(sanitizeRejectedValue(error.getRejectedValue()))
                        .message(error.getDefaultMessage())
                        .code(error.getCode())
                        .build())
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED,
                validationErrors);
        response.setPath(request.getRequestURI());
        response.setTraceId(traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles Bean Validation constraint violations at the method level.
     * 
     * <p>This handler processes {@link ConstraintViolationException} which occurs
     * when method-level validation fails, typically for path variables, request
     * parameters, or programmatic validation calls.</p>
     * 
     * <h4>Common Triggers:</h4>
     * <ul>
     *   <li>{@code @PathVariable} validation failures</li>
     *   <li>{@code @RequestParam} constraint violations</li>
     *   <li>Programmatic validation calls that fail</li>
     *   <li>Service layer method parameter validation</li>
     * </ul>
     * 
     * @param ex the constraint violation exception
     * @param request the HTTP request that triggered the violation
     * @return structured response with constraint violation details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Constraint violation [{}]: {}", traceId, ex.getMessage());

        List<ValidationError> validationErrors = ex.getConstraintViolations().stream()
                .map(violation -> ValidationError.builder()
                        .field(getFieldName(violation))
                        .rejectedValue(sanitizeRejectedValue(violation.getInvalidValue()))
                        .message(violation.getMessage())
                        .code("CONSTRAINT_VIOLATION")
                        .build())
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.CONSTRAINT_VIOLATION,
                ErrorMessages.CONSTRAINT_VIOLATION, validationErrors);
        response.setPath(request.getRequestURI());
        response.setTraceId(traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.badRequest().body(response);
    }

    // ==================== SECURITY EXCEPTION HANDLERS ====================

    /**
     * Handles authentication failures across the application.
     * 
     * <p>This handler processes {@link AuthenticationException} instances which
     * represent failures in the authentication process, such as invalid credentials,
     * expired tokens, or missing authentication information.</p>
     * 
     * <h4>Authentication Failure Scenarios:</h4>
     * <ul>
     *   <li>Invalid username/password combinations</li>
     *   <li>Expired or malformed JWT tokens</li>
     *   <li>Missing required authentication headers</li>
     *   <li>Disabled or locked user accounts</li>
     * </ul>
     * 
     * @param ex the authentication exception
     * @param request the HTTP request that failed authentication
     * @return unauthorized error response with security-safe messaging
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Authentication failed [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.UNAUTHORIZED, ErrorMessages.UNAUTHORIZED,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatus()).body(response);
    }

    /**
     * Handles Spring Security bad credentials exceptions.
     * 
     * <p>This handler specifically processes {@link BadCredentialsException}
     * from Spring Security, providing a consistent response format for
     * credential-related authentication failures.</p>
     * 
     * @param ex the bad credentials exception
     * @param request the HTTP request with invalid credentials
     * @return invalid credentials error response
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Bad credentials [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_CREDENTIALS, ErrorMessages.INVALID_CREDENTIALS,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getHttpStatus()).body(response);
    }

    /**
     * Handles access denied exceptions for authorization failures.
     * 
     * <p>This handler processes {@link AccessDeniedException} which occurs when
     * an authenticated user attempts to access a resource they don't have
     * permission to access.</p>
     * 
     * <h4>Authorization Failure Scenarios:</h4>
     * <ul>
     *   <li>Insufficient role-based permissions</li>
     *   <li>Resource-level access restrictions</li>
     *   <li>Method-level security violations</li>
     *   <li>Custom authorization rule failures</li>
     * </ul>
     * 
     * @param ex the access denied exception
     * @param request the HTTP request that was denied access
     * @return forbidden error response with authorization details
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Access denied [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.ACCESS_DENIED, ErrorMessages.ACCESS_DENIED,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getHttpStatus()).body(response);
    }

    // ==================== DATA LAYER EXCEPTION HANDLERS ====================

    /**
     * Handles database data integrity violations.
     * 
     * <p>This handler processes {@link DataIntegrityViolationException} which
     * occurs when database constraints are violated, such as unique constraints,
     * foreign key constraints, or check constraints.</p>
     * 
     * <h4>Data Integrity Violations:</h4>
     * <ul>
     *   <li><strong>Unique Constraints:</strong> Duplicate key violations</li>
     *   <li><strong>Foreign Key Constraints:</strong> Referenced entity doesn't exist</li>
     *   <li><strong>Not Null Constraints:</strong> Required fields missing values</li>
     *   <li><strong>Check Constraints:</strong> Business rule violations at DB level</li>
     * </ul>
     * 
     * <h4>Smart Error Detection:</h4>
     * The handler analyzes the exception message to provide more specific
     * error codes and messages for common scenarios like duplicate entries.
     * 
     * @param ex the data integrity violation exception
     * @param request the HTTP request that caused the violation
     * @return conflict error response with appropriate error code
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Data integrity violation [{}]: {}", traceId, ex.getMessage());

        // Determine specific error based on exception message
        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;
        String message = ErrorMessages.DATA_INTEGRITY_VIOLATION;

        if (ex.getMessage() != null
                && (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("Duplicate entry"))) {
            errorCode = ErrorCode.DUPLICATE_ENTRY;
            message = ErrorMessages.DUPLICATE_ENTRY;
        }

        ApiResponse<Void> response = ApiResponse.error(errorCode, message, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * Handles JPA entity not found exceptions.
     * 
     * <p>This handler processes {@link EntityNotFoundException} which occurs
     * when JPA operations cannot find the requested entity by its identifier
     * or query criteria.</p>
     * 
     * @param ex the entity not found exception
     * @param request the HTTP request for the missing entity
     * @return not found error response
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Entity not found [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND, ErrorMessages.ENTITY_NOT_FOUND,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.ENTITY_NOT_FOUND.getHttpStatus()).body(response);
    }

    // ==================== HTTP EXCEPTION HANDLERS ====================

    /**
     * Handles HTTP method not supported exceptions.
     * 
     * <p>This handler processes {@link HttpRequestMethodNotSupportedException}
     * which occurs when a client attempts to use an HTTP method that is not
     * supported for a particular endpoint.</p>
     * 
     * <h4>Common Scenarios:</h4>
     * <ul>
     *   <li>Using POST when only GET is supported</li>
     *   <li>Using PUT when only POST is supported</li>
     *   <li>Attempting DELETE on read-only endpoints</li>
     * </ul>
     * 
     * @param ex the method not supported exception
     * @param request the HTTP request with unsupported method
     * @return method not allowed error response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Method not supported [{}]: {} - Supported methods: {}", 
                traceId, ex.getMessage(), ex.getSupportedMethods());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED, ErrorMessages.METHOD_NOT_ALLOWED,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus()).body(response);
    }

    /**
     * Handles unsupported media type exceptions.
     * 
     * <p>This handler processes {@link HttpMediaTypeNotSupportedException}
     * which occurs when the Content-Type header of the request is not supported
     * by the endpoint.</p>
     * 
     * <h4>Media Type Issues:</h4>
     * <ul>
     *   <li>Sending XML when only JSON is accepted</li>
     *   <li>Missing Content-Type header for POST/PUT requests</li>
     *   <li>Incorrect charset specifications</li>
     *   <li>Unsupported file upload formats</li>
     * </ul>
     * 
     * @param ex the media type not supported exception
     * @param request the HTTP request with unsupported media type
     * @return unsupported media type error response
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Media type not supported [{}]: {} - Supported types: {}", 
                traceId, ex.getMessage(), ex.getSupportedMediaTypes());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED,
                ErrorMessages.MEDIA_TYPE_NOT_SUPPORTED, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED.getHttpStatus()).body(response);
    }

    /**
     * Handles missing required request parameter exceptions.
     * 
     * <p>This handler processes {@link MissingServletRequestParameterException}
     * which occurs when a required request parameter is missing from the HTTP request.</p>
     * 
     * @param ex the missing parameter exception
     * @param request the HTTP request with missing parameter
     * @return bad request error response with parameter details
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Missing parameter [{}]: {} (type: {})", traceId, ex.getParameterName(), ex.getParameterType());

        String message = messageResolver.resolveMessage(ErrorMessages.MISSING_REQUIRED_FIELD, ex.getParameterName());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.MISSING_REQUIRED_FIELD, message,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles method argument type mismatch exceptions.
     * 
     * <p>This handler processes {@link MethodArgumentTypeMismatchException}
     * which occurs when request parameters cannot be converted to the expected
     * type (e.g., string to integer conversion failures).</p>
     * 
     * <h4>Type Conversion Failures:</h4>
     * <ul>
     *   <li>Invalid number formats in path variables</li>
     *   <li>Malformed date/time parameters</li>
     *   <li>Boolean parameter parsing errors</li>
     *   <li>Enum value mismatch errors</li>
     * </ul>
     * 
     * @param ex the type mismatch exception
     * @param request the HTTP request with invalid parameter type
     * @return bad request error response with format details
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Type mismatch [{}]: {} - Expected: {}, Actual: {}", 
                traceId, ex.getName(), ex.getRequiredType(), ex.getValue());

        String message = messageResolver.resolveMessage(ErrorMessages.INVALID_DATA_FORMAT, ex.getName());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_DATA_FORMAT, message, request.getRequestURI(),
                traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles HTTP message not readable exceptions.
     * 
     * <p>This handler processes {@link HttpMessageNotReadableException}
     * which occurs when the request body cannot be parsed or deserialized,
     * typically due to malformed JSON or XML.</p>
     * 
     * <h4>Message Parsing Issues:</h4>
     * <ul>
     *   <li>Malformed JSON syntax errors</li>
     *   <li>Invalid XML structure</li>
     *   <li>Unexpected EOF in request body</li>
     *   <li>Character encoding problems</li>
     * </ul>
     * 
     * @param ex the message not readable exception
     * @param request the HTTP request with unparseable body
     * @return bad request error response for parsing issues
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Message not readable [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_DATA_FORMAT, ErrorMessages.INVALID_DATA_FORMAT,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles file upload size exceeded exceptions.
     * 
     * <p>This handler processes {@link MaxUploadSizeExceededException}
     * which occurs when uploaded files exceed the configured maximum size limits.</p>
     * 
     * @param ex the file size exceeded exception
     * @param request the HTTP request with oversized file
     * @return payload too large error response with size limits
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("File size exceeded [{}]: Max allowed: {} bytes", traceId, ex.getMaxUploadSize());

        String maxSize = ex.getMaxUploadSize() > 0 ? String.valueOf(ex.getMaxUploadSize() / (1024 * 1024)) : "unknown";
        String message = messageResolver.resolveMessage(ErrorMessages.FILE_SIZE_EXCEEDED, maxSize);

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.FILE_SIZE_EXCEEDED, message, request.getRequestURI(),
                traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.FILE_SIZE_EXCEEDED.getHttpStatus()).body(response);
    }

    /**
     * Handles endpoint not found exceptions.
     * 
     * <p>This handler processes {@link NoHandlerFoundException}
     * which occurs when Spring cannot find a handler method for the requested URL.</p>
     * 
     * @param ex the no handler found exception
     * @param request the HTTP request for non-existent endpoint
     * @return not found error response for unknown endpoints
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("No handler found [{}]: {} {}", traceId, ex.getHttpMethod(), ex.getRequestURL());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.ENDPOINT_NOT_FOUND, ErrorMessages.ENDPOINT_NOT_FOUND,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.ENDPOINT_NOT_FOUND.getHttpStatus()).body(response);
    }

    /**
     * Handles general database access exceptions.
     * 
     * <p>This handler processes {@link DataAccessException} which represents
     * various database connectivity and operation issues that don't fall under
     * more specific exception types.</p>
     * 
     * <h4>Database Access Issues:</h4>
     * <ul>
     *   <li>Connection pool exhaustion</li>
     *   <li>Database server connectivity problems</li>
     *   <li>Transaction timeout issues</li>
     *   <li>SQL execution errors</li>
     * </ul>
     * 
     * @param ex the data access exception
     * @param request the HTTP request that triggered the database error
     * @return internal server error response for database issues
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Database error [{}]: {}", traceId, ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.DATABASE_CONNECTION_ERROR,
                ErrorMessages.DATABASE_CONNECTION_ERROR, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.DATABASE_CONNECTION_ERROR.getHttpStatus()).body(response);
    }

    // ==================== GENERIC EXCEPTION HANDLERS ====================

    /**
     * Handles illegal argument exceptions.
     * 
     * <p>This handler processes {@link IllegalArgumentException} which typically
     * indicates programming errors or invalid method parameters that violate
     * preconditions.</p>
     * 
     * <h4>Common Scenarios:</h4>
     * <ul>
     *   <li>Invalid enum values passed to methods</li>
     *   <li>Null arguments where non-null is required</li>
     *   <li>Out-of-range numeric parameters</li>
     *   <li>Invalid configuration values</li>
     * </ul>
     * 
     * @param ex the illegal argument exception
     * @param request the HTTP request that triggered the exception
     * @return bad request error response for invalid arguments
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex,
            HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Illegal argument [{}]: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_REQUEST, ErrorMessages.INVALID_REQUEST,
                request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles null pointer exceptions.
     * 
     * <p>This handler processes {@link NullPointerException} which typically
     * indicates programming errors where null values are accessed without
     * proper null checks.</p>
     * 
     * <h4>Prevention Strategy:</h4>
     * While this handler provides a safety net, NPEs should be prevented through:
     * <ul>
     *   <li>Proper null checks and validation</li>
     *   <li>Use of Optional for nullable values</li>
     *   <li>Defensive programming practices</li>
     *   <li>Comprehensive unit testing</li>
     * </ul>
     * 
     * @param ex the null pointer exception
     * @param request the HTTP request that triggered the exception
     * @return internal server error response for null pointer issues
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointer(NullPointerException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Null pointer exception [{}]: {}", traceId, ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorMessages.INTERNAL_SERVER_ERROR, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
    }

    /**
     * Handles runtime exceptions not caught by more specific handlers.
     * 
     * <p>This handler processes {@link RuntimeException} instances that don't
     * match any of the more specific exception handlers. This serves as a
     * catch-all for unexpected runtime errors.</p>
     * 
     * <h4>Runtime Error Categories:</h4>
     * <ul>
     *   <li>Unexpected business logic failures</li>
     *   <li>Third-party library exceptions</li>
     *   <li>Configuration-related runtime errors</li>
     *   <li>Resource allocation failures</li>
     * </ul>
     * 
     * @param ex the runtime exception
     * @param request the HTTP request that triggered the exception
     * @return internal server error response for runtime issues
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Runtime exception [{}]: {}", traceId, ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorMessages.INTERNAL_SERVER_ERROR, request.getRequestURI(), traceId);
        response.setMetadata(buildRequestMetadata(request));

        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
    }

    /**
     * Global fallback exception handler for all uncaught exceptions.
     * 
     * <p>This handler serves as the ultimate fallback for any exception that
     * doesn't match any of the more specific handlers above. It ensures that
     * no exception goes unhandled and that clients always receive a properly
     * formatted error response.</p>
     * 
     * <h4>Fallback Handler Responsibilities:</h4>
     * <ul>
     *   <li><strong>Error Logging:</strong> Comprehensive error logging with stack traces</li>
     *   <li><strong>Response Consistency:</strong> Ensures all errors follow the same format</li>
     *   <li><strong>Security:</strong> Prevents sensitive information leakage in error messages</li>
     *   <li><strong>Monitoring:</strong> Enables error tracking and alerting systems</li>
     * </ul>
     * 
     * <h4>Error Response Strategy:</h4>
     * <ul>
     *   <li>Generic error message to prevent information disclosure</li>
     *   <li>Unique trace ID for debugging and support</li>
     *   <li>Complete request metadata for audit trails</li>
     *   <li>Consistent HTTP 500 status code</li>
     * </ul>
     * 
     * @param ex the uncaught exception
     * @param request the HTTP request that triggered the exception
     * @return standardized internal server error response
     */
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
//        String traceId = generateTraceId();
//        log.error("Unexpected error [{}]: {}", traceId, ex.getMessage(), ex);
//
//        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR,
//                ErrorMessages.INTERNAL_SERVER_ERROR, request.getRequestURI(), traceId);
//        response.setMetadata(buildRequestMetadata(request));
//
//        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
//    }

    // ==================== UTILITY METHODS ====================

    /**
     * Generates a unique trace ID for request correlation and debugging.
     * 
     * <p>Creates a short, URL-safe identifier that can be used to correlate
     * log entries, error reports, and debugging sessions across distributed systems.</p>
     * 
     * @return 8-character unique trace identifier
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Extracts field name from Bean Validation constraint violation.
     * 
     * <p>Processes the property path from constraint violations to extract
     * the actual field name, handling nested object paths and method parameters.</p>
     * 
     * @param violation the constraint violation to process
     * @return the field name that caused the violation
     */
    private String getFieldName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        return propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
    }

    /**
     * Builds standard request metadata for error responses.
     * 
     * <p>Creates a metadata map containing request context information
     * for audit trails, debugging, and error analysis. Safely handles
     * cases where user principal might not be available.</p>
     * 
     * <h4>Included Metadata:</h4>
     * <ul>
     *   <li><strong>method:</strong> HTTP method (GET, POST, PUT, DELETE, etc.)</li>
     *   <li><strong>userId:</strong> Authenticated user identifier (if available)</li>
     *   <li><strong>userAgent:</strong> Client user agent string (if available)</li>
     *   <li><strong>remoteAddr:</strong> Client IP address (considering proxies)</li>
     * </ul>
     * 
     * @param request the HTTP servlet request
     * @return metadata map with request context information
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
     * Extracts the real client IP address considering proxy headers.
     * 
     * <p>Attempts to determine the actual client IP address by checking
     * common proxy headers before falling back to the remote address.
     * This is important for accurate logging and security monitoring.</p>
     * 
     * @param request the HTTP servlet request
     * @return the client IP address
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

    /**
     * Sanitizes rejected values for safe logging and response inclusion.
     * 
     * <p>Removes or masks sensitive information from rejected values to prevent
     * accidental exposure of passwords, tokens, or other sensitive data in
     * error responses and log files.</p>
     * 
     * <h4>Sanitization Rules:</h4>
     * <ul>
     *   <li>Null values returned as "null"</li>
     *   <li>Strings containing "password" are masked</li>
     *   <li>Strings containing "token" are masked</li>
     *   <li>Very long strings are truncated to prevent log pollution</li>
     *   <li>Other values are converted to string safely</li>
     * </ul>
     * 
     * @param value the rejected value to sanitize
     * @return sanitized string representation of the value
     */
    private Object sanitizeRejectedValue(Object value) {
        if (value == null) {
            return "null";
        }
        
        String stringValue = value.toString();
        String lowerCase = stringValue.toLowerCase();
        
        // Mask sensitive fields
        if (lowerCase.contains("password") || lowerCase.contains("token") || 
            lowerCase.contains("secret") || lowerCase.contains("key")) {
            return "***MASKED***";
        }
        
        // Truncate very long values
        if (stringValue.length() > 100) {
            return stringValue.substring(0, 97) + "...";
        }
        
        return value;
    }
}
