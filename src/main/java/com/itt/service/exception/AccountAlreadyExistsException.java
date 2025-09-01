package com.itt.service.exception;

import com.itt.service.enums.ErrorCode;

/**
 * Business exception thrown when attempting to create an account that already exists.
 * 
 * <p>This exception is thrown during account registration or creation processes
 * when the system detects that an account with the same identifier (email, username, etc.)
 * already exists in the system.</p>
 * 
 * <p>This exception automatically maps to HTTP 409 Conflict status via the ErrorCode.ACCOUNT_ALREADY_EXISTS
 * mapping, providing consistent error handling across the application.</p>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see CustomException
 * @see ErrorCode
 */
public class AccountAlreadyExistsException extends CustomException {

	private static final long serialVersionUID = 7439642984069939024L;

	/**
	 * Creates an AccountAlreadyExistsException with the specified error message.
	 * 
	 * @param reason the error message describing which account already exists
	 */
	public AccountAlreadyExistsException(final String reason) {
		super(ErrorCode.USER_ALREADY_EXISTS, reason);
	}

	/**
	 * Creates an AccountAlreadyExistsException with parameterized message.
	 * 
	 * @param reason the error message template
	 * @param messageArgs the parameters for message template substitution
	 */
	public AccountAlreadyExistsException(final String reason, Object... messageArgs) {
		super(ErrorCode.USER_ALREADY_EXISTS, reason, messageArgs);
	}
}