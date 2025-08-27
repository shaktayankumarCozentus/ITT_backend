package com.itt.service.fw.logger.core.aspect;

import com.itt.service.fw.logger.api.config.LoggerConfigurationProperties;
import com.itt.service.fw.logger.storage.LoggingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class MethodLoggerAspect {

    private final LoggingService loggingService;
    private final LoggerConfigurationProperties props;
    private final HttpServletRequest request;

    @Around("@annotation(com.itt.framework.logger.api.annotation.Loggable)")
    public Object logMethod(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().toShortString();

        if (props.isLogMethodStart()) {
            loggingService.info("➡️ START: " + methodName + " | Args: " + Arrays.toString(pjp.getArgs()));
        }

        try {
            Object result = pjp.proceed();

            if (props.isLogMethodEnd()) {
                loggingService.info("⬅️ END: " + methodName);
            }

            return result;
        } catch (Throwable ex) {
            loggingService.error("❌ Exception in method " + methodName + ": " + ex.getMessage());
            try {
                loggingService.logExceptionToDB(new Exception(ex), request.getRequestURI(), request.getMethod());
            } catch (Exception logEx) {
                // Log to fallback (only in dev if needed, or ignore)
            }
            throw ex;
        }
    }
}
