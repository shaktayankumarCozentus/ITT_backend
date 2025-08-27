package com.itt.service.fw.logger.exception;

import com.itt.service.fw.logger.storage.LoggingService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.itt.service.**")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class GlobalExceptionLogger {

    private final LoggingService loggingService;

    @ExceptionHandler(Exception.class)
    public void handleException(Exception ex, HttpServletRequest request) {
        log.error("❌ Exception occurred : {} ", ex);
        loggingService.logExceptionToDB(ex, request.getRequestURI(), request.getMethod());
    }
}


