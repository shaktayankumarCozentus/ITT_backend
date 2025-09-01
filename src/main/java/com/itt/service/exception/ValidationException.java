package com.itt.service.exception;

import java.util.List;

import com.itt.service.dto.ApiResponse.ValidationError;
import com.itt.service.enums.ErrorCode;

import lombok.Getter;

/**
 * Validation exception for handling detailed field-level validation errors.
 * 
 * <p>This specialized exception extends {@link CustomException} to provide
 * comprehensive validation error reporting with field-level details. It's designed
 * to handle complex validation scenarios where multiple fields may have validation
 * errors that need to be communicated back to the client.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Field-Level Details:</strong> Captures specific field validation failures</li>
 *   <li><strong>Comprehensive Error Info:</strong> Includes field names, rejected values, and error codes</li>
 *   <li><strong>Bulk Validation Support:</strong> Handles multiple validation errors in a single exception</li>
 *   <li><strong>Client-Friendly Format:</strong> Structured data for easy client-side processing</li>
 * </ul>
 * 
 * <h3>Validation Error Structure:</h3>
 * Each {@link ValidationError} contains:
 * <ul>
 *   <li><strong>Field Name:</strong> The name of the field that failed validation</li>
 *   <li><strong>Rejected Value:</strong> The value that was rejected (sanitized for security)</li>
 *   <li><strong>Error Message:</strong> Human-readable description of the validation failure</li>
 *   <li><strong>Error Code:</strong> Machine-readable error identifier for client handling</li>
 * </ul>
 * 
 * <h3>Usage Scenarios:</h3>
 * <ul>
 *   <li><strong>Form Validation:</strong> Complex multi-field form validation errors</li>
 *   <li><strong>Business Rules:</strong> Cross-field validation and business logic violations</li>
 *   <li><strong>Data Import:</strong> Bulk data validation with multiple error points</li>
 *   <li><strong>API Validation:</strong> Request payload validation with detailed feedback</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Single field validation error
 * List<ValidationError> errors = Arrays.asList(
 *     ValidationError.builder()
 *         .field("email")
 *         .rejectedValue("invalid-email")
 *         .message("Invalid email format")
 *         .code("EMAIL_INVALID")
 *         .build()
 * );
 * throw new ValidationException("Validation failed", errors);
 * 
 * // Multiple field validation errors
 * List<ValidationError> errors = Arrays.asList(
 *     ValidationError.builder()
 *         .field("email")
 *         .rejectedValue("invalid-email")
 *         .message("Invalid email format")
 *         .code("EMAIL_INVALID")
 *         .build(),
 *     ValidationError.builder()
 *         .field("password")
 *         .rejectedValue("***MASKED***")
 *         .message("Password must be at least 8 characters")
 *         .code("PASSWORD_TOO_SHORT")
 *         .build()
 * );
 * throw new ValidationException("Multiple validation errors", errors);
 * }</pre>
 * 
 * <h3>Integration with Global Exception Handler:</h3>
 * This exception is automatically processed by {@link GlobalExceptionHandler}
 * to provide consistent error response format with detailed validation information.
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see CustomException
 * @see ValidationError
 * @see GlobalExceptionHandler
 */
@Getter
public class ValidationException extends CustomException {
    
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * List of detailed validation errors with field-level information.
     * Each error contains field name, rejected value, error message, and error code.
     */
    private final List<ValidationError> validationErrors;
    
    /**
     * Creates a validation exception with a user message and detailed validation errors.
     * 
     * <p>This constructor automatically sets the error code to {@link ErrorCode#VALIDATION_FAILED}
     * as all instances of this exception represent validation failures.</p>
     * 
     * @param userMessage the overall validation failure message for the user
     * @param validationErrors the list of specific field validation errors
     * @throws IllegalArgumentException if validationErrors is null or empty
     */
    public ValidationException(String userMessage, List<ValidationError> validationErrors) {
        super(ErrorCode.VALIDATION_FAILED, userMessage);
        if (validationErrors == null || validationErrors.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "Validation errors list cannot be null or empty");
        }
        this.validationErrors = validationErrors;
    }
    
    /**
     * Creates a validation exception with custom error code and detailed validation errors.
     * 
     * <p>This constructor allows for custom error codes in cases where the validation
     * failure represents a specific type of validation error that requires different
     * handling or status codes.</p>
     * 
     * @param errorCode the specific validation error code
     * @param userMessage the overall validation failure message for the user
     * @param validationErrors the list of specific field validation errors
     * @throws IllegalArgumentException if validationErrors is null or empty
     */
    public ValidationException(ErrorCode errorCode, String userMessage, List<ValidationError> validationErrors) {
        super(errorCode, userMessage);
        if (validationErrors == null || validationErrors.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "Validation errors list cannot be null or empty");
        }
        this.validationErrors = validationErrors;
    }
}