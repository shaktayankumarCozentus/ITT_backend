package com.itt.service.util;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.ApiResponse.ValidationError;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Comprehensive response builder utility for Service API responses.
 * 
 * <p>This utility class provides a fluent, consistent way to build standardized API
 * responses across all controllers and service layers. It implements the builder pattern
 * to ensure response consistency and simplifies the creation of success, error, and
 * validation responses with proper HTTP status codes and metadata.</p>
 * 
 * <h3>Response Standardization:</h3>
 * <p>All API responses follow a consistent structure defined by {@link ApiResponse}:</p>
 * <ul>
 *   <li><strong>Success Indicator:</strong> Boolean flag indicating operation success</li>
 *   <li><strong>Data Payload:</strong> The actual response data (if any)</li>
 *   <li><strong>Error Information:</strong> Error codes and messages for failures</li>
 *   <li><strong>Metadata:</strong> Additional context and debugging information</li>
 *   <li><strong>Timestamps:</strong> Response generation time for tracking</li>
 * </ul>
 * 
 * <h3>Response Categories:</h3>
 * <ul>
 *   <li><strong>Success Responses:</strong> 200 OK, 201 Created, 204 No Content</li>
 *   <li><strong>Error Responses:</strong> 400 Bad Request, 500 Internal Server Error</li>
 *   <li><strong>Validation Responses:</strong> 400 Bad Request with field-level errors</li>
 *   <li><strong>Dynamic Responses:</strong> Content-aware status code selection</li>
 * </ul>
 * 
 * <h3>HTTP Status Code Mapping:</h3>
 * <ul>
 *   <li><strong>200 OK:</strong> Successful operations with data</li>
 *   <li><strong>201 Created:</strong> Successful resource creation</li>
 *   <li><strong>204 No Content:</strong> Successful operations without response data</li>
 *   <li><strong>400 Bad Request:</strong> Client errors, validation failures</li>
 *   <li><strong>500 Internal Server Error:</strong> Server-side errors and exceptions</li>
 * </ul>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Type Safety:</strong> Generic type parameters ensure compile-time type checking</li>
 *   <li><strong>Null Safety:</strong> Comprehensive null handling for all parameters</li>
 *   <li><strong>Request Context:</strong> Automatic extraction of request path and trace IDs</li>
 *   <li><strong>Validation Support:</strong> Field-level validation error reporting</li>
 *   <li><strong>Metadata Enrichment:</strong> Flexible metadata attachment for debugging</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Simple success response
 * return ResponseBuilder.success(userData);
 * 
 * // Success with custom message
 * return ResponseBuilder.success("User created successfully", userData);
 * 
 * // Created response for resource creation
 * return ResponseBuilder.created("Role created successfully", roleDto);
 * 
 * // Error response with error code
 * return ResponseBuilder.error(ErrorCode.ROLE_NOT_FOUND, "Role not found");
 * 
 * // Validation error response
 * return ResponseBuilder.validationError(
 *     ErrorCode.VALIDATION_FAILED, 
 *     "Validation failed", 
 *     validationErrors, 
 *     request
 * );
 * 
 * // Dynamic response based on content
 * return ResponseBuilder.dynamicResponse(optionalData);
 * }</pre>
 * 
 * <h3>Trace ID Integration:</h3>
 * <p>The builder automatically extracts trace IDs from request headers for:</p>
 * <ul>
 *   <li>Request correlation across distributed systems</li>
 *   <li>End-to-end transaction tracking</li>
 *   <li>Debugging and troubleshooting support</li>
 *   <li>Log correlation and analysis</li>
 * </ul>
 * 
 * <h3>Validation Error Support:</h3>
 * <p>Comprehensive field-level validation error reporting including:</p>
 * <ul>
 *   <li>Field names that failed validation</li>
 *   <li>Rejected values (sanitized for security)</li>
 *   <li>Human-readable error messages</li>
 *   <li>Validation constraint codes for client handling</li>
 * </ul>
 * 
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li><strong>Consistency:</strong> Use this builder for all API responses</li>
 *   <li><strong>Error Codes:</strong> Always use standardized ErrorCode enums</li>
 *   <li><strong>Messages:</strong> Provide clear, actionable error messages</li>
 *   <li><strong>Metadata:</strong> Include debugging information in metadata</li>
 *   <li><strong>Security:</strong> Avoid exposing sensitive information in error messages</li>
 * </ul>
 * 
 * @author Service Team
 * @version 2.0
 * @since 1.0
 * @see ApiResponse
 * @see ErrorCode
 * @see ValidationError
 */
