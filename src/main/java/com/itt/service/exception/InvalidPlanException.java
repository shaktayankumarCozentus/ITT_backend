package com.itt.service.exception;

import com.itt.service.enums.ErrorCode;

/**
 * Business exception thrown when an invalid or non-existent plan is referenced.
 * 
 * <p>This exception is thrown during plan-related operations when the system
 * cannot find the specified plan or when the plan is invalid for the requested operation.</p>
 * 
 * <p>This exception automatically maps to appropriate HTTP status via the ErrorCode mapping,
 * providing consistent error handling across the application.</p>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see CustomException
 * @see ErrorCode
 */
public class InvalidPlanException extends CustomException {

	private static final long serialVersionUID = 4506094675559975006L;

	/**
	 * Creates an InvalidPlanException with the specified error message.
	 * 
	 * @param reason the error message describing the invalid plan
	 */
	public InvalidPlanException(final String reason) {
		super(ErrorCode.RESOURCE_NOT_FOUND, reason);
	}

	/**
	 * Creates an InvalidPlanException with parameterized message.
	 * 
	 * @param reason the error message template
	 * @param messageArgs the parameters for message template substitution
	 */
	public InvalidPlanException(final String reason, Object... messageArgs) {
		super(ErrorCode.RESOURCE_NOT_FOUND, reason, messageArgs);
	}
}
