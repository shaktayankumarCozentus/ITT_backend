package com.itt.service.exception;

public class AwsSecretFetchException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AwsSecretFetchException(String message) {
		super(message);
	}

	public AwsSecretFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}