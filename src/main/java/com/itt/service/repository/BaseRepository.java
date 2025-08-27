package com.itt.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for Service entities.
 * 
 * <p>This interface serves as the foundation for all repository interfaces in the
 * application, providing a consistent set of data access capabilities. It combines
 * Spring Data JPA's standard CRUD operations with dynamic query capabilities through
 * JPA Specifications.</p>
 * 
 * <h3>Repository Architecture:</h3>
 * <ul>
 *   <li><strong>Standard CRUD:</strong> Via {@link JpaRepository} - save, find, delete operations</li>
 *   <li><strong>Dynamic Queries:</strong> Via {@link JpaSpecificationExecutor} - complex, type-safe queries</li>
 *   <li><strong>Pagination:</strong> Built-in support for paginated and sorted results</li>
 *   <li><strong>Batch Operations:</strong> Bulk save and delete operations</li>
 * </ul>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Type Safety:</strong> Generic type parameters ensure compile-time type checking</li>
 *   <li><strong>Specification Support:</strong> Enables complex query composition using JPA Criteria API</li>
 *   <li><strong>Transaction Management:</strong> Automatic transaction handling for all operations</li>
 *   <li><strong>Lazy Loading:</strong> Supports JPA's lazy loading patterns</li>
 * </ul>
 * 
 * <h3>Extended Capabilities:</h3>
 * <p>Through {@link JpaSpecificationExecutor}, this interface provides:</p>
 * <ul>
 *   <li><strong>Dynamic Filtering:</strong> Runtime query composition based on user input</li>
 *   <li><strong>Complex Joins:</strong> Multi-entity queries with type safety</li>
 *   <li><strong>Sorting and Pagination:</strong> Flexible result ordering and pagination</li>
 *   <li><strong>Aggregations:</strong> Count queries and existence checks</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Standard repository interface
 * public interface RoleRepository extends BaseRepository<Role, Integer> {
 *     // Custom query methods can be added here
 *     List<Role> findByIsActive(Integer isActive);
 *     
 *     @Query("SELECT r FROM Role r WHERE r.name = :name")
 *     Optional<Role> findByName(@Param("name") String name);
 * }
 * 
 * // Using in service layer
 * @Service
 * public class RoleService {
 *     private final RoleRepository roleRepository;
 *     
 *     // Standard CRUD operations
 *     public Role saveRole(Role role) {
 *         return roleRepository.save(role);
 *     }
 *     
 *     // Specification-based query
 *     public Page<Role> findRoles(Specification<Role> spec, Pageable pageable) {
 *         return roleRepository.findAll(spec, pageable);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Inheritance Hierarchy:</h3>
 * <pre>
 * BaseRepository&lt;T, ID&gt;
 *   ├── RoleRepository
 *   ├── MasterConfigRepository
 *   ├── MasterCompanyRepository
 *   └── Other entity repositories...
 * </pre>
 * 
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li><strong>Naming:</strong> Use clear, descriptive method names following Spring Data conventions</li>
 *   <li><strong>Performance:</strong> Consider fetch strategies and query optimization for complex operations</li>
 *   <li><strong>Transactions:</strong> Use {@code @Transactional} appropriately in service layer</li>
 *   <li><strong>Testing:</strong> Leverage {@code @DataJpaTest} for repository layer testing</li>
 * </ul>
 * 
 * @param <T> the entity type managed by this repository
 * @param <ID> the type of the entity's identifier
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see JpaRepository
 * @see JpaSpecificationExecutor
 * @see HybridRepository
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    // This interface provides all necessary CRUD and Specification operations
    // through its parent interfaces. Concrete repositories can add custom
    // query methods as needed for specific entity requirements.
}