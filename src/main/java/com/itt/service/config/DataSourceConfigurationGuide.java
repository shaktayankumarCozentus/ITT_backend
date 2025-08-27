package com.itt.service.config;

/**
 * Configuration Guide for Read/Write DataSource Separation
 * 
 * This class provides documentation and examples for using the read/write 
 * datasource configuration in your services and repositories.
 * 
 * <h2>Quick Start Guide:</h2>
 * 
 * <h3>1. Service Layer Annotations</h3>
 * <pre>
 * {@code
 * @Service
 * public class UserService {
 * 
 *     // Read operations - use @ReadOnlyDataSource
 *     @ReadOnlyDataSource
 *     @Transactional(readOnly = true)
 *     public List<User> getAllUsers() {
 *         return userRepository.findAll();
 *     }
 * 
 *     // Write operations - use @WriteDataSource (optional since it's default)
 *     @WriteDataSource
 *     @Transactional
 *     public User createUser(User user) {
 *         return userRepository.save(user);
 *     }
 * 
 *     // Complex read operations
 *     @ReadOnlyDataSource("Complex search query optimized for read replica")
 *     @Transactional(readOnly = true)
 *     public Page<User> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
 *         return userRepository.findByCriteria(criteria, pageable);
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>2. Class-Level Annotations</h3>
 * <pre>
 * {@code
 * // All methods in this class will use read datasource by default
 * @ReadOnlyDataSource
 * @Service
 * public class UserQueryService {
 * 
 *     @Transactional(readOnly = true)
 *     public List<User> findActiveUsers() {
 *         return userRepository.findByActive(true);
 *     }
 * 
 *     // Override class-level annotation for specific method
 *     @WriteDataSource
 *     @Transactional
 *     public void updateLastLoginTime(Long userId) {
 *         userRepository.updateLastLoginTime(userId, LocalDateTime.now());
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>3. Repository Layer Examples</h3>
 * <pre>
 * {@code
 * @Repository
 * public interface UserRepository extends JpaRepository<User, Long> {
 * 
 *     // Standard CRUD operations inherit from service layer annotations
 *     // No additional annotations needed
 * 
 *     // Custom read queries can be explicitly marked
 *     @ReadOnlyDataSource
 *     @Query("SELECT u FROM User u WHERE u.department = :dept")
 *     List<User> findByDepartment(@Param("dept") String department);
 * 
 *     // Complex write operations
 *     @WriteDataSource
 *     @Modifying
 *     @Query("UPDATE User u SET u.lastLogin = :time WHERE u.id = :id")
 *     void updateLastLoginTime(@Param("id") Long id, @Param("time") LocalDateTime time);
 * }
 * }
 * </pre>
 * 
 * <h3>4. Controller Layer Best Practices</h3>
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/api/users")
 * public class UserController {
 * 
 *     // Read endpoints - delegate to service layer
 *     @GetMapping
 *     public ResponseEntity<List<UserDTO>> getAllUsers() {
 *         // Service method should have @ReadOnlyDataSource
 *         List<UserDTO> users = userService.getAllUsers();
 *         return ResponseEntity.ok(users);
 *     }
 * 
 *     // Write endpoints - delegate to service layer  
 *     @PostMapping
 *     public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
 *         // Service method should have @WriteDataSource (or inherit default)
 *         UserDTO user = userService.createUser(request);
 *         return ResponseEntity.ok(user);
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>5. Transaction Management Integration</h3>
 * <pre>
 * {@code
 * @Service
 * public class UserService {
 * 
 *     // Best practice: Combine datasource routing with proper transaction config
 *     @ReadOnlyDataSource
 *     @Transactional(readOnly = true, timeout = 30)
 *     public UserStatistics getUserStatistics() {
 *         // Long-running read query with timeout
 *         return userRepository.calculateStatistics();
 *     }
 * 
 *     @WriteDataSource
 *     @Transactional(rollbackFor = Exception.class, timeout = 10)
 *     public User updateUserProfile(Long userId, UpdateUserRequest request) {
 *         // Write operation with rollback configuration
 *         User user = userRepository.findById(userId)
 *             .orElseThrow(() -> new UserNotFoundException(userId));
 *         user.updateProfile(request);
 *         return userRepository.save(user);
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>Configuration Details:</h2>
 * 
 * <h3>DataSource Configuration</h3>
 * <ul>
 *   <li><strong>Write DataSource:</strong> Optimized for INSERT/UPDATE/DELETE operations</li>
 *   <li><strong>Read DataSource:</strong> Optimized for SELECT operations with larger connection pool</li>
 *   <li><strong>Connection Pools:</strong> Separate HikariCP pools with different configurations</li>
 *   <li><strong>Routing:</strong> Automatic routing based on annotations via AOP</li>
 * </ul>
 * 
 * <h3>Performance Benefits</h3>
 * <ul>
 *   <li><strong>Load Distribution:</strong> Separate read and write traffic</li>
 *   <li><strong>Connection Pool Optimization:</strong> Different pool sizes for different operations</li>
 *   <li><strong>Read Replica Support:</strong> Easy to configure read replicas later</li>
 *   <li><strong>Transaction Optimization:</strong> Read-only transactions for better performance</li>
 * </ul>
 * 
 * <h3>Testing Considerations</h3>
 * <ul>
 *   <li><strong>Test Profile:</strong> Uses single H2 database for both read and write</li>
 *   <li><strong>Annotation Validation:</strong> AOP aspect validates transaction configuration</li>
 *   <li><strong>Connection Pool:</strong> Minimal pool configuration for fast test execution</li>
 * </ul>
 * 
 * @author ITT Team
 * @version 1.0
 * @see ReadOnlyDataSource
 * @see WriteDataSource  
 * @see DataSourceRoutingAspect
 * @see DatabaseConfig
 */
public final class DataSourceConfigurationGuide {
    
    private DataSourceConfigurationGuide() {
        // Utility class - no instantiation
    }
    
    /**
     * Quick reference for common operation patterns.
     */
    public static final class OperationPatterns {
        
        public static final String READ_OPERATIONS = """
            Common read operations that should use @ReadOnlyDataSource:
            - findAll(), findById(), findBy*()
            - search(), filter(), paginate()
            - count(), exists(), statistics()
            - dashboard queries, reports
            - lookup data, reference data
            """;
            
        public static final String WRITE_OPERATIONS = """
            Common write operations that should use @WriteDataSource:
            - save(), saveAll(), create()
            - update(), updateAll(), patch()
            - delete(), deleteAll(), remove()
            - bulk operations, batch processing
            - audit logging, event publishing
            """;
            
        private OperationPatterns() {}
    }
    
    /**
     * Performance tuning recommendations.
     */
    public static final class PerformanceTips {
        
        public static final String CONNECTION_POOLS = """
            Connection Pool Optimization:
            - Read Pool: Larger size (100+ connections) for concurrent reads
            - Write Pool: Smaller size (50 connections) for controlled writes
            - Idle Timeout: Longer for reads (10 min), shorter for writes (5 min)
            - Connection Timeout: Faster for reads (10s), standard for writes (20s)
            """;
            
        public static final String TRANSACTION_CONFIG = """
            Transaction Configuration Best Practices:
            - Read operations: @Transactional(readOnly = true)
            - Write operations: @Transactional with appropriate rollback rules
            - Use timeouts for long-running operations
            - Combine datasource routing with transaction configuration
            """;
            
        private PerformanceTips() {}
    }
}
