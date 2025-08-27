package com.itt.service.service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itt.service.annotation.ReadOnlyDataSource;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.fw.search.DynamicSearchQueryBuilder;
import com.itt.service.fw.search.SearchableEntity;
import com.itt.service.fw.search.UniversalSortFieldValidator;
import com.itt.service.repository.BaseRepository;
import com.itt.service.validator.SortFieldValidator;

import lombok.RequiredArgsConstructor;

/**
 * Abstract base service class for Service entities.
 * 
 * <p>This class provides a standardized foundation for service layer implementations,
 * offering common patterns for data access, pagination, filtering, and entity-to-DTO
 * mapping. It implements the Template Method pattern to ensure consistency across
 * different service implementations while allowing customization for specific needs.</p>
 * 
 * <h3>Service Layer Architecture:</h3>
 * <ul>
 *   <li><strong>Data Access:</strong> Unified repository access patterns</li>
 *   <li><strong>Pagination:</strong> Standardized pagination and sorting</li>
 *   <li><strong>Filtering:</strong> Dynamic query building with specifications</li>
 *   <li><strong>Mapping:</strong> Consistent entity-to-DTO conversion</li>
 *   <li><strong>Validation:</strong> Integrated field validation for sorting and filtering</li>
 * </ul>
 * 
 * <h3>Generic Type Parameters:</h3>
 * <ul>
 *   <li><strong>T:</strong> The entity type (e.g., Role, MasterConfig)</li>
 *   <li><strong>ID:</strong> The entity's identifier type (e.g., Integer, Long)</li>
 *   <li><strong>DTO:</strong> The data transfer object type for client communication</li>
 * </ul>
 * 
 * <h3>Core Features:</h3>
 * <ul>
 *   <li><strong>Paginated Queries:</strong> Efficient data retrieval with pagination support</li>
 *   <li><strong>Dynamic Filtering:</strong> Runtime query composition based on client requests</li>
 *   <li><strong>Field Validation:</strong> Sort field validation to prevent SQL injection</li>
 *   <li><strong>Specification Support:</strong> Type-safe query building using JPA Criteria API</li>
 *   <li><strong>Automatic Mapping:</strong> Seamless entity-to-DTO conversion</li>
 * </ul>
 * 
 * <h3>Data Table Integration:</h3>
 * <p>The service is designed to work seamlessly with client-side data tables:</p>
 * <ul>
 *   <li><strong>Sorting:</strong> Multi-column sorting with field validation</li>
 *   <li><strong>Filtering:</strong> Global and column-specific filtering</li>
 *   <li><strong>Pagination:</strong> Page-based data loading with size controls</li>
 *   <li><strong>Search:</strong> Full-text search capabilities across entity fields</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Concrete service implementation
 * @Service
 * public class RoleService extends BaseService<Role, Integer, RoleDto> {
 *     
 *     public RoleService(RoleRepository repository, 
 *                       SortFieldValidator validator,
 *                       Function<Role, RoleDto> mapper) {
 *         super(repository, validator, mapper);
 *     }
 *     
 *     // Additional business methods can be added here
 *     public RoleDto createRole(CreateRoleRequest request) {
 *         Role role = convertToEntity(request);
 *         Role saved = repository.save(role);
 *         return mapper.apply(saved);
 *     }
 * }
 * 
 * // Controller usage
 * @RestController
 * public class RoleController {
 *     private final RoleService roleService;
 *     
 *     @PostMapping("/roles/search")
 *     public ResponseEntity<ApiResponse<PaginationResponse<RoleDto>>> searchRoles(
 *             @RequestBody DataTableRequest request) {
 *         PaginationResponse<RoleDto> response = roleService.findAll(request);
 *         return ResponseEntity.ok(ApiResponse.success(response));
 *     }
 * }
 * }</pre>
 * 
 * <h3>Inheritance Hierarchy:</h3>
 * <pre>
 * BaseService&lt;T, ID, DTO&gt;
 *   ├── RoleManagementService
 *   ├── CustomerSubscriptionService
 *   ├── UserService
 *   └── Other domain services...
 * </pre>
 * 
 * <h3>Dependency Requirements:</h3>
 * <ul>
 *   <li><strong>Repository:</strong> Entity-specific repository extending BaseRepository</li>
 *   <li><strong>Validator:</strong> Sort field validator for security and data integrity</li>
 *   <li><strong>Mapper:</strong> Function to convert entities to DTOs</li>
 * </ul>
 * 
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li><strong>Validation:</strong> Always use sort field validators to prevent injection attacks</li>
 *   <li><strong>Mapping:</strong> Keep entity-to-DTO mapping logic separate and testable</li>
 *   <li><strong>Performance:</strong> Consider fetch strategies and projection queries for large datasets</li>
 *   <li><strong>Security:</strong> Implement proper access controls in concrete service methods</li>
 * </ul>
 * 
 * @param <T> the entity type managed by this service
 * @param <ID> the type of the entity's identifier
 * @param <DTO> the data transfer object type returned to clients
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see BaseRepository
 * @see DataTableRequest
 * @see PaginationResponse
 * @see SearchableEntity
 * @see DynamicSearchQueryBuilder
 */
@Service
@RequiredArgsConstructor
public abstract class BaseService<T, ID, DTO> {
    
    /**
     * Repository for data access operations.
     * Provides CRUD operations and specification-based queries.
     */
    protected final BaseRepository<T, ID> repository;
    
    /**
     * Validator for sort field names to prevent SQL injection.
     * Ensures only valid entity fields are used for sorting operations.
     */
    protected final SortFieldValidator sortFieldValidator;
    
    /**
     * Function to map entities to DTOs.
     * Enables consistent entity-to-DTO conversion across all service operations.
     */
    protected final Function<T, DTO> mapper;

    // ==========================================
    // UNIVERSAL SEARCH FRAMEWORK INTEGRATION
    // ==========================================
    
    @Autowired
    private DynamicSearchQueryBuilder queryBuilder;

    @Autowired
    @Qualifier("universalSortFieldValidator")
    private UniversalSortFieldValidator universalSortValidator;

    /**
     * Override this method in concrete services to enable Universal Search Framework.
     * 
     * @return SearchableEntity configuration for this entity
     */
    protected SearchableEntity<T> getSearchableEntity() {
        return null; // Override this method to enable Universal Search Framework
    }

    /**
     * Universal search method that automatically handles search/sort/pagination.
     * Uses the Universal Search Framework if getSearchableEntity() is implemented.
     * 
     * @param request DataTable request with search/sort/pagination parameters
     * @return Paginated response with search results
     */
    @ReadOnlyDataSource("Universal search with automatic query generation")
    @Transactional(readOnly = true, timeout = 30)
    public PaginationResponse<DTO> search(DataTableRequest request) {
        SearchableEntity<T> searchableEntity = getSearchableEntity();
        
        if (searchableEntity != null) {
            // Use Universal Search Framework
            return searchWithFramework(request, searchableEntity);
        } else {
            // Framework not enabled - require SearchableEntity implementation
            throw new IllegalStateException(
                "Universal Search Framework not enabled. Override getSearchableEntity() method in " + 
                this.getClass().getSimpleName() + " to provide SearchableEntity configuration."
            );
        }
    }
    
    /**
     * Internal method for Universal Search Framework execution
     */
    private PaginationResponse<DTO> searchWithFramework(DataTableRequest request, SearchableEntity<T> searchableEntity) {
        // SECURITY: Validate request fields before processing
        if (request == null) {
            throw new IllegalArgumentException("DataTableRequest cannot be null");
        }
        
        // Register entity for sort validation
        universalSortValidator.registerEntity(searchableEntity);
        
        // Set entity-specific sort validator for security
        request.setSortFieldValidator(new EntitySpecificSortValidator<>(searchableEntity, universalSortValidator));
        
        // Execute dynamic search (includes validation)
        Page<T> page = queryBuilder.findWithDynamicSearch(searchableEntity, request);
        
        // Convert entities to DTOs
        Page<DTO> dtoPage = page.map(mapper);
        
        // Convert to response format
        return new PaginationResponse<>(dtoPage);
    }

    /**
     * Entity-specific sort validator that only validates fields for this entity
     */
    private static class EntitySpecificSortValidator<T> implements SortFieldValidator {
        
        private final SearchableEntity<T> entity;
        private final UniversalSortFieldValidator universalValidator;
        
        public EntitySpecificSortValidator(SearchableEntity<T> entity, UniversalSortFieldValidator universalValidator) {
            this.entity = entity;
            this.universalValidator = universalValidator;
        }
        
