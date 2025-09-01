package com.itt.service.exception;

import org.springframework.stereotype.Component;

import com.itt.service.constants.ErrorMessages;
import com.itt.service.enums.ErrorCode;

@Component
public class ExceptionFactory {

	// Business Exceptions
	public static BusinessException userAlreadyExists() {
		return new BusinessException(ErrorCode.USER_ALREADY_EXISTS, ErrorMessages.USER_ALREADY_EXISTS);
	}

	public static BusinessException userNotFound() {
		return new BusinessException(ErrorCode.USER_NOT_FOUND, ErrorMessages.USER_NOT_FOUND);
	}

	public static BusinessException emailAlreadyRegistered() {
		return new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED, ErrorMessages.EMAIL_ALREADY_REGISTERED);
	}

	public static BusinessException insufficientBalance() {
		return new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, ErrorMessages.INSUFFICIENT_BALANCE);
	}

	public static BusinessException operationNotAllowed() {
		return new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, ErrorMessages.OPERATION_NOT_ALLOWED);
	}

	// Authentication Exceptions
	public static CustomException invalidCredentials() {
		return new CustomException(ErrorCode.INVALID_CREDENTIALS, ErrorMessages.INVALID_CREDENTIALS);
	}

	public static CustomException tokenExpired() {
		return new CustomException(ErrorCode.TOKEN_EXPIRED, ErrorMessages.TOKEN_EXPIRED);
	}

	public static CustomException accountLocked() {
		return new CustomException(ErrorCode.ACCOUNT_LOCKED, ErrorMessages.ACCOUNT_LOCKED);
	}

	// File Exceptions
	public static CustomException fileNotFound() {
		return new CustomException(ErrorCode.FILE_NOT_FOUND, ErrorMessages.FILE_NOT_FOUND);
	}

	public static CustomException invalidFileFormat(String supportedFormats) {
		return new CustomException(ErrorCode.INVALID_FILE_FORMAT, ErrorMessages.INVALID_FILE_FORMAT, supportedFormats);
	}

	public static CustomException fileUploadFailed() {
		return new CustomException(ErrorCode.FILE_UPLOAD_FAILED, ErrorMessages.FILE_UPLOAD_FAILED);
	}

	// Generic Exceptions
	public static CustomException resourceNotFound(String resourceName) {
		return new CustomException(ErrorCode.RESOURCE_NOT_FOUND,
				ErrorMessages.RESOURCE_NOT_FOUND + ": " + resourceName);
	}

	public static CustomException accessDenied() {
		return new CustomException(ErrorCode.ACCESS_DENIED, ErrorMessages.ACCESS_DENIED);
	}

	public static CustomException invalidRequest(String details) {
		return new CustomException(ErrorCode.INVALID_REQUEST,
				ErrorMessages.INVALID_REQUEST + (details != null ? ": " + details : ""));
	}
}
