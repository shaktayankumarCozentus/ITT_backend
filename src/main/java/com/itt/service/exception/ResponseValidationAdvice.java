package com.itt.service.exception;

import com.itt.service.dto.ApiResponse;
import com.itt.service.enums.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Advice that intercepts outgoing ApiResponse bodies and applies Bean Validation
 * to ensure the response contract is not violated. If violations are found,
 * an error ApiResponse is returned with HTTP 500 status.
 */
@ControllerAdvice(basePackages = "com.itt.service.**")
@Slf4j
@Hidden
public class ResponseValidationAdvice implements ResponseBodyAdvice<Object> {

    private final Validator validator;

    public ResponseValidationAdvice(Validator validator) {
        this.validator = validator;
    }

    /**
     * Determine if this advice applies: only for controller methods returning ApiResponse.
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    /**
     * Validate the ApiResponse object before writing body to the client.
     * On constraint violations, log details and replace the body with an error response.
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (!(body instanceof ApiResponse<?> api)) {
            return body;
        }
        // Validate all fields of ApiResponse against declared constraints
        Set<ConstraintViolation<ApiResponse<?>>> violations = validator.validate(api);
        if (!violations.isEmpty()) {
            // Log violation details for debugging
            violations.forEach(v -> log.error(
                "Response validation error: {} invalid due to {}", v.getPropertyPath(), v.getMessage()));

            // Aggregate messages and produce error response
            String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
            ApiResponse<Object> error = ApiResponse.error(
                    ErrorCode.RESPONSE_VALIDATION_ERROR, message);
            error.setTimestamp(LocalDateTime.now());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return error;
        }
        return body;
    }
}
