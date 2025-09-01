package com.itt.service.exception;

import com.itt.service.enums.ErrorCode;

/**
 * Exception thrown when secret parsing operations fail.
 * 
 * <p>This exception is thrown when the system cannot parse or decode
 * secret values retrieved from the secrets management system.</p>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see CustomException
 */
public class SecretParsingException extends CustomException {
	private static final long serialVersionUID = 1L;

	public SecretParsingException(String message) {
		super(ErrorCode.INVALID_DATA_FORMAT, message);
	}

	public SecretParsingException(String message, Throwable cause) {
		super(ErrorCode.INVALID_DATA_FORMAT, message, cause);
	}
}