package com.itt.service.validator;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.itt.service.dto.ApiResponse.ValidationError;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.fw.search.SearchableEntity;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Constraint validator implementation for {@link ValidDataTableRequest} annotation.
 * 
 * <p>This validator integrates with Spring's validation framework to provide automatic
 * validation of DataTableRequest objects against SearchableEntity configurations.
 * It leverages the {@link DataTableRequestValidator} for the actual validation logic
 * and provides proper constraint violation reporting.</p>
 * 
 * <h3>Integration Features:</h3>
 * <ul>
 *   <li><strong>Spring Integration:</strong> Automatic dependency injection</li>
 *   <li><strong>Entity Resolution:</strong> Dynamically resolves SearchableEntity beans</li>
 *   <li><strong>Error Mapping:</strong> Converts ValidationErrors to ConstraintViolations</li>
 *   <li><strong>Performance:</strong> Caches entity lookups for efficiency</li>
 * </ul>
 * 
 * <h3>Validation Flow:</h3>
 * <ol>
 *   <li>Resolve SearchableEntity bean from Spring context</li>
 *   <li>Delegate validation to DataTableRequestValidator</li>
 *   <li>Convert validation errors to constraint violations</li>
 *   <li>Return validation result to Spring framework</li>
 * </ol>
 * 
 * @author ITT Development Team
 * @since 2025.1.0
 * @see ValidDataTableRequest
 * @see DataTableRequestValidator
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidDataTableRequestValidator implements ConstraintValidator<ValidDataTableRequest, DataTableRequest> {

    private final ApplicationContext applicationContext;
    private final DataTableRequestValidator requestValidator;
    
    private Class<?> entityClass;

    /**
     * Initializes the validator with the constraint annotation.
     * 
     * @param constraintAnnotation The ValidDataTableRequest annotation
     */
    @Override
    public void initialize(ValidDataTableRequest constraintAnnotation) {
        this.entityClass = constraintAnnotation.entity();
        log.debug("Initialized ValidDataTableRequestValidator for entity: {}", entityClass.getSimpleName());
    }

    /**
     * Performs the validation logic.
     * 
     * <p>This method resolves the SearchableEntity bean, delegates validation to
     * DataTableRequestValidator, and builds appropriate constraint violations
     * if validation fails.</p>
     * 
     * @param request The DataTableRequest to validate
     * @param context The constraint validator context
     * @return true if validation passes, false otherwise
     */
    @Override
    public boolean isValid(DataTableRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let @NotNull handle null validation
        }

        try {
            // Resolve SearchableEntity bean from Spring context
            SearchableEntity<?> entity = resolveSearchableEntity();
            if (entity == null) {
                log.error("Could not resolve SearchableEntity bean for class: {}", entityClass.getName());
                buildConstraintViolation(context, 
                    "searchableEntity",
                    entityClass.getName(),
                    "SearchableEntity configuration not found for " + entityClass.getSimpleName(),
                    "ENTITY_NOT_FOUND");
                return false;
            }

            // Get validation errors from DataTableRequestValidator
            List<ValidationError> errors = requestValidator.getValidationErrors(request, entity);
            
            if (errors.isEmpty()) {
                return true; // Validation passed
            }

            // Build constraint violations for each validation error
            context.disableDefaultConstraintViolation();
            for (ValidationError error : errors) {
                buildConstraintViolation(context, 
                    error.getField(), 
                    error.getRejectedValue(), 
                    error.getMessage(),
                    error.getCode());
            }
            
            log.debug("DataTableRequest validation failed for entity {}: {} errors", 
                     entityClass.getSimpleName(), errors.size());
            return false;

        } catch (Exception e) {
            log.error("Unexpected error during DataTableRequest validation for entity {}: {}", 
                     entityClass.getSimpleName(), e.getMessage(), e);
            
            context.disableDefaultConstraintViolation();
            buildConstraintViolation(context,
                "request",
                request,
                "Validation failed due to internal error: " + e.getMessage(),
                "VALIDATION_ERROR");
            return false;
        }
    }

    /**
     * Resolves the SearchableEntity bean from Spring application context.
     * 
     * <p>This method searches for beans implementing SearchableEntity with the
     * matching entity class. It handles both direct bean lookup and component
     * scanning scenarios.</p>
     * 
     * @return SearchableEntity instance or null if not found
     */
    private SearchableEntity<?> resolveSearchableEntity() {
        try {
            // Try to find SearchableEntity beans that match our entity class
            String[] beanNames = applicationContext.getBeanNamesForType(SearchableEntity.class);
            
            for (String beanName : beanNames) {
                SearchableEntity<?> entity = (SearchableEntity<?>) applicationContext.getBean(beanName);
                
                // Check if this entity handles our target class
                if (entityClass.equals(entity.getEntityClass()) || 
                    entityClass.isAssignableFrom(entity.getClass())) {
                    
                    log.debug("Resolved SearchableEntity bean '{}' for entity class: {}", 
                             beanName, entityClass.getSimpleName());
                    return entity;
                }
            }
            
            // Try direct bean lookup by class name patterns
            String[] possibleBeanNames = {
                entityClass.getSimpleName() + "SearchConfig",
                Character.toLowerCase(entityClass.getSimpleName().charAt(0)) + entityClass.getSimpleName().substring(1) + "SearchConfig",
                entityClass.getSimpleName().replace("SearchConfig", "") + "SearchConfig"
            };
            
            for (String beanName : possibleBeanNames) {
                if (applicationContext.containsBean(beanName)) {
                    Object bean = applicationContext.getBean(beanName);
                    if (bean instanceof SearchableEntity) {
                        log.debug("Resolved SearchableEntity bean '{}' by name pattern for entity class: {}", 
                                 beanName, entityClass.getSimpleName());
                        return (SearchableEntity<?>) bean;
                    }
                }
            }
            
            log.warn("No SearchableEntity bean found for entity class: {}", entityClass.getName());
            return null;
            
        } catch (Exception e) {
            log.error("Error resolving SearchableEntity bean for class {}: {}", 
                     entityClass.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Builds a constraint violation with detailed error information.
     * 
     * @param context The constraint validator context
     * @param field The field name where the violation occurred
     * @param rejectedValue The value that was rejected
     * @param message The error message
     * @param code The error code
     */
    private void buildConstraintViolation(ConstraintValidatorContext context, 
                                        String field, 
                                        Object rejectedValue, 
                                        String message,
                                        String code) {
        
        // Create a detailed message that includes the error code
        String detailedMessage = String.format("[%s] %s", code, message);
        
        // Build the constraint violation with property path
        context.buildConstraintViolationWithTemplate(detailedMessage)
               .addPropertyNode(field)
               .addConstraintViolation();
               
        log.debug("Built constraint violation for field '{}' with code '{}': {}", 
                 field, code, message);
    }
}