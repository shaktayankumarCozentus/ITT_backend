package com.itt.service.aspect;

import com.itt.service.annotation.ReadOnlyDataSource;
import com.itt.service.annotation.WriteDataSource;
import com.itt.service.config.DataSourceContextHolder;
import com.itt.service.config.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * AOP Aspect for managing datasource routing based on annotations.
 * 
 * This aspect intercepts method calls annotated with @ReadOnlyDataSource
 * or @WriteDataSource and sets the appropriate datasource context.
 * 
 * Order is set to 1 to ensure this aspect runs before transaction aspects.
 * 
 * @author ITT Team
 * @version 1.0
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class DataSourceRoutingAspect {

    /**
     * Around advice for methods annotated with @ReadOnlyDataSource.
     * Sets the datasource context to READ before method execution.
     */
    @Around("@annotation(com.itt.service.annotation.ReadOnlyDataSource)")
    public Object routeToReadDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithDataSource(joinPoint, DataSourceType.READ, "READ");
    }

    /**
     * Around advice for methods annotated with @WriteDataSource.
     * Sets the datasource context to WRITE before method execution.
     */
    @Around("@annotation(com.itt.service.annotation.WriteDataSource)")
    public Object routeToWriteDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithDataSource(joinPoint, DataSourceType.WRITE, "WRITE");
    }

    /**
     * Around advice for class-level @ReadOnlyDataSource annotation.
     * Sets the datasource context to READ for the entire class.
     */
    @Around("@within(com.itt.service.annotation.ReadOnlyDataSource)")
    public Object routeClassToReadDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if method has explicit datasource annotation
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(WriteDataSource.class) || 
            method.isAnnotationPresent(ReadOnlyDataSource.class)) {
            // Method-level annotation takes precedence
            return joinPoint.proceed();
        }
        return executeWithDataSource(joinPoint, DataSourceType.READ, "READ (class-level)");
    }

    /**
     * Around advice for class-level @WriteDataSource annotation.
     * Sets the datasource context to write for the entire class.
     */
    @Around("@within(com.itt.service.annotation.WriteDataSource)")
    public Object routeClassToWriteDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if method has explicit datasource annotation
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(WriteDataSource.class) || 
            method.isAnnotationPresent(ReadOnlyDataSource.class)) {
            // Method-level annotation takes precedence
            return joinPoint.proceed();
        }
        return executeWithDataSource(joinPoint, DataSourceType.WRITE, "WRITE (class-level)");
    }

    /**
     * Executes the method with the specified datasource context.
     * 
     * @param joinPoint the proceeding join point
     * @param dataSourceType the datasource type to use
     * @param operation the operation description for logging
     * @return the method result
     * @throws Throwable if method execution fails
     */
    private Object executeWithDataSource(ProceedingJoinPoint joinPoint, 
                                       DataSourceType dataSourceType, 
                                       String operation) throws Throwable {
        
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + 
                           "." + joinPoint.getSignature().getName();
        
        DataSourceType originalDataSource = DataSourceContextHolder.getDataSourceType();
        
        try {
            // Set the datasource context
            DataSourceContextHolder.setDataSourceType(dataSourceType);
            log.debug("Routed method [{}] to {} datasource", methodName, operation);
            
            // Validate transaction configuration for read-only operations
            validateTransactionConfiguration(joinPoint, dataSourceType);
            
            // Proceed with method execution
            return joinPoint.proceed();
            
        } catch (Exception e) {
            log.error("Error executing method [{}] with {} datasource: {}", 
                     methodName, operation, e.getMessage(), e);
            throw e;
        } finally {
            // Restore original datasource context
            DataSourceContextHolder.setDataSourceType(originalDataSource);
            log.debug("Restored datasource context for method [{}]", methodName);
        }
    }

    /**
     * Validates transaction configuration for read-only operations.
     * Issues warnings if read-only methods are not properly configured.
     */
    private void validateTransactionConfiguration(ProceedingJoinPoint joinPoint, DataSourceType dataSourceType) {
        if (dataSourceType == DataSourceType.READ) {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            
            // Check if method or class is annotated with @Transactional
            Transactional transactional = method.getAnnotation(Transactional.class);
            if (transactional == null) {
                transactional = method.getDeclaringClass().getAnnotation(Transactional.class);
            }
            
            // Warn if read-only method is not marked as readOnly=true in @Transactional
            if (transactional != null && !transactional.readOnly()) {
                log.warn("Method [{}] uses read datasource but @Transactional is not marked as readOnly=true. " +
                        "Consider adding readOnly=true for better performance.", 
                        joinPoint.getSignature().toShortString());
            }
        }
    }
}
