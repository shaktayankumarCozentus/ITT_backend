package com.itt.service.exception;

import com.itt.service.enums.ErrorCode;

import lombok.Getter;

/**
 * Custom business exception for Service application.
 * 
 * <p>This exception class represents application-specific business errors that occur
 * during normal application flow. Unlike technical exceptions, these represent
 * anticipated error conditions that should be handled gracefully and communicated
 * to the end user in a user-friendly manner.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Error Code Mapping:</strong> Associates exceptions with standardized error codes</li>
 *   <li><strong>User-Friendly Messages:</strong> Separates technical details from user-facing messages</li>
 *   <li><strong>Internationalization Support:</strong> Supports parameterized messages for localization</li>
 *   <li><strong>Cause Chain Preservation:</strong> Maintains original exception context for debugging</li>
 * </ul>
 * 
 * <h3>Error Code Integration:</h3>
 * Each CustomException is associated with an {@link ErrorCode} that provides:
 * <ul>
 *   <li>Standardized error identification</li>
 *   <li>Appropriate HTTP status code mapping</li>
 *   <li>Consistent error categorization</li>
 *   <li>Client-side error handling support</li>
 * </ul>
 * 
 * <h3>Message Parameterization:</h3>
 * Supports message templates with parameters for dynamic content:
 * <pre>{@code
 * // Simple message
 * throw new CustomException(ErrorCode.ROLE_NOT_FOUND, "Role not found");
 * 
 * // Parameterized message
 * throw new CustomException(ErrorCode.ROLE_NOT_FOUND, 
 *     "Role with ID {0} not found", roleId);
 * 
 * // With cause chain
 * throw new CustomException(ErrorCode.DATABASE_ERROR, 
 *     "Failed to save role", databaseException);
 * }</pre>
 * 
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li><strong>Business Logic:</strong> Use for business rule violations and expected error conditions</li>
 *   <li><strong>User Messages:</strong> Provide clear, actionable error messages for end users</li>
 *   <li><strong>Technical Details:</strong> Include technical context in the cause chain, not user message</li>
 *   <li><strong>Error Codes:</strong> Always use appropriate ErrorCode enums for consistency</li>
 * </ul>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see ErrorCode
 * @see ValidationException
 * @see BusinessException
 */
@Getter
public class CustomException extends RuntimeException {
    
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The standardized error code associated with this exception.
     * Used for error categorization and HTTP status mapping.
     */
    private final ErrorCode errorCode;
    
    /**
     * User-friendly error message intended for display to end users.
     * Should be clear, actionable, and free of technical jargon.
     */
    private final String userMessage;
    
    /**
     * Optional message parameters for internationalization and dynamic content.
     * Used with message templates to create localized, context-specific messages.
     */
    private final Object[] messageArgs;
    
    /**
     * Creates a custom exception with error code and user message.
     * 
     * @param errorCode the standardized error code
     * @param userMessage the user-friendly error message
     */
    public CustomException(ErrorCode errorCode, String userMessage) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.messageArgs = null;
    }
    
    /**
     * Creates a custom exception with parameterized user message.
     * 
     * <p>The message arguments are used to populate placeholders in the user message
     * template, enabling dynamic and localized error messages.</p>
     * 
     * @param errorCode the standardized error code
     * @param userMessage the user-friendly error message template
     * @param messageArgs the parameters for message template substitution
     */
    public CustomException(ErrorCode errorCode, String userMessage, Object... messageArgs) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.messageArgs = messageArgs;
    }
    
    /**
     * Creates a custom exception with cause chain preservation.
     * 
     * <p>This constructor maintains the original exception context while providing
     * a business-friendly error message. The cause chain enables detailed debugging
     * while keeping user messages clean and understandable.</p>
     * 
     * @param errorCode the standardized error code
     * @param userMessage the user-friendly error message
     * @param cause the underlying exception that triggered this business error
     */
    public CustomException(ErrorCode errorCode, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.messageArgs = null;
    }
    
    /**
     * Creates a custom exception with both parameterized message and cause chain.
     * 
     * <p>This is the most comprehensive constructor, supporting both dynamic message
     * content and exception cause preservation. Use this when you need full context
     * preservation with user-friendly, parameterized error messages.</p>
     * 
     * @param errorCode the standardized error code
     * @param userMessage the user-friendly error message template
     * @param cause the underlying exception that triggered this business error
     * @param messageArgs the parameters for message template substitution
     */
    public CustomException(ErrorCode errorCode, String userMessage, Throwable cause, Object... messageArgs) {
        super(userMessage, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.messageArgs = messageArgs;
    }
}