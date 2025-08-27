package com.itt.service.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that should use the read-only datasource.
 * 
 * This annotation can be applied to:
 * - Service methods that only perform read operations
 * - Repository methods for complex read queries
 * - Controller methods for read-only endpoints
 * 
 * Usage:
 * {@code
 * @ReadOnlyDataSource
 * public List<User> findAllUsers() {
 *     return userRepository.findAll();
 * }
 * }
 * 
 * @author ITT Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnlyDataSource {
    
    /**
     * Optional description of why this method uses read-only datasource.
     * 
     * @return description
     */
    String value() default "";
}
