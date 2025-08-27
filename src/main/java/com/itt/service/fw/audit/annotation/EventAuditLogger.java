package com.itt.service.fw.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventAuditLogger {

    /**
     * Whether to log the request body or not.
     */
    boolean logRequest() default true;

    /**
     * Whether to log the response body or not.
     */
    boolean logResponse() default true;

    /**
     * Whether to log error details if an exception occurs.
     */
    boolean logError() default true;

    /**
     * Mask sensitive fields in request/response bodies.
     * E.g., {"password", "ssn"}.
     */
    String[] maskFields() default {};

    /**
     * If true, skip logging for successful executions.
     * Only errors will be logged.
     */
    boolean onlyOnError() default false;
}


