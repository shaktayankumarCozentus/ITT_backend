package com.itt.service.exception;

public class SecretParsingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SecretParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}