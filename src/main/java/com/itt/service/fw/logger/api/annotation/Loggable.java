package com.itt.service.fw.logger.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
    /**
     * If true, logging will propagate to all methods invoked within the call chain
     * until the top-level method with propagate=true returns.
     * If false or not specified, logging is only for the annotated method.
     */
    boolean propagate() default false;
}