package com.itt.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import com.itt.service.validator.SortFieldValidator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for client-side data table requests.
 * 
 * <p>This DTO standardizes the way client applications (web interfaces, mobile apps)
 * request paginated, sorted, and filtered data from the server. It supports complex
 * data table operations commonly found in administrative interfaces and dashboards.</p>
 * 
 * <h3>Core Functionality:</h3>
 * <ul>
 *   <li><strong>Pagination:</strong> Page-based data loading with configurable page sizes</li>
 *   <li><strong>Sorting:</strong> Multi-column sorting with direction control</li>
 *   <li><strong>Filtering:</strong> Global search and column-specific filtering</li>
 *   <li><strong>Security:</strong> Sort field validation to prevent SQL injection</li>
 * </ul>
 * 
 * <h3>Data Table Integration:</h3>
 * <p>Designed to work seamlessly with popular frontend data table libraries:</p>
 * <ul>
 *   <li><strong>DataTables (jQuery):</strong> Direct parameter mapping</li>
 *   <li><strong>AG-Grid:</strong> Column model and filtering support</li>
 *   <li><strong>PrimeNG Table:</strong> Angular data table integration</li>
 *   <li><strong>React Table:</strong> Modern React table component support</li>
 * </ul>
 * 
 * <h3>Security Features:</h3>
 * <ul>
 *   <li><strong>Sort Field Validation:</strong> Prevents SQL injection through sort parameters</li>
 *   <li><strong>Size Limits:</strong> Prevents resource exhaustion with page size limits</li>
 *   <li><strong>Input Sanitization:</strong> Clean handling of search and filter inputs</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Client-side request (JSON)
 * {
 *   "pagination": {
 *     "page": 0,
 *     "size": 25
 *   },
 *   "searchFilter": {
 *     "searchText": "admin",
 *     "columns": ["name", "description"]
 *   },
 *   "columns": [
 *     {
 *       "columnName": "name",
 *       "sort": "asc"
 *     },
 *     {
 *       "columnName": "createdOn",
 *       "sort": "desc"
 *     }
 *   ]
 * }
 * 
 * // Controller usage
 * @PostMapping("/roles/search")
 * public ResponseEntity<ApiResponse<PaginationResponse<RoleDto>>> searchRoles(
 *         @Valid @RequestBody DataTableRequest request) {
 *     PaginationResponse<RoleDto> response = roleService.findAll(request);
 *     return ResponseBuilder.success(response);
 * }
 * 
 * // Service layer usage
 * public PaginationResponse<RoleDto> findRoles(DataTableRequest request) {
 *     request.setSortFieldValidator(roleSortValidator);
 *     Pageable pageable = request.toPageable();
 *     // ... query execution
 * }
 * }</pre>
 * 
 * <h3>Conversion to Spring Data:</h3>
 * <p>The {@link #toPageable()} method converts client request parameters into
 * Spring Data {@link Pageable} objects for seamless repository integration.</p>
 * 
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li><strong>Page Size:</strong> Between 10 and 50 records to balance performance and usability</li>
 *   <li><strong>Page Number:</strong> Zero-based indexing starting from 0</li>
 *   <li><strong>Column Names:</strong> Must be non-blank and validated against entity fields</li>
 *   <li><strong>Sort Direction:</strong> Must be 'asc' or 'desc' (case-insensitive)</li>
 * </ul>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see PaginationResponse
 * @see SortFieldValidator
 * @see BaseService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTableRequest {
    
    /**
     * Pagination parameters for the request.
     * Defaults to first page with 10 records if not specified.
     */
    @Valid
    private Pagination pagination = new Pagination();
    
    /**
     * Search and filtering parameters.
     * Optional - if not provided, no filtering is applied.
     */
    @Valid
    private SearchFilter searchFilter;
    
    /**
     * Column definitions including sorting and filtering.
     * Used to specify which columns to sort by and in what direction.
     */
    @Valid
    private List<Column> columns = new ArrayList<>();
    
    /**
     * Validator for sort field names to prevent SQL injection.
     * This is set by the service layer before processing the request.
     */
    private transient SortFieldValidator sortFieldValidator;

    /**
     * Constructor with sort field validator injection.
     * 
     * @param sortFieldValidator validator for sort field security
     */
    public DataTableRequest(SortFieldValidator sortFieldValidator) {
        this.sortFieldValidator = sortFieldValidator;
    }

    /**
     * Converts this data table request into a Spring Data Pageable object.
     * 
     * <p>This method transforms the client-side request parameters into a format
     * that can be used directly with Spring Data JPA repositories. It handles:</p>
     * 
     * <ul>
     *   <li><strong>Pagination:</strong> Page number and size conversion</li>
     *   <li><strong>Sorting:</strong> Multi-column sort order with validation</li>
     *   <li><strong>Security:</strong> Sort field validation to prevent injection</li>
     *   <li><strong>Fallback:</strong> Default sorting by 'id' if no valid sorts found</li>
     * </ul>
     * 
     * <h4>Sort Processing Algorithm:</h4>
     * <ol>
     *   <li>Extract columns with sort direction specified</li>
     *   <li>Validate each column name against allowed entity fields</li>
     *   <li>Parse sort direction (asc/desc) with error handling</li>
     *   <li>Build Spring Data Sort.Order objects</li>
     *   <li>Fall back to default 'id' sort if no valid sorts found</li>
     * </ol>
     * 
     * <h4>Security Considerations:</h4>
     * <p>All sort field names are validated through the {@link SortFieldValidator}
     * to ensure they correspond to actual entity fields and prevent SQL injection
     * attacks through malicious sort parameters.</p>
     * 
     * @return Spring Data Pageable object for repository queries
     * @throws IllegalArgumentException if pagination parameters are invalid
     */
    public Pageable toPageable() {
        // Fall back to default sorting if no columns or validator available
        if (ObjectUtils.isEmpty(columns) || ObjectUtils.isEmpty(sortFieldValidator)) {
            return PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by("id"));
        }

        // Process columns with sort specifications
        List<Sort.Order> orders = columns.stream()
                .filter(col -> StringUtils.hasText(col.getSort()))
                .map(col -> {
                    try {
                        // Parse sort direction with error handling
                        Sort.Direction dir = Sort.Direction.fromString(col.getSort());
                        
                        // Validate sort field for security
                        if (sortFieldValidator.isValidSortField(col.getColumnName())) {
                            return new Sort.Order(dir, col.getColumnName());
                        }
                        return null;
                    } catch (IllegalArgumentException e) {
                        // Invalid sort direction - skip this column
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Use validated sorts or fall back to default
        Sort sort = ObjectUtils.isEmpty(orders) ? Sort.by("id") : Sort.by(orders);
        return PageRequest.of(pagination.getPage(), pagination.getSize(), sort);
    }

    /**
     * Pagination parameters for data table requests.
     * 
     * <p>Defines the page-based pagination behavior including page number
     * and the number of records per page. Includes validation to ensure
     * reasonable limits for performance and user experience.</p>
     * 
     * <h4>Design Rationale:</h4>
     * <ul>
     *   <li><strong>Page Size Limits:</strong> 10-50 records balance performance with usability</li>
     *   <li><strong>Zero-Based Indexing:</strong> Consistent with Spring Data conventions</li>
     *   <li><strong>Sensible Defaults:</strong> First page with 10 records for immediate usability</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        
        /**
         * Zero-based page number for the request.
         * 
         * <p>Page numbering starts from 0, following Spring Data conventions.
         * Must be non-negative to prevent invalid page requests.</p>
         * 
         * @minimum 0
         * @default 0
         */
        @Min(0)
        private Integer page = 0;
        
        /**
         * Number of records per page.
         * 
         * <p>Limited to a reasonable range to balance user experience with
         * system performance. Large page sizes can impact query performance
         * and memory usage.</p>
         * 
         * @minimum 10
         * @maximum 50
         * @default 10
         */
        @Min(10)
        @Max(50)
        private Integer size = 10;
    }

    /**
     * Search and filtering parameters for data table requests.
     * 
     * <p>Enables both global search across multiple columns and targeted
     * filtering for more precise data retrieval. Supports full-text search
     * patterns commonly used in administrative interfaces.</p>
     * 
     * <h4>Search Capabilities:</h4>
     * <ul>
     *   <li><strong>Global Search:</strong> Search text applied across multiple columns</li>
     *   <li><strong>Column Selection:</strong> Specify which columns to include in search</li>
     *   <li><strong>Flexible Matching:</strong> Supports partial text matching</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchFilter {
        
        /**
         * Global search text to apply across specified columns.
         * 
         * <p>This text will be used for partial matching against the
         * specified columns. Typically supports case-insensitive matching
         * and may include wildcards depending on implementation.</p>
         */
        private String searchText;
        
        /**
         * List of column names to include in the global search.
         * 
         * <p>Specifies which entity fields should be searched when applying
         * the global search text. Empty list means search all searchable fields.</p>
         */
        private List<String> columns = new ArrayList<>();
    }

    /**
     * Column definition for sorting and filtering in data table requests.
     * 
     * <p>Represents a single column in the data table with its associated
     * sorting and filtering specifications. Each column can have independent
     * sort direction and filter criteria.</p>
     * 
     * <h4>Column Operations:</h4>
     * <ul>
     *   <li><strong>Sorting:</strong> Ascending or descending sort direction</li>
     *   <li><strong>Filtering:</strong> Column-specific filter criteria</li>
     *   <li><strong>Identification:</strong> Column name matching entity fields</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Column {
        
        /**
         * Name of the column corresponding to an entity field.
         * 
         * <p>Must match a valid field name in the target entity. This name
         * is validated through the {@link SortFieldValidator} to ensure
         * security and prevent SQL injection attacks.</p>
         * 
         * <p>Examples: "name", "createdOn", "isActive", "role.name"</p>
         */
        @NotBlank
        private String columnName;
        
        /**
         * Column-specific filter criteria.
         * 
         * <p>Optional filter value to apply to this specific column.
         * The interpretation of this value depends on the column type
         * and implementation (exact match, partial match, range, etc.).</p>
         */
        private String filter;
        
        /**
         * Sort direction for this column.
         * 
         * <p>Valid values are "asc" for ascending or "desc" for descending.
         * Case-insensitive. If not specified, this column is not included
         * in the sort criteria.</p>
         * 
         * <p>Examples: "asc", "desc", "ASC", "DESC"</p>
         */
        private String sort;
    }
}