package com.itt.service.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.itt.service.dto.ApiResponse.ValidationError;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.exception.ValidationException;
import com.itt.service.fw.search.SearchableEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Comprehensive validator for DataTableRequest objects that enforces strict field validation.
 * 
 * <p>This validator ensures that all filter fields, sort fields, and search columns specified 
 * in a DataTableRequest are valid according to the target SearchableEntity configuration.
 * Unlike the previous implementation that silently skipped invalid fields, this validator
 * throws ValidationException with detailed field-level errors.</p>
 * 
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li><strong>Filter Fields:</strong> Must be in entity's getSearchableFields() set</li>
 *   <li><strong>Sort Fields:</strong> Must be in entity's getSortableFields() set</li>
 *   <li><strong>Search Columns:</strong> Must be in entity's getSearchableFields() set</li>
 *   <li><strong>DTO Field Names:</strong> All fields must use DTO field names (mandatory standard)</li>
 *   <li><strong>Security:</strong> Prevents SQL injection through field whitelisting</li>
 * </ul>
 * 
 * <h3>Error Format:</h3>
 * <pre>{@code
 * {
 *   "timestamp": "2025-08-25T10:30:00",
 *   "status": 400,
 *   "error": "VALIDATION_ERROR",
 *   "message": "Request validation failed",
 *   "validationErrors": [
 *     {
 *       "field": "columns[0].columnName",
 *       "rejectedValue": "invalidField",
 *       "message": "Invalid filter field 'invalidField'. Valid fields: [id, name, email, userType]",
 *       "code": "INVALID_FILTER_FIELD"
 *     },
 *     {
 *       "field": "columns[1].columnName", 
 *       "rejectedValue": "password",
 *       "message": "Invalid sort field 'password'. Valid fields: [id, name, email, createdOn]",
 *       "code": "INVALID_SORT_FIELD"
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Service layer usage
 * @Service
 * public class UserManagementServiceImpl {
 *     private final DataTableRequestValidator validator;
 *     
 *     public ApiResponse<PageResponse<UserDto>> searchUsers(DataTableRequest request) {
 *         // Validate request against UserSearchConfig
 *         validator.validateRequest(request, userSearchConfig);
 *         
 *         // Proceed with search - all fields are guaranteed valid
 *         return dynamicSearchQueryBuilder.search(userSearchConfig, request);
 *     }
 * }
 * 
 * // Controller layer with annotation (requires ValidDataTableRequest constraint)
 * @PostMapping("/search")
 * public ApiResponse<PageResponse<UserDto>> searchUsers(
 *     @Valid @ValidDataTableRequest(entity = UserSearchConfig.class) 
 *     @RequestBody DataTableRequest request) {
 *     return userService.searchUsers(request);
 * }
 * }</pre>
 * 
 * @author ITT Development Team
 * @since 2025.1.0
 * @see SearchableEntity
 * @see ValidationException
 * @see DataTableRequest
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataTableRequestValidator {

    /**
     * Validates a DataTableRequest against a SearchableEntity configuration.
     * 
     * <p>Performs comprehensive validation of all request fields including filters,
     * sorting, and search columns. Throws ValidationException with detailed field-level
     * errors if any validation failures are detected.</p>
     * 
     * @param <T> Entity type
     * @param request The DataTableRequest to validate
     * @param entity The SearchableEntity configuration to validate against
     * @throws ValidationException if any validation failures are detected
     * @throws IllegalArgumentException if request or entity is null
     */
    public <T> void validateRequest(DataTableRequest request, SearchableEntity<T> entity) {
        if (request == null) {
            throw new IllegalArgumentException("DataTableRequest cannot be null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("SearchableEntity cannot be null");
        }

        List<ValidationError> errors = new ArrayList<>();
        
        // Validate column filters and sorting
        validateColumns(request.getColumns(), entity, errors);
        
        // Validate search filter columns
        validateSearchFilter(request.getSearchFilter(), entity, errors);
        
        // Throw ValidationException if any errors found
        if (!errors.isEmpty()) {
            String entityName = entity.getEntityClass().getSimpleName();
            String message = String.format("Invalid fields detected in request for %s. Use only DTO field names.", entityName);
            
            log.warn("DataTableRequest validation failed for entity {}: {} errors detected", 
                    entityName, errors.size());
            
            throw new ValidationException(message, errors);
        }
        
        log.debug("DataTableRequest validation passed for entity {}", entity.getEntityClass().getSimpleName());
    }

    /**
     * Validates column specifications including filters and sorting.
     * 
     * @param <T> Entity type
     * @param columns List of column specifications to validate
     * @param entity SearchableEntity configuration
     * @param errors List to collect validation errors
     */
    private <T> void validateColumns(List<DataTableRequest.Column> columns, SearchableEntity<T> entity, List<ValidationError> errors) {
        if (columns == null || columns.isEmpty()) {
            return;
        }

        Set<String> searchableFields = entity.getSearchableFields();
        Set<String> sortableFields = entity.getSortableFields();
        
        for (int i = 0; i < columns.size(); i++) {
            DataTableRequest.Column column = columns.get(i);
            if (column == null || column.getColumnName() == null) {
                continue;
            }

            String fieldName = column.getColumnName();
            String actualField = entity.getFieldAliases().getOrDefault(fieldName, fieldName);
            
            // Validate filter field
            if (column.getFilter() != null && !column.getFilter().trim().isEmpty()) {
                if (!searchableFields.contains(actualField)) {
                    errors.add(createValidationError(
                        String.format("columns[%d].columnName", i),
                        fieldName,
                        String.format("Invalid filter field '%s'. Valid searchable fields: %s", 
                                     fieldName, searchableFields),
                        "INVALID_FILTER_FIELD"
                    ));
                }
            }
            
            // Validate sort field
            if (column.getSort() != null && !column.getSort().trim().isEmpty()) {
                if (!sortableFields.contains(actualField)) {
                    errors.add(createValidationError(
                        String.format("columns[%d].columnName", i),
                        fieldName,
                        String.format("Invalid sort field '%s'. Valid sortable fields: %s", 
                                     fieldName, sortableFields),
                        "INVALID_SORT_FIELD"
                    ));
                }
            }
        }
    }

    /**
     * Validates search filter column specifications.
     * 
     * @param <T> Entity type
     * @param searchFilter SearchFilter to validate
     * @param entity SearchableEntity configuration
     * @param errors List to collect validation errors
     */
    private <T> void validateSearchFilter(DataTableRequest.SearchFilter searchFilter, SearchableEntity<T> entity, List<ValidationError> errors) {
        if (searchFilter == null || searchFilter.getColumns() == null || searchFilter.getColumns().isEmpty()) {
            return;
        }

        Set<String> searchableFields = entity.getSearchableFields();
        List<String> searchColumns = searchFilter.getColumns();
        
        for (int i = 0; i < searchColumns.size(); i++) {
            String fieldName = searchColumns.get(i);
            if (fieldName == null || fieldName.trim().isEmpty()) {
                continue;
            }

            String actualField = entity.getFieldAliases().getOrDefault(fieldName, fieldName);
            
            if (!searchableFields.contains(actualField)) {
                errors.add(createValidationError(
                    String.format("searchFilter.columns[%d]", i),
                    fieldName,
                    String.format("Invalid search column '%s'. Valid searchable fields: %s", 
                                 fieldName, searchableFields),
                    "INVALID_SEARCH_COLUMN"
                ));
            }
        }
    }

    /**
     * Creates a ValidationError with standardized format.
     * 
     * @param field Field path for the error
     * @param rejectedValue The invalid value
     * @param message Human-readable error message
     * @param code Error code for client handling
     * @return ValidationError object
     */
    private ValidationError createValidationError(String field, Object rejectedValue, String message, String code) {
        return ValidationError.builder()
                .field(field)
                .rejectedValue(rejectedValue)
                .message(message)
                .code(code)
                .build();
    }

    /**
     * Validates and sanitizes a field name against entity configuration.
     * 
     * <p>This method can be used for individual field validation outside the context
     * of a full DataTableRequest validation.</p>
     * 
     * @param <T> Entity type
     * @param fieldName The field name to validate
     * @param entity SearchableEntity configuration
     * @param context Context for error messages (e.g., "filter", "sort", "search")
     * @return The actual field name after alias resolution
     * @throws ValidationException if field is invalid
     */
    public <T> String validateField(String fieldName, SearchableEntity<T> entity, String context) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new ValidationException(
                String.format("Field name cannot be null or empty for %s operation", context),
                List.of(createValidationError(
                    context + "Field",
                    fieldName,
                    String.format("Field name cannot be null or empty for %s operation", context),
                    "FIELD_NAME_REQUIRED"
                ))
            );
        }

        String actualField = entity.getFieldAliases().getOrDefault(fieldName, fieldName);
        Set<String> validFields = "sort".equals(context) ? entity.getSortableFields() : entity.getSearchableFields();
        
        if (!validFields.contains(actualField)) {
            throw new ValidationException(
                String.format("Invalid %s field '%s'", context, fieldName),
                List.of(createValidationError(
                    context + "Field",
                    fieldName,
                    String.format("Invalid %s field '%s'. Valid fields: %s", context, fieldName, validFields),
                    "INVALID_" + context.toUpperCase() + "_FIELD"
                ))
            );
        }

        return actualField;
    }

    /**
     * Quick validation check without throwing exceptions.
     * 
     * <p>Returns true if the request would pass validation, false otherwise.
     * Useful for conditional logic where you want to check validity without
     * handling exceptions.</p>
     * 
     * @param <T> Entity type
     * @param request DataTableRequest to check
     * @param entity SearchableEntity configuration
     * @return true if request is valid, false otherwise
     */
    public <T> boolean isValidRequest(DataTableRequest request, SearchableEntity<T> entity) {
        try {
            validateRequest(request, entity);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /**
     * Gets detailed validation results without throwing exceptions.
     * 
     * <p>Returns a list of validation errors that would be thrown by validateRequest().
     * Useful for scenarios where you want to examine all validation issues
     * programmatically.</p>
     * 
     * @param <T> Entity type
     * @param request DataTableRequest to validate
     * @param entity SearchableEntity configuration
     * @return List of validation errors (empty if valid)
     */
    public <T> List<ValidationError> getValidationErrors(DataTableRequest request, SearchableEntity<T> entity) {
        if (request == null || entity == null) {
            return List.of(createValidationError(
                "request",
                null,
                "Request and entity cannot be null",
                "NULL_PARAMETER"
            ));
        }

        List<ValidationError> errors = new ArrayList<>();
        validateColumns(request.getColumns(), entity, errors);
        validateSearchFilter(request.getSearchFilter(), entity, errors);
        return errors;
    }
}