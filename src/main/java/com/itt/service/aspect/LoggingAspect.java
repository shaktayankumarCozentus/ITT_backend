package com.itt.service.aspect;

import com.itt.service.service.FeatureToggleService;
import com.itt.service.util.AsyncLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Aspect for logging entry, exit, and exceptions of controller and service methods.
 * <p>Uses {@link com.itt.service.util.AsyncLogger} to offload log calls
 * asynchronously, and respects the runtime feature toggle {@code async_logging_enabled}.
 */
@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final AsyncLogger asyncLogger;
    private final FeatureToggleService featureToggle;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerBean() {}

    @Pointcut("execution(* com.itt.service.service..*(..)) && !execution(* com.itt.service.service..*FeatureToggle*.*(..))")
    public void serviceLayer() {}

    /**
     * Around advice that logs method entry with arguments, method exit with result and
     * execution time, and exceptions if thrown.
     */
    @Around("controllerBean() || serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // Prevent infinite recursion by checking if this is the FeatureToggleService itself
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        if (className.contains("FeatureToggle")) {
            return joinPoint.proceed();
        }
        
        // Skip async logging if feature toggle is off
        // Use try-catch to prevent infinite recursion in case of issues
        boolean asyncLoggingEnabled = false;
        try {
            asyncLoggingEnabled = featureToggle.isAsyncLoggingEnabled();
        } catch (Exception e) {
            // Fallback to regular execution if feature toggle check fails
            return joinPoint.proceed();
        }
        
        if (!asyncLoggingEnabled) {
            return joinPoint.proceed();
        }
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));

        asyncLogger.info("Entering {}.{}() with arguments [{}]", className, methodName, args);
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            asyncLogger.info("Exiting {}.{}() with result [{}] in {} ms", className, methodName, result, elapsed);
            return result;
        } catch (Throwable ex) {
            asyncLogger.error("Exception in {}.{}() with cause = {} and message = {}",
                    className, methodName,
                    ex.getCause() != null ? ex.getCause() : "NULL",
                    ex.getMessage(), ex);
            throw ex;
        }
    }
}