        @Override
        public boolean isValidSortField(String field) {
            return universalValidator.isValidSortField(field, entity.getEntityClass());
        }
        
        @Override
        public java.util.Set<String> getValidSortFields() {
            return entity.getSortableFields();
        }
    }

    /**
     * Retrieves paginated and filtered data based on client request parameters.
     * 
     * <p>This method serves as the primary data retrieval operation for data tables
     * and list views. It combines pagination, sorting, filtering, and mapping into
     * a single, efficient operation.</p>
     * 
     * <h4>Processing Flow:</h4>
     * <ol>
     *   <li><strong>Validation:</strong> Validates sort fields against allowed entity fields</li>
     *   <li><strong>Pagination:</strong> Converts request parameters to Spring Data Pageable</li>
     *   <li><strong>Specification:</strong> Builds dynamic query specification from filters</li>
     *   <li><strong>Execution:</strong> Executes paginated query against repository</li>
     *   <li><strong>Mapping:</strong> Converts entity results to DTO objects</li>
     *   <li><strong>Response:</strong> Wraps results in pagination response structure</li>
     * </ol>
     * 
     * <h4>Request Parameters:</h4>
     * <ul>
     *   <li><strong>Pagination:</strong> page, size parameters for result paging</li>
     *   <li><strong>Sorting:</strong> sort field and direction specifications</li>
     *   <li><strong>Filtering:</strong> global search and column-specific filters</li>
     *   <li><strong>Search:</strong> full-text search across specified entity fields</li>
     * </ul>
     * 
     * <h4>Security Features:</h4>
     * <ul>
     *   <li><strong>Field Validation:</strong> Prevents injection through sort field validation</li>
     *   <li><strong>Safe Filtering:</strong> Uses parameterized queries via JPA Specifications</li>
     *   <li><strong>Access Control:</strong> Can be extended for entity-level security</li>
     * </ul>
     * 
     * @param request the data table request containing pagination, sorting, and filtering parameters
     * @return paginated response containing the requested data and metadata
     * @throws IllegalArgumentException if sort fields are invalid or request parameters are malformed
     */
    // ==========================================
    // READ OPERATIONS - Use Read DataSource
    // ==========================================
    
    /**
     * Find all entities without pagination.
     * Uses read datasource for optimal performance.
     * 
     * @return List of all DTOs
     */
    @ReadOnlyDataSource("Simple findAll query - optimized for read replica")
    @Transactional(readOnly = true, timeout = 30)
	public List<DTO> findAll() {
		List<T> result = repository.findAll();
		return result.stream().map(mapper).collect(Collectors.toList());
	}
	
    /**
     * Find entity by ID.
     * Uses read datasource for optimal performance.
     * 
     * @param id Entity identifier
     * @return DTO if found, null otherwise
     */
    @ReadOnlyDataSource("Single entity lookup by ID")
    @Transactional(readOnly = true)
    public DTO findById(ID id) {
        return repository.findById(id)
                .map(mapper)
                .orElse(null);
    }
    
    /**
     * Check if entity exists by ID.
     * Uses read datasource for optimal performance.
     * 
     * @param id Entity identifier
     * @return true if entity exists
     */
    @ReadOnlyDataSource("Existence check by ID")
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }
    
    /**
     * Count total number of entities.
     * Uses read datasource for optimal performance.
     * 
     * @return Total count
     */
    @ReadOnlyDataSource("Count query for all entities")
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    // ==========================================
    // WRITE OPERATIONS - Use Write DataSource (Default)
    // ==========================================
    
    /**
     * Create or update an entity.
     * Uses write datasource (default) for data modification.
     * 
     * Note: This method should be overridden in concrete implementations
     * to provide proper DTO to Entity conversion logic.
     * 
     * @param dto DTO to save
     * @return Saved DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public DTO save(DTO dto) {
        throw new UnsupportedOperationException(
            "save() method must be implemented in concrete service classes. " +
            "Implement DTO to Entity conversion logic in your service.");
    }
    
    /**
     * Delete entity by ID.
     * Uses write datasource (default) for data modification.
     * 
     * @param id Entity identifier
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ID id) {
        repository.deleteById(id);
    }
    
    /**
     * Delete multiple entities by IDs.
     * Uses write datasource (default) for data modification.
     * 
     * @param ids List of entity identifiers
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllById(Iterable<ID> ids) {
        repository.deleteAllById(ids);
    }
}