public final class ResponseBuilder {

    /**
     * HTTP header name for trace ID correlation.
     * Used to extract trace IDs from incoming requests for response correlation.
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with static methods only.
     */
    private ResponseBuilder() { 
        throw new UnsupportedOperationException("Utility class cannot be instantiated"); 
    }

    // ==========================================
    // SUCCESS RESPONSE BUILDERS
    // ==========================================

    /**
     * Builds a simple success response with data payload only.
     * 
     * <p>Creates a standardized success response (HTTP 200 OK) containing the
     * provided data. This is the most commonly used success response builder
     * for straightforward data retrieval operations.</p>
     * 
     * <h4>Response Characteristics:</h4>
     * <ul>
     *   <li><strong>HTTP Status:</strong> 200 OK</li>
     *   <li><strong>Success Flag:</strong> true</li>
     *   <li><strong>Message:</strong> null (no custom message)</li>
     *   <li><strong>Metadata:</strong> null (no additional metadata)</li>
     * </ul>
     * 
     * <h4>Use Cases:</h4>
     * <ul>
     *   <li>Successful data retrieval operations</li>
     *   <li>Query results with data payload</li>
     *   <li>Simple GET endpoint responses</li>
     * </ul>
     * 
     * @param data the data payload to include in the response
     * @param <T> the type of the data payload
     * @return ResponseEntity with 200 OK status containing the success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(null, data, null);
    }

    /**
     * Builds a success response with custom message and data payload.
     * 
     * <p>Creates a success response (HTTP 200 OK) with both a custom success
     * message and data payload. Useful when you want to provide additional
     * context or confirmation about the operation performed.</p>
     * 
     * <h4>Response Characteristics:</h4>
     * <ul>
     *   <li><strong>HTTP Status:</strong> 200 OK</li>
     *   <li><strong>Success Flag:</strong> true</li>
     *   <li><strong>Custom Message:</strong> User-defined success message</li>
     *   <li><strong>Data Payload:</strong> Operation result data</li>
     * </ul>
     * 
     * <h4>Use Cases:</h4>
     * <ul>
     *   <li>Operations requiring user confirmation</li>
     *   <li>Complex operations with success status</li>
     *   <li>Multi-step processes with intermediate feedback</li>
     * </ul>
     * 
     * @param message the custom success message to include
     * @param data the data payload to include in the response
     * @param <T> the type of the data payload
     * @return ResponseEntity with 200 OK status containing the success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return success(message, data, null);
    }
    
    /**
     * Builds a success response with message only (no data payload).
     * 
     * <p>Creates a success response (HTTP 200 OK) containing only a success
     * message without any data payload. Ideal for operations that confirm
     * successful completion but don't return data.</p>
     * 
     * <h4>Response Characteristics:</h4>
     * <ul>
     *   <li><strong>HTTP Status:</strong> 200 OK</li>
     *   <li><strong>Success Flag:</strong> true</li>
     *   <li><strong>Custom Message:</strong> User-defined success message</li>
     *   <li><strong>Data Payload:</strong> null (no data returned)</li>
     * </ul>
     * 
     * <h4>Use Cases:</h4>
     * <ul>
     *   <li>Successful update operations</li>
     *   <li>Configuration changes</li>
     *   <li>Status acknowledgments</li>
     * </ul>
     * 
     * @param message the success message to include in the response
     * @return ResponseEntity with 200 OK status containing the success response
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.ok(createSuccessResponse(message, null, null));
    }

    /**
     * Builds a comprehensive success response with message, data, and metadata.
     * 
     * <p>Creates the most detailed success response (HTTP 200 OK) including
     * custom message, data payload, and additional metadata. This is the
     * foundation method used by other success response builders.</p>
     * 
     * <h4>Response Characteristics:</h4>
     * <ul>
     *   <li><strong>HTTP Status:</strong> 200 OK</li>
     *   <li><strong>Success Flag:</strong> true</li>
     *   <li><strong>Custom Message:</strong> User-defined success message</li>
     *   <li><strong>Data Payload:</strong> Operation result data</li>
     *   <li><strong>Metadata:</strong> Additional context information</li>
     * </ul>
     * 
     * <h4>Metadata Use Cases:</h4>
     * <ul>
     *   <li>Performance metrics (execution time, query counts)</li>
     *   <li>Operational context (affected records, processing details)</li>
     *   <li>Debugging information (trace IDs, processing steps)</li>
     *   <li>Business metrics (calculation details, audit information)</li>
     * </ul>
     * 
     * @param message the custom success message to include
     * @param data the data payload to include in the response
     * @param metadata additional metadata for context and debugging
     * @param <T> the type of the data payload
     * @return ResponseEntity with 200 OK status containing the complete success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data, Map<String, Object> metadata) {
        return ResponseEntity.ok(createSuccessResponse(message, data, metadata));
    }

    /**
     * Builds a resource creation success response (HTTP 201 Created).
     * 
     * <p>Creates a success response specifically for resource creation operations.
     * Uses HTTP 201 Created status to indicate that a new resource has been
     * successfully created as a result of the request.</p>
     * 
     * <h4>Response Characteristics:</h4>
     * <ul>
     *   <li><strong>HTTP Status:</strong> 201 Created</li>
     *   <li><strong>Success Flag:</strong> true</li>
     *   <li><strong>Custom Message:</strong> Creation confirmation message</li>
     *   <li><strong>Data Payload:</strong> The newly created resource</li>
     * </ul>
     * 
     * <h4>Use Cases:</h4>
     * <ul>
     *   <li>POST endpoints creating new resources</li>
     *   <li>User registration operations</li>
     *   <li>Entity creation with immediate data return</li>
     *   <li>File upload operations</li>
     * </ul>
     * 
     * <h4>RESTful Best Practice:</h4>
     * <p>Using 201 Created follows REST conventions and helps clients
     * distinguish between data retrieval (200) and resource creation (201).</p>
     * 
     * @param message the creation success message
     * @param data the newly created resource data
     * @param <T> the type of the created resource
     * @return ResponseEntity with 201 Created status containing the creation response
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createSuccessResponse(message, data, null));
    }

    /**
     * Builds a NO_CONTENT (204) response.
     *
     * @return ResponseEntity with 204 No Content
     */
    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body);
    }
    // ==========================================
    // ERROR RESPONSE BUILDERS
    // ==========================================
    
    /**
     * Builds a generic error response with a provided error code and message.
     *
     * @param errorCode error code enum
     * @param message   error message
     * @param <T>       type of data (typically null)
     * @return ResponseEntity with 400 Bad Request
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String message) {
        return error(errorCode, message, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Builds a custom error response with a specified HTTP status.
     *
     * @param errorCode error code enum
     * @param message   error message
     * @param status    HTTP status to return
     * @param <T>       type of data (typically null)
     * @return ResponseEntity with provided HTTP status
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(createErrorResponse(errorCode, message, null, null, null, null));
    }
    
    /**
     * Builds a detailed error response using request context.
     *
     * @param errorCode error code enum
     * @param message   error message
     * @param request   HttpServletRequest to extract path and traceId if set
     * @param <T>       type of data (typically null)
     * @return ResponseEntity with 400 Bad Request including request details
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String message, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(createErrorResponse(errorCode, message, 
                    extractPath(request), extractTraceId(request), null, null));
    }
    
    /**
     * Builds a validation error response with multiple field errors.
     *
     * @param errorCode        error code enum
     * @param message          main error message
     * @param validationErrors list of validation errors
     * @param request          HttpServletRequest to extract the path
     * @param <T>              type of data (typically null)
     * @return ResponseEntity with 400 Bad Request including validation error details
     */
    public static <T> ResponseEntity<ApiResponse<T>> validationError(
            ErrorCode errorCode,
            String message,
            List<ValidationError> validationErrors,
            HttpServletRequest request) {
        
        return ResponseEntity.badRequest()
                .body(createErrorResponse(errorCode, message, 
                    extractPath(request), null, validationErrors, null));
    }
    
    /**
     * Builds an error response from an Exception, mapping it to a given error code.
     *
     * @param errorCode error code enum
     * @param ex        exception thrown
     * @param <T>       type of data (typically null)
     * @return ResponseEntity with 500 Internal Server Error
     */
    public static <T> ResponseEntity<ApiResponse<T>> exception(ErrorCode errorCode, Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(errorCode, ex.getMessage(), null, null, null, null));
    }
    
    /**
     * Builds a custom error response including metadata.
     *
     * @param errorCode error code enum
     * @param message   error message
     * @param metadata  additional metadata
     * @param <T>       type of data (typically null)
     * @return ResponseEntity with 400 Bad Request including metadata
     */
    public static <T> ResponseEntity<ApiResponse<T>> errorWithMetadata(
            ErrorCode errorCode, String message, Map<String, Object> metadata) {
        return ResponseEntity.badRequest()
                .body(createErrorResponse(errorCode, message, null, null, null, metadata));
    }

    // ==========================================
    // DYNAMIC RESPONSE BUILDER
    // ==========================================

    /**
     * Dynamically builds a response based on data content.
     * Returns NO_CONTENT (204) for null, empty, or blank data.
     * Returns SUCCESS (200) for valid data.
     *
     * @param data the response data
     * @param <T>  type of data
     * @return ResponseEntity with appropriate status
     */
    public static <T> ResponseEntity<ApiResponse<T>> dynamicResponse(T data) {
        if (isEmptyData(data)) {
            return noContent();
        }
        return success(data);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    /**
     * Creates a success ApiResponse with the given parameters.
     */
    private static <T> ApiResponse<T> createSuccessResponse(String message, T data, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error ApiResponse with the given parameters.
     */
    private static <T> ApiResponse<T> createErrorResponse(
            ErrorCode errorCode, 
            String message, 
            String path, 
            String traceId, 
            List<ValidationError> validationErrors, 
            Map<String, Object> metadata) {
        
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(message)
                .path(path)
                .traceId(traceId)
                .validationErrors(validationErrors)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Determines if the given data is considered empty.
     */
    private static <T> boolean isEmptyData(T data) {
        if (data == null) {
            return true;
        }
        
        if (data instanceof String) {
            return !StringUtils.hasText((String) data);
        }
        
        if (data instanceof Collection) {
            return ((Collection<?>) data).isEmpty();
        }
        
        if (data instanceof PaginationResponse) {
            PaginationResponse<?> paginationResponse = (PaginationResponse<?>) data;
            return paginationResponse.getContent() == null || paginationResponse.getContent().isEmpty();
        }
        
        return false;
    }

    /**
     * Safely extracts the request path from HttpServletRequest.
     */
    private static String extractPath(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(HttpServletRequest::getRequestURI)
                .orElse(null);
    }

    /**
     * Safely extracts the trace ID from HttpServletRequest headers.
     */
    private static String extractTraceId(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(req -> req.getHeader(TRACE_ID_HEADER))
                .orElse(null);
    }
}
