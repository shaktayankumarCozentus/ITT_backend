package com.itt.service.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class CustomBadCredentialsException extends BadCredentialsException {

	private static final long serialVersionUID = 1L;

	public CustomBadCredentialsException(String message) {
        super(message);
    }

    public CustomBadCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}