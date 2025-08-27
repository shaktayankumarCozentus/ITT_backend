package com.itt.service.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validation annotation for DataTableRequest objects that enforces strict field validation.
 * 
 * <p>This constraint validates that all filter fields, sort fields, and search columns
 * specified in a DataTableRequest are valid according to a specific SearchableEntity
 * configuration. It ensures mandatory DTO field name compliance and prevents SQL
 * injection through field whitelisting.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Automatic Validation:</strong> Integrates with Spring's validation framework</li>
 *   <li><strong>Entity-Specific:</strong> Validates against specific SearchableEntity configuration</li>
 *   <li><strong>Detailed Errors:</strong> Provides field-level validation error details</li>
 *   <li><strong>DTO Field Enforcement:</strong> Ensures mandatory DTO field name usage</li>
 *   <li><strong>Security:</strong> Prevents unauthorized field access through whitelisting</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Controller method parameter validation
 * @PostMapping("/users/search")
 * public ApiResponse<PageResponse<UserDto>> searchUsers(
 *     @Valid @ValidDataTableRequest(entity = UserSearchConfig.class)
 *     @RequestBody DataTableRequest request) {
 *     return userService.searchUsers(request);
 * }
 * 
 * // Request DTO field validation
 * @Data
 * public class SearchUsersRequest {
 *     @ValidDataTableRequest(entity = UserSearchConfig.class)
 *     private DataTableRequest dataTableRequest;
 *     
 *     private String additionalFilter;
 * }
 * 
 * // Service method validation
 * public ApiResponse<PageResponse<RoleDto>> searchRoles(
 *     @ValidDataTableRequest(entity = RoleSearchConfig.class) DataTableRequest request) {
 *     // Method implementation
 * }
 * }</pre>
 * 
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li><strong>Filter Fields:</strong> Must be in entity's searchableFields set</li>
 *   <li><strong>Sort Fields:</strong> Must be in entity's sortableFields set</li>
 *   <li><strong>Search Columns:</strong> Must be in entity's searchableFields set</li>
 *   <li><strong>Field Names:</strong> Must use DTO field names (resolved through fieldAliases)</li>
 * </ul>
 * 
 * <h3>Error Response Format:</h3>
 * <pre>{@code
 * {
 *   "timestamp": "2025-08-25T10:30:00",
 *   "status": 400,
 *   "error": "VALIDATION_ERROR",
 *   "message": "Invalid fields detected in request for UserSearchConfig",
 *   "validationErrors": [
 *     {
 *       "field": "columns[0].columnName",
 *       "rejectedValue": "invalidField",
 *       "message": "Invalid filter field 'invalidField'. Valid fields: [userId, name, email]",
 *       "code": "INVALID_FILTER_FIELD"
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * @author ITT Development Team
 * @since 2025.1.0
 * @see DataTableRequestValidator
 * @see com.itt.service.fw.search.SearchableEntity
 * @see com.itt.service.dto.DataTableRequest
 */
@Documented
@Constraint(validatedBy = ValidDataTableRequestValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDataTableRequest {
    
    /**
     * The SearchableEntity class to validate against.
     * 
     * <p>This must be a class that implements SearchableEntity interface and is
     * registered as a Spring component. The validator will use this entity's
     * configuration to validate field names.</p>
     * 
     * @return SearchableEntity class for validation
     */
    Class<?> entity();
    
    /**
     * Validation error message.
     * 
     * @return error message template
     */
    String message() default "Invalid DataTableRequest fields";
    
    /**
     * Validation groups.
     * 
     * @return validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload for extensibility.
     * 
     * @return payload classes
     */
    Class<? extends Payload>[] payload() default {};
}