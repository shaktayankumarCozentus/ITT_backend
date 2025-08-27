package com.itt.service.annotation;

import java.lang.annotation.*;

/**
 * Annotation to explicitly mark methods that should use the write datasource.
 * 
 * This annotation can be applied to:
 * - Service methods that perform write operations (INSERT, UPDATE, DELETE)
 * - Repository methods for complex write queries
 * - Controller methods for write operations
 * 
 * Note: Write datasource is the default, so this annotation is optional
 * but useful for explicit documentation.
 * 
 * Usage:
 * {@code
 * @WriteDataSource
 * public User createUser(User user) {
 *     return userRepository.save(user);
 * }
 * }
 * 
 * @author ITT Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WriteDataSource {
    
    /**
     * Optional description of why this method uses write datasource.
     * 
     * @return description
     */
    String value() default "";
}
