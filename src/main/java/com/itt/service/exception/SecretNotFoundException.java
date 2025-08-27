package com.itt.service.exception;

public class SecretNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SecretNotFoundException(String message) {
		super(message);
	}

	public SecretNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}