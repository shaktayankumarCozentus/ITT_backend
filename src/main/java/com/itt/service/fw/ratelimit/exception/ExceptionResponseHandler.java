package com.itt.service.fw.ratelimit.exception;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.itt.service.dto.ApiResponse;
import com.itt.service.enums.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.annotation.Order;

@Slf4j
@ControllerAdvice(basePackages = "com.itt.service.fw.ratelimit.**") // ← RESTRICTED to rate limiting only
@Order(2) // ← Lower priority than GlobalExceptionHandler (@Order(1))
@Hidden
public class ExceptionResponseHandler {
	
	private static final String NOT_READABLE_REQUEST_ERROR_MESSAGE = "The request is malformed. Ensure the JSON structure is correct.";
	
	@ResponseBody
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiResponse<Void>> responseStatusExceptionHandler(final ResponseStatusException exception) {
		logException(exception);
		
		ApiResponse<Void> response = ApiResponse.<Void>builder()
				.success(false)
				.errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
				.message(exception.getReason())
				.build();
				
		return ResponseEntity.status(exception.getStatusCode()).body(response);
	}


	
	@ResponseBody
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
		logException(exception);
		
		String message = NOT_READABLE_REQUEST_ERROR_MESSAGE;

		if (exception.getCause() instanceof InvalidFormatException invalidFormatException) {
			message = invalidFormatException.getPath().stream()
					.map(Reference::getFieldName)
					.findFirst()
					.map(fieldName -> String.format("Invalid value '%s' for '%s'.", 
								invalidFormatException.getValue(), fieldName))
					.orElse(message);
		} else if (exception.getCause() instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
			message = unrecognizedPropertyException.getPath().stream()
					.map(Reference::getFieldName)
					.findFirst()
					.map(fieldName -> String.format("Unrecognized property '%s' detected.", fieldName))
					.orElse(message);
		} else if (exception.getCause() instanceof MismatchedInputException mismatchedInputException) {
			message = mismatchedInputException.getPath().stream()
					.map(Reference::getFieldName)
					.findFirst()
					.map(fieldName -> String.format("Invalid data type for field '%s'.", fieldName))
					.orElse(message);
		}

		ApiResponse<Void> response = ApiResponse.<Void>builder()
				.success(false)
				.errorCode(ErrorCode.INVALID_DATA_FORMAT.getCode())
				.message(message)
				.build();
				
		return ResponseEntity.badRequest().body(response);
	}

	@ResponseBody
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> serverExceptionHandler(final Exception exception) {
		logException(exception);
		
		ApiResponse<Void> response = ApiResponse.<Void>builder()
				.success(false)
				.errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
				.message("Something went wrong.")
				.build();
				
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
	
	private void logException(final @NonNull Exception exception) {
		log.error("Exception encountered: {}", exception.getMessage(), exception);
	}

}