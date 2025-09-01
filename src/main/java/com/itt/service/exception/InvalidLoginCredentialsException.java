package com.itt.service.exception;

import com.itt.service.enums.ErrorCode;

/**
 * Authentication exception thrown when invalid login credentials are provided.
 * 
 * <p>This exception is thrown during authentication processes when the provided
 * credentials (username/password, token, etc.) are invalid or cannot be verified.</p>
 * 
 * <p>This exception automatically maps to HTTP 401 Unauthorized status via the 
 * ErrorCode.INVALID_CREDENTIALS mapping, providing consistent authentication error handling.</p>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see CustomException
 * @see ErrorCode
 */
public class InvalidLoginCredentialsException extends CustomException {

	private static final long serialVersionUID = 7439642984069939024L;
	private static final String DEFAULT_MESSAGE = "Invalid login credentials provided";

	/**
	 * Creates an InvalidLoginCredentialsException with the default message.
	 */
	public InvalidLoginCredentialsException() {
		super(ErrorCode.INVALID_CREDENTIALS, DEFAULT_MESSAGE);
	}

	/**
	 * Creates an InvalidLoginCredentialsException with a custom message.
	 * 
	 * @param message the custom error message
	 */
	public InvalidLoginCredentialsException(String message) {
		super(ErrorCode.INVALID_CREDENTIALS, message);
	}
}