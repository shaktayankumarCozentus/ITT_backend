package com.itt.service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom constraint annotation for validating SaveRoleRequest business rules.
 * <p>
 * This class-level constraint validates complex business rules that span
 * multiple fields of the SaveRoleRequest DTO, including:
 * <ul>
 *   <li>Conditional field requirements (e.g., customLanding → landingPageConfigId)</li>
 *   <li>Database referential integrity checks</li>
 *   <li>Cross-field business rule validation</li>
 *   <li>Role type and skin configuration compatibility</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * {@code
 * @SaveRoleRequestConstraint
 * public class SaveRoleRequest {
 *     // ... fields
 * }
 * }
 * </pre>
 * 
 * @see com.itt.service.validator.SaveRoleRequestValidator
 * @see com.itt.service.dto.role.SaveRoleRequest
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = SaveRoleRequestValidator.class)
public @interface SaveRoleRequestConstraint {
    
    /**
     * The default error message when validation fails.
     * Individual field violations will have more specific messages.
     */
    String message() default "SaveRoleRequest validation failed";
    
    /**
     * Validation groups for conditional validation scenarios.
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload for additional constraint metadata.
     */
    Class<? extends Payload>[] payload() default {};
}
