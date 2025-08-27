package com.itt.service.exception;

import com.itt.service.enums.ErrorCode;

/**
 * Business logic exception for Service application.
 * 
 * <p>This exception class represents business rule violations and domain-specific
 * errors that occur during business logic execution. It serves as a semantic
 * wrapper around {@link CustomException} to clearly identify business logic
 * failures as distinct from technical or validation errors.</p>
 * 
 * <h3>Business Exception Categories:</h3>
 * <ul>
 *   <li><strong>Domain Rule Violations:</strong> Business rules defined by the domain model</li>
 *   <li><strong>State Transition Errors:</strong> Invalid state changes in business entities</li>
 *   <li><strong>Business Process Failures:</strong> Workflow and process constraint violations</li>
 *   <li><strong>Authorization Failures:</strong> Business-level permission and access control</li>
 * </ul>
 * 
 * <h3>When to Use BusinessException:</h3>
 * <ul>
 *   <li><strong>Domain Logic:</strong> When business rules prevent an operation</li>
 *   <li><strong>State Management:</strong> When entity state transitions are invalid</li>
 *   <li><strong>Business Constraints:</strong> When business constraints are violated</li>
 *   <li><strong>Workflow Control:</strong> When business processes cannot proceed</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Domain rule violation
 * if (role.isSystemRole() && !user.isSystemAdmin()) {
 *     throw new BusinessException(ErrorCode.INSUFFICIENT_PRIVILEGES, 
 *         "Only system administrators can modify system roles");
 * }
 * 
 * // State transition error
 * if (subscription.getStatus() == Status.CANCELLED) {
 *     throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, 
 *         "Cannot modify cancelled subscription");
 * }
 * 
 * // Business process failure
 * if (company.hasActiveSubscriptions()) {
 *     throw new BusinessException(ErrorCode.BUSINESS_CONSTRAINT_VIOLATION, 
 *         "Cannot delete company with active subscriptions");
 * }
 * }</pre>
 * 
 * <h3>Exception Hierarchy:</h3>
 * <pre>
 * RuntimeException
 *   └── CustomException
 *       ├── BusinessException (this class)
 *       ├── ValidationException
 *       └── Other domain-specific exceptions
 * </pre>
 * 
 * <h3>Error Handling Strategy:</h3>
 * BusinessExceptions are typically:
 * <ul>
 *   <li>Logged at WARN level (expected business errors)</li>
 *   <li>Returned with appropriate HTTP status codes (400, 409, 403)</li>
 *   <li>Translated to user-friendly error messages</li>
 *   <li>Used for client-side business logic handling</li>
 * </ul>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see CustomException
 * @see ValidationException
 * @see ErrorCode
 */
public class BusinessException extends CustomException {
    
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a business exception with error code and user message.
     * 
     * <p>Use this constructor for simple business rule violations where
     * no additional context or parameterization is needed.</p>
     * 
     * @param errorCode the business error code that categorizes this failure
     * @param userMessage the business-friendly error message for end users
     */
    public BusinessException(ErrorCode errorCode, String userMessage) {
        super(errorCode, userMessage);
    }
    
    /**
     * Creates a business exception with parameterized user message.
     * 
     * <p>Use this constructor when the error message needs to include dynamic
     * content such as entity names, IDs, or other contextual information.</p>
     * 
     * @param errorCode the business error code that categorizes this failure
     * @param userMessage the business-friendly error message template
     * @param messageArgs the parameters for message template substitution
     */
    public BusinessException(ErrorCode errorCode, String userMessage, Object... messageArgs) {
        super(errorCode, userMessage, messageArgs);
    }
    
    /**
     * Creates a business exception with cause chain preservation.
     * 
     * <p>Use this constructor when a business rule violation is triggered by
     * an underlying technical error that should be preserved for debugging
     * while presenting a business-friendly message to the user.</p>
     * 
     * @param errorCode the business error code that categorizes this failure
     * @param userMessage the business-friendly error message for end users
     * @param cause the underlying technical cause that triggered this business error
     */
    public BusinessException(ErrorCode errorCode, String userMessage, Throwable cause) {
        super(errorCode, userMessage, cause);
    }
    
    /**
     * Creates a business exception with both parameterized message and cause chain.
     * 
     * <p>This is the most comprehensive constructor for complex business errors
     * that require both dynamic message content and technical context preservation.</p>
     * 
     * @param errorCode the business error code that categorizes this failure
     * @param userMessage the business-friendly error message template
     * @param cause the underlying technical cause that triggered this business error
     * @param messageArgs the parameters for message template substitution
     */
    public BusinessException(ErrorCode errorCode, String userMessage, Throwable cause, Object... messageArgs) {
        super(errorCode, userMessage, cause, messageArgs);
    }
}
