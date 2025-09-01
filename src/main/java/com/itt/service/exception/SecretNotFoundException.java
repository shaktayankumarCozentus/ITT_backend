package com.itt.service.exception;

import com.itt.service.enums.ErrorCode;

/**
 * Exception thrown when a required secret is not found.
 * 
 * <p>This exception is thrown when the system cannot locate a required
 * secret in the secrets management system.</p>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see CustomException
 */
public class SecretNotFoundException extends CustomException {
	private static final long serialVersionUID = 1L;

	public SecretNotFoundException(String message) {
		super(ErrorCode.RESOURCE_NOT_FOUND, message);
	}

	public SecretNotFoundException(String message, Throwable cause) {
		super(ErrorCode.RESOURCE_NOT_FOUND, message, cause);
	}
}