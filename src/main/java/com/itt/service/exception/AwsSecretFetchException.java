package com.itt.service.exception;

import com.itt.service.enums.ErrorCode;

/**
 * Exception thrown when AWS secret fetching operations fail.
 * 
 * <p>This exception is thrown during AWS Secrets Manager operations when
 * the system cannot retrieve required secrets or when secret access fails.</p>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see CustomException
 */
public class AwsSecretFetchException extends CustomException {
	private static final long serialVersionUID = 1L;

	public AwsSecretFetchException(String message) {
		super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, message);
	}

	public AwsSecretFetchException(String message, Throwable cause) {
		super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, message, cause);
	}
}