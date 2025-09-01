package com.itt.service.exception;

import com.itt.service.dto.ApiResponse;
import com.itt.service.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * Custom HandlerExceptionResolver with the highest precedence to intercept 
 * MethodArgumentNotValidException BEFORE Spring's DefaultHandlerExceptionResolver.
 * 
 * This resolver specifically handles validation exceptions and returns our 
 * simple error format per the Exception Framework Guide.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomValidationExceptionResolver implements HandlerExceptionResolver, Ordered {
    
    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Higher precedence than DefaultHandlerExceptionResolver
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, 
                                       Object handler, Exception ex) {
        
        if (ex instanceof MethodArgumentNotValidException manvEx) {
            log.info("Custom validation resolver handling {}: {} field errors, {} global errors", 
                    manvEx.getBindingResult().getObjectName(),
                    manvEx.getBindingResult().getFieldErrors().size(),
                    manvEx.getBindingResult().getGlobalErrors().size());
            
            // Create ValidationError objects for field errors
            List<ApiResponse.ValidationError> validationErrors = manvEx.getBindingResult().getFieldErrors().stream()
                    .map(error -> ApiResponse.ValidationError.builder()
                            .field(error.getField())
                            .rejectedValue(error.getRejectedValue())
                            .message(error.getDefaultMessage())
                            .code(error.getCode())
                            .build())
                    .toList();
            
            // Determine main error message
            String message;
            if (!validationErrors.isEmpty()) {
                // Use first field error message
                message = validationErrors.get(0).getMessage();
            } else {
                // Check global errors (like @SaveRoleRequestConstraint)
                message = manvEx.getBindingResult().getGlobalErrors().stream()
                        .findFirst()
                        .map(org.springframework.context.MessageSourceResolvable::getDefaultMessage)
                        .orElse("Validation failed");
            }
            
            // Create proper ApiResponse following framework structure
            ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .errorCode(ErrorCode.VALIDATION_FAILED.getCode())
                    .message(message)
                    .validationErrors(validationErrors.isEmpty() ? null : validationErrors)
                    .path(request.getRequestURI())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            
            try {
                // Set response properties
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                
                // Write JSON response
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                response.getWriter().write(jsonResponse);
                response.getWriter().flush();
                
                // Return empty ModelAndView to indicate we handled the exception
                return new ModelAndView();
                
            } catch (IOException e) {
                log.error("Error writing validation exception response: {}", e.getMessage(), e);
            }
        }
        
        // Return null to let other resolvers handle other exceptions
        return null;
    }
}