# 📚 Complete Read/Write DataSource Developer Guide

*A comprehensive, beginner-to-advanced guide for implementing and using read/write datasource separation*

---

## 📖 Table of Contents

1. [🎯 What is Read/Write DataSource Separation?](#what-is-readwrite-datasource-separation)
2. [🏗️ Why Do We Need This?](#why-do-we-need-this)
3. [🔧 How We Implemented It](#how-we-implemented-it)
4. [📋 Complete Implementation Guide](#complete-implementation-guide)
5. [💡 Step-by-Step Usage Guide](#step-by-step-usage-guide)
6. [🎨 Practical Examples](#practical-examples)
7. [🚀 Best Practices](#best-practices)
8. [🔍 Troubleshooting](#troubleshooting)
9. [📦 Reusing in New Projects](#reusing-in-new-projects)

---

## 🎯 What is Read/Write DataSource Separation?

### Simple Explanation
Imagine you have a restaurant with two kitchens:
- **🍽️ Fast Kitchen**: Handles simple orders (reading menu, getting prices) - **READ OPERATIONS**
- **👨‍🍳 Main Kitchen**: Handles complex cooking (preparing food, modifying recipes) - **WRITE OPERATIONS**

Similarly, in our application:
- **📖 Read DataSource**: Handles SELECT queries (finding, searching, counting)
- **✏️ Write DataSource**: Handles INSERT/UPDATE/DELETE queries (creating, updating, deleting)

### Technical Benefits
```
┌─────────────────┐    ┌─────────────────┐
│   READ QUERIES  │    │  WRITE QUERIES  │
│                 │    │                 │
│ • SELECT        │    │ • INSERT        │
│ • COUNT         │    │ • UPDATE        │
│ • EXISTS        │    │ • DELETE        │
│ • SEARCH        │    │ • BATCH OPS     │
└─────────────────┘    └─────────────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│  READ DATABASE  │    │ WRITE DATABASE  │
│                 │    │                 │
│ • 100 Connections│   │ • 50 Connections│
│ • Read Optimized │   │ • Write Optimized│
│ • Faster Reads   │   │ • ACID Compliance│
└─────────────────┘    └─────────────────┘
```

---

## 🏗️ Why Do We Need This?

### 🚀 Performance Benefits
- **Faster Reads**: Read database optimized for SELECT queries
- **Better Writes**: Write database optimized for transactions
- **Load Distribution**: Spread database load across multiple connections
- **Scalability**: Handle more concurrent users

### 🛡️ Reliability Benefits
- **Isolation**: Read operations don't block write operations
- **Availability**: If one database is slow, the other can still work
- **Monitoring**: Separate metrics for read vs write performance

### 💰 Cost Benefits
- **Resource Optimization**: Different connection pools for different needs
- **AWS Cost Savings**: Optimize database resources based on usage patterns

---

## 🔧 How We Implemented It

Our implementation has **4 main components**:

### 1. 🗃️ **Database Configuration** (`DatabaseConfig.java`)
Creates two separate connection pools

### 2. 🏷️ **Custom Annotations**
- `@ReadOnlyDataSource` - Marks methods that only read data
- `@WriteDataSource` - Marks methods that modify data

### 3. 🔄 **AOP Aspect** (`DataSourceRoutingAspect.java`)
Automatically routes database calls to the right datasource

### 4. 🧭 **Context Holder** (`DataSourceContextHolder.java`)
Keeps track of which datasource to use

```
┌─────────────────────────────────────────────────────────────┐
│                    YOUR SERVICE METHOD                       │
│  @ReadOnlyDataSource                                        │
│  public List<User> getAllUsers() { ... }                    │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  AOP ASPECT INTERCEPTS                      │
│  1. Sees @ReadOnlyDataSource annotation                     │
│  2. Sets context to "READ"                                  │
│  3. Executes your method                                    │
│  4. Cleans up context                                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                ROUTING DATASOURCE                           │
│  Checks context: "READ" → routes to Read Database          │
│                 "WRITE" → routes to Write Database         │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE                                 │
│  Query executes on the correct database                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Complete Implementation Guide

### Step 1: 🗃️ Database Configuration

**File: `src/main/java/config/DatabaseConfig.java`**

```java
@Configuration
@EnableJpaRepositories(basePackages = "com.yourproject.repository")
@EnableTransactionManagement
public class DatabaseConfig {

    // ========================================
    // WRITE DATASOURCE (Primary Database)
    // ========================================
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.write.hikari")
    public DataSource writeDataSource() {
        return DataSourceBuilder
            .create()
            .type(HikariDataSource.class)
            .driverClassName("com.amazonaws.secretsmanager.sql.AWSSecretsManagerMySQLDriver")
            .url("jdbc-secretsmanager:mysql://write-db-host:3306/yourdb")
            .build();
    }

    // ========================================
    // READ DATASOURCE (Read-Only Database)
    // ========================================
    
    @Bean
    @ConfigurationProperties("spring.datasource.read.hikari")
    public DataSource readDataSource() {
        return DataSourceBuilder
            .create()
            .type(HikariDataSource.class)
            .driverClassName("com.amazonaws.secretsmanager.sql.AWSSecretsManagerMySQLDriver")
            .url("jdbc-secretsmanager:mysql://read-db-host:3306/yourdb")
            .build();
    }

    // ========================================
    // ROUTING DATASOURCE (Smart Router)
    // ========================================
    
    @Bean
    public DataSource routingDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.WRITE, writeDataSource());
        targetDataSources.put(DataSourceType.READ, readDataSource());
        
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(writeDataSource()); // Default to write
        
        return routingDataSource;
    }

    // ========================================
    // JPA CONFIGURATION
    // ========================================
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(routingDataSource());
        em.setPackagesToScan("com.yourproject.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }
}
```

### Step 2: 🏷️ Custom Annotations

**File: `src/main/java/annotation/ReadOnlyDataSource.java`**

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnlyDataSource {
    /**
     * Description of what this read operation does.
     * This helps with monitoring and debugging.
     */
    String value() default "Read operation";
}
```

**File: `src/main/java/annotation/WriteDataSource.java`**

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WriteDataSource {
    /**
     * Description of what this write operation does.
     * This helps with monitoring and debugging.
     */
    String value() default "Write operation";
}
```

### Step 3: 🧭 Context Holder

**File: `src/main/java/context/DataSourceContextHolder.java`**

```java
/**
 * Thread-local storage for datasource routing context.
 * 
 * This class keeps track of which datasource (READ/WRITE) 
 * the current thread should use.
 */
public class DataSourceContextHolder {
    
    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();
    
    /**
     * Set the datasource type for current thread
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        contextHolder.set(dataSourceType);
    }
    
    /**
     * Get the datasource type for current thread
     */
    public static DataSourceType getDataSourceType() {
        return contextHolder.get();
    }
    
    /**
     * Clear the datasource type (cleanup)
     */
    public static void clearDataSourceType() {
        contextHolder.remove();
    }
}

/**
 * Enum for datasource types
 */
public enum DataSourceType {
    READ,   // For SELECT queries
    WRITE   // For INSERT/UPDATE/DELETE queries
}
```

### Step 4: 🔄 AOP Aspect (The Magic Happens Here!)

**File: `src/main/java/aspect/DataSourceRoutingAspect.java`**

```java
@Aspect
@Component
@Slf4j
public class DataSourceRoutingAspect {

    /**
     * Intercept methods with @ReadOnlyDataSource annotation
     */
    @Around("@annotation(readOnlyDataSource)")
    public Object routeToReadDataSource(ProceedingJoinPoint joinPoint, ReadOnlyDataSource readOnlyDataSource) throws Throwable {
        return executeWithDataSource(joinPoint, DataSourceType.READ, readOnlyDataSource.value());
    }

    /**
     * Intercept methods with @WriteDataSource annotation
     */
    @Around("@annotation(writeDataSource)")
    public Object routeToWriteDataSource(ProceedingJoinPoint joinPoint, WriteDataSource writeDataSource) throws Throwable {
        return executeWithDataSource(joinPoint, DataSourceType.WRITE, writeDataSource.value());
    }

    /**
     * Intercept all methods in classes with @ReadOnlyDataSource annotation
     */
    @Around("@within(readOnlyDataSource) && !@annotation(WriteDataSource)")
    public Object routeClassToReadDataSource(ProceedingJoinPoint joinPoint, ReadOnlyDataSource readOnlyDataSource) throws Throwable {
        return executeWithDataSource(joinPoint, DataSourceType.READ, readOnlyDataSource.value());
    }

    /**
     * Execute method with specified datasource
     */
    private Object executeWithDataSource(ProceedingJoinPoint joinPoint, DataSourceType dataSourceType, String description) throws Throwable {
        
        // Remember the previous datasource (for nested calls)
        DataSourceType previousDataSourceType = DataSourceContextHolder.getDataSourceType();
        
        try {
            // Set the datasource for this operation
            DataSourceContextHolder.setDataSourceType(dataSourceType);
            
            // Log for debugging (remove in production)
            log.debug("Routing to {} datasource: {} - {}", 
                     dataSourceType, joinPoint.getSignature().getName(), description);
            
            // Execute the actual method
            return joinPoint.proceed();
            
        } finally {
            // Restore previous datasource (important for nested calls)
            DataSourceContextHolder.setDataSourceType(previousDataSourceType);
        }
    }
}
```

### Step 5: 🧭 Routing DataSource

**File: `src/main/java/config/RoutingDataSource.java`**

```java
/**
 * Custom DataSource that routes to different databases
 * based on the current thread context.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    /**
     * This method is called by Spring to determine which
     * actual datasource to use for the current operation.
     */
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = DataSourceContextHolder.getDataSourceType();
        
        // If no specific datasource is set, default to WRITE
        if (dataSourceType == null) {
            return DataSourceType.WRITE;
        }
        
        return dataSourceType;
    }
}
```

---

## 💡 Step-by-Step Usage Guide

### 🚀 For New Developers: Start Here!

#### 1. **Identify Your Operation Type**

Ask yourself: *"What is this method doing?"*

| Operation Type | Examples | Use This Annotation |
|---|---|---|
| **📖 Reading Data** | `findAll()`, `findById()`, `search()`, `count()`, `exists()` | `@ReadOnlyDataSource` |
| **✏️ Writing Data** | `save()`, `create()`, `update()`, `delete()`, `saveAll()` | `@WriteDataSource` or none |

#### 2. **Basic Service Pattern**

```java
@Service
@RequiredArgsConstructor  // Lombok for constructor injection
public class UserService {
    
    private final UserRepository userRepository;
    
    // ========================================
    // 📖 READ OPERATIONS
    // ========================================
    
    @ReadOnlyDataSource("Get all users for admin panel")
    @Transactional(readOnly = true)  // Important: readOnly = true
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    @ReadOnlyDataSource("Search users by name")
    @Transactional(readOnly = true, timeout = 30)  // 30 second timeout
    public Page<UserDTO> searchUsers(String name, Pageable pageable) {
        return userRepository.findByNameContaining(name, pageable)
                .map(this::convertToDTO);
    }
    
    @ReadOnlyDataSource("Get user statistics")
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);
        return new UserStatistics(totalUsers, activeUsers);
    }
    
    // ========================================
    // ✏️ WRITE OPERATIONS
    // ========================================
    
    @WriteDataSource("Create new user")  // Optional - defaults to write anyway
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }
    
    @Transactional(rollbackFor = Exception.class)  // No annotation = write datasource
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    @WriteDataSource("Delete user")
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
    }
    
    // ========================================
    // 🔧 HELPER METHODS
    // ========================================
    
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.isActive()
        );
    }
}
```

#### 3. **Repository Pattern**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    // ========================================
    // These methods inherit datasource from service layer
    // ========================================
    
    // Standard methods - no annotations needed
    List<User> findByActive(boolean active);
    Page<User> findByNameContaining(String name, Pageable pageable);
    boolean existsByEmail(String email);
    long countByActive(boolean active);
    
    // ========================================
    // Custom queries with explicit routing (advanced)
    // ========================================
    
    @ReadOnlyDataSource("Complex user search")
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.department = :dept")
    List<User> findByNameAndDepartment(@Param("name") String name, @Param("dept") String department);
    
    @WriteDataSource("Bulk user status update")
    @Modifying
    @Query("UPDATE User u SET u.active = :status WHERE u.id IN :ids")
    void updateUserStatusBatch(@Param("ids") List<Long> userIds, @Param("status") boolean status);
}
```

---

## 🎨 Practical Examples

### Example 1: 🛒 E-commerce Product Service

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    // ========================================
    // 📖 CUSTOMER-FACING READ OPERATIONS
    // ========================================
    
    @ReadOnlyDataSource("Product catalog for website")
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductCatalog(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::toDTO);
    }
    
    @ReadOnlyDataSource("Product search")
    @Transactional(readOnly = true, timeout = 30)
    public List<ProductDTO> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query)
                .stream()
                .map(this::toDTO)
                .toList();
    }
    
    @ReadOnlyDataSource("Product details page")
    @Transactional(readOnly = true)
    public ProductDTO getProductDetails(Long productId) {
        return productRepository.findById(productId)
                .map(this::toDTO)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    // ========================================
    // ✏️ ADMIN WRITE OPERATIONS
    // ========================================
    
    @WriteDataSource("Create new product")
    @Transactional(rollbackFor = Exception.class)
    public ProductDTO createProduct(CreateProductRequest request) {
        // Validate product doesn't exist
        if (productRepository.existsByName(request.getName())) {
            throw new ProductAlreadyExistsException(request.getName());
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }
    
    @WriteDataSource("Update product inventory")
    @Transactional(rollbackFor = Exception.class)
    public void updateInventory(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        product.setQuantity( quantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        productRepository.save(product);
    }
    
    // ========================================
    // 🔄 MIXED OPERATIONS (Read then Write)
    // ========================================
    
    @WriteDataSource("Process order - read then write")
    @Transactional(rollbackFor = Exception.class)
    public OrderResult processOrder(OrderRequest request) {
        // This read will use WRITE datasource (we're in a write transaction)
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));
        
        // Validate inventory
        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientInventoryException(product.getName());
        }
        
        // Update inventory
        product.setQuantity(product.getQuantity() - request.getQuantity());
        productRepository.save(product);
        
        return new OrderResult(product.getName(), request.getQuantity());
    }
    
    private ProductDTO toDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getQuantity(),
            product.isActive()
        );
    }
}
```

### Example 2: 📊 Analytics Service (Mostly Read Operations)

```java
@ReadOnlyDataSource  // Class-level annotation - all methods use read datasource by default
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    // ========================================
    // All these methods automatically use READ datasource
    // ========================================
    
    @Transactional(readOnly = true, timeout = 60)  // Long timeout for complex analytics
    public DashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        
        return new DashboardStats(totalUsers, totalOrders, totalProducts);
    }
    
    @Transactional(readOnly = true, timeout = 120)  // Even longer for complex queries
    public List<SalesReportDTO> getMonthlySalesReport(int year) {
        return orderRepository.findMonthlySalesReport(year)
                .stream()
                .map(this::toSalesReportDTO)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<TopProductDTO> getTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(limit)
                .stream()
                .map(this::toTopProductDTO)
                .toList();
    }
    
    // ========================================
    // Exception: This method needs to write (audit log)
    // ========================================
    
    @WriteDataSource("Log analytics access")  // Override class-level annotation
    @Transactional(rollbackFor = Exception.class)
    public void logAnalyticsAccess(String userId, String reportType) {
        AnalyticsLog log = new AnalyticsLog();
        log.setUserId(userId);
        log.setReportType(reportType);
        log.setAccessTime(LocalDateTime.now());
        
        analyticsLogRepository.save(log);  // This needs write datasource
    }
}
```

### Example 3: 🔄 Mixed Operations Service

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    // ========================================
    // 📖 READ OPERATIONS
    // ========================================
    
    @ReadOnlyDataSource("Get user's order history")
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }
    
    @ReadOnlyDataSource("Order status tracking")
    @Transactional(readOnly = true)
    public OrderStatusDTO getOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        return new OrderStatusDTO(order.getId(), order.getStatus(), order.getTrackingNumber());
    }
    
    // ========================================
    // ✏️ WRITE OPERATIONS
    // ========================================
    
    @WriteDataSource("Create new order")
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(CreateOrderRequest request) {
        // Validate user exists (this read uses WRITE datasource since we're in a write transaction)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        
        // Create order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        // Process order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));
            
            // Check inventory
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientInventoryException(product.getName());
            }
            
            // Update inventory
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
            
            // Add to order
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            order.addItem(orderItem);
            
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        return toDTO(savedOrder);
    }
    
    @WriteDataSource("Update order status")
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        order.setStatus(newStatus);
        if (trackingNumber != null) {
            order.setTrackingNumber(trackingNumber);
        }
        order.setUpdatedAt(LocalDateTime.now());
        
        orderRepository.save(order);
    }
    
    private OrderDTO toDTO(Order order) {
        return new OrderDTO(
            order.getId(),
            order.getUserId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt()
        );
    }
}
```

---

## 🚀 Best Practices

### ✅ DO's

#### 1. **Always Use Appropriate Annotations**
```java
// ✅ GOOD: Clear annotation with description
@ReadOnlyDataSource("User profile lookup for dashboard")
@Transactional(readOnly = true)
public UserDTO getUserProfile(Long userId) { }

// ❌ BAD: No annotation or description
@Transactional
public UserDTO getUserProfile(Long userId) { }
```

#### 2. **Match Transaction Configuration**
```java
// ✅ GOOD: Read operation with readOnly = true
@ReadOnlyDataSource("Search users")
@Transactional(readOnly = true, timeout = 30)
public List<UserDTO> searchUsers(String query) { }

// ✅ GOOD: Write operation with rollback rules
@WriteDataSource("Create user")
@Transactional(rollbackFor = Exception.class)
public UserDTO createUser(CreateUserRequest request) { }
```

#### 3. **Use Meaningful Descriptions**
```java
// ✅ GOOD: Descriptive
@ReadOnlyDataSource("Complex analytics query for monthly sales report")

// ❌ BAD: Generic
@ReadOnlyDataSource("Read operation")
```

#### 4. **Handle Timeouts Appropriately**
```java
// ✅ GOOD: Quick operations
@ReadOnlyDataSource("Simple user lookup")
@Transactional(readOnly = true, timeout = 5)  // 5 seconds
public UserDTO findUser(Long id) { }

// ✅ GOOD: Complex operations
@ReadOnlyDataSource("Complex analytics")
@Transactional(readOnly = true, timeout = 120)  // 2 minutes
public AnalyticsReport generateReport() { }
```

### ❌ DON'Ts

#### 1. **Don't Mix Read/Write Without Annotations**
```java
// ❌ BAD: Reading then writing without proper annotations
@Transactional
public void updateUserProfile(Long userId, UpdateRequest request) {
    // This read might go to read database
    User user = userRepository.findById(userId).get();
    
    // This write might fail or be inconsistent
    user.setName(request.getName());
    userRepository.save(user);
}

// ✅ GOOD: Use @WriteDataSource for mixed operations
@WriteDataSource("Update user profile")
@Transactional(rollbackFor = Exception.class)
public void updateUserProfile(Long userId, UpdateRequest request) {
    // Both read and write use write database for consistency
    User user = userRepository.findById(userId).get();
    user.setName(request.getName());
    userRepository.save(user);
}
```

#### 2. **Don't Forget Transaction Configuration**
```java
// ❌ BAD: Read operation without readOnly = true
@ReadOnlyDataSource("Get users")
@Transactional  // Missing readOnly = true
public List<UserDTO> getUsers() { }

// ✅ GOOD
@ReadOnlyDataSource("Get users")
@Transactional(readOnly = true)
public List<UserDTO> getUsers() { }
```

#### 3. **Don't Use Long Timeouts for Simple Operations**
```java
// ❌ BAD: Unnecessarily long timeout
@ReadOnlyDataSource("Simple lookup")
@Transactional(readOnly = true, timeout = 300)  // 5 minutes for simple lookup!
public UserDTO findUser(Long id) { }

// ✅ GOOD: Appropriate timeout
@ReadOnlyDataSource("Simple lookup")
@Transactional(readOnly = true, timeout = 5)  // 5 seconds
public UserDTO findUser(Long id) { }
```

---

## 🔍 Troubleshooting

### Common Issues and Solutions

#### Issue 1: 🚨 "Method uses read datasource but transaction is not readOnly"

**Error Message:**
```
WARN: Method [getUserStats()] uses read datasource but @Transactional is not marked as readOnly=true
```

**Solution:**
```java
// ❌ Problem
@ReadOnlyDataSource("User statistics")
@Transactional
public UserStats getUserStats() { }

// ✅ Fix
@ReadOnlyDataSource("User statistics")
@Transactional(readOnly = true)  // Add readOnly = true
public UserStats getUserStats() { }
```

#### Issue 2: 🚨 Connection Pool Exhaustion

**Error Message:**
```
HikariPool-1 - Connection is not available, request timed out after 30000ms
```

**Possible Causes:**
1. Too many concurrent operations
2. Long-running transactions not committed
3. Connection leaks

**Solutions:**
```java
// ✅ Add appropriate timeouts
@ReadOnlyDataSource("Complex query")
@Transactional(readOnly = true, timeout = 60)  // 60 second timeout
public ComplexResult complexQuery() { }

// ✅ Use batch operations for bulk updates
@WriteDataSource("Bulk user update")
@Transactional(rollbackFor = Exception.class)
public void updateUsersBatch(List<User> users) {
    userRepository.saveAll(users);  // More efficient than individual saves
}
```

#### Issue 3: 🚨 Data Inconsistency

**Problem:** Reading from read database, then writing to write database

```java
// ❌ Problem: Potential inconsistency
@Transactional
public void updateUserIfExists(Long userId, String newName) {
    // This might read from read database
    if (userRepository.existsById(userId)) {
        User user = userRepository.findById(userId).get();
        user.setName(newName);
        userRepository.save(user);  // This writes to write database
    }
}
```

**Solution:** Use @WriteDataSource for read-then-write operations

```java
// ✅ Fix: Both read and write use write database
@WriteDataSource("Update user if exists")
@Transactional(rollbackFor = Exception.class)
public void updateUserIfExists(Long userId, String newName) {
    if (userRepository.existsById(userId)) {
        User user = userRepository.findById(userId).get();
        user.setName(newName);
        userRepository.save(user);
    }
}
```

#### Issue 4: 🚨 Annotation Not Working

**Possible Causes:**
1. Missing `@EnableAspectJAutoProxy` in configuration
2. Method not public
3. Method called from within same class (AOP limitation)

**Solutions:**
```java
// ✅ Configuration
@Configuration
@EnableAspectJAutoProxy  // Don't forget this!
public class AppConfig { }

// ✅ Method must be public
@ReadOnlyDataSource("Get users")
@Transactional(readOnly = true)
public List<UserDTO> getUsers() { }  // public, not private

// ❌ Problem: Internal method call
@Service
public class UserService {
    
    @ReadOnlyDataSource("Get users")
    @Transactional(readOnly = true)
    public List<UserDTO> getUsers() {
        return getActiveUsers();  // This won't use read datasource!
    }
    
    @ReadOnlyDataSource("Get active users")
    @Transactional(readOnly = true)
    public List<UserDTO> getActiveUsers() { }
}

// ✅ Fix: Use separate service or direct repository call
@Service
public class UserService {
    
    @ReadOnlyDataSource("Get users")
    @Transactional(readOnly = true)
    public List<UserDTO> getUsers() {
        return userRepository.findByActive(true)  // Direct call
                .stream()
                .map(this::toDTO)
                .toList();
    }
}
```

### Debugging Tips

#### 1. **Enable Debug Logging**
```yaml
# application.yml
logging:
  level:
    com.yourproject.aspect.DataSourceRoutingAspect: DEBUG
    org.springframework.jdbc.datasource: DEBUG
```

#### 2. **Monitor Connection Pools**
```yaml
# application.yml
spring:
  datasource:
    write:
      hikari:
        leak-detection-threshold: 60000  # 1 minute
    read:
      hikari:
        leak-detection-threshold: 60000  # 1 minute
```

#### 3. **Add Health Checks**
```java
@Component
public class DataSourceHealthIndicator implements HealthIndicator {
    
    @Autowired
    @Qualifier("writeDataSource")
    private DataSource writeDataSource;
    
    @Autowired
    @Qualifier("readDataSource") 
    private DataSource readDataSource;
    
    @Override
    public Health health() {
        try {
            // Test both datasources
            writeDataSource.getConnection().close();
            readDataSource.getConnection().close();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

---

## 📦 Reusing in New Projects

### Quick Setup Checklist

#### 1. **Copy Core Files**
```
src/main/java/
├── config/
│   ├── DatabaseConfig.java           ✅ Copy
│   └── RoutingDataSource.java        ✅ Copy
├── annotation/
│   ├── ReadOnlyDataSource.java       ✅ Copy
│   └── WriteDataSource.java          ✅ Copy
├── aspect/
│   └── DataSourceRoutingAspect.java  ✅ Copy
└── context/
    ├── DataSourceContextHolder.java  ✅ Copy
    └── DataSourceType.java           ✅ Copy
```

#### 2. **Update Configuration**
```yaml
# application.yml - Update with your database URLs
spring:
  datasource:
    write:
      hikari:
        jdbc-url: jdbc-secretsmanager:mysql://your-write-db:3306/yourdb
        maximum-pool-size: 50
        minimum-idle: 10
    read:
      hikari:
        jdbc-url: jdbc-secretsmanager:mysql://your-read-db:3306/yourdb
        maximum-pool-size: 100
        minimum-idle: 20
```

#### 3. **Add Dependencies**
```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- AOP for routing aspect -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    
    <!-- HikariCP (usually included with Spring Boot) -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
    </dependency>
    
    <!-- AWS Secrets Manager (if using AWS) -->
    <dependency>
        <groupId>com.amazonaws.secretsmanager</groupId>
        <artifactId>aws-secretsmanager-jdbc</artifactId>
        <version>1.0.12</version>
    </dependency>
</dependencies>
```

#### 4. **Update Package Names**
Update all package names in the copied files:
```java
// Change from:
package com.itt.service.annotation;

// To your project:
package com.yourcompany.yourproject.annotation;
```

#### 5. **Configuration Class**
```java
@SpringBootApplication
@EnableAspectJAutoProxy  // Don't forget this!
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### Template Service for New Projects

```java
@Service
@RequiredArgsConstructor
public class TemplateService {
    
    private final YourRepository repository;
    
    // ========================================
    // 📖 READ OPERATIONS TEMPLATE
    // ========================================
    
    @ReadOnlyDataSource("Find all entities")
    @Transactional(readOnly = true)
    public List<YourDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }
    
    @ReadOnlyDataSource("Find entity by ID")
    @Transactional(readOnly = true)
    public YourDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found: " + id));
    }
    
    @ReadOnlyDataSource("Search entities")
    @Transactional(readOnly = true, timeout = 30)
    public Page<YourDTO> search(String query, Pageable pageable) {
        return repository.findByNameContaining(query, pageable)
                .map(this::toDTO);
    }
    
    // ========================================
    // ✏️ WRITE OPERATIONS TEMPLATE
    // ========================================
    
    @WriteDataSource("Create entity")
    @Transactional(rollbackFor = Exception.class)
    public YourDTO create(CreateRequest request) {
        YourEntity entity = new YourEntity();
        entity.setName(request.getName());
        // Set other fields...
        
        YourEntity saved = repository.save(entity);
        return toDTO(saved);
    }
    
    @WriteDataSource("Update entity")
    @Transactional(rollbackFor = Exception.class)
    public YourDTO update(Long id, UpdateRequest request) {
        YourEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found: " + id));
        
        entity.setName(request.getName());
        // Update other fields...
        
        YourEntity updated = repository.save(entity);
        return toDTO(updated);
    }
    
    @WriteDataSource("Delete entity")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity not found: " + id);
        }
        repository.deleteById(id);
    }
    
    private YourDTO toDTO(YourEntity entity) {
        return new YourDTO(
            entity.getId(),
            entity.getName()
            // Map other fields...
        );
    }
}
```

---

## 🎓 Learning Path for New Developers

### Week 1: Understanding the Basics
1. **Day 1-2**: Read this guide completely
2. **Day 3-4**: Study the example services in your project
3. **Day 5**: Try modifying existing services with read/write annotations

### Week 2: Hands-On Practice
1. **Day 1-2**: Create a simple CRUD service using the patterns
2. **Day 3-4**: Add complex read operations (search, filtering)
3. **Day 5**: Add batch operations and mixed read/write operations

### Week 3: Advanced Usage
1. **Day 1-2**: Learn about transaction configuration and timeouts
2. **Day 3-4**: Practice debugging and troubleshooting
3. **Day 5**: Review and optimize existing services

### Quick Reference Card

Print this and keep it handy:

```
🎯 READ OPERATIONS                    ✏️ WRITE OPERATIONS
========================              ========================
@ReadOnlyDataSource("description")    @WriteDataSource("description")
@Transactional(readOnly = true)       @Transactional(rollbackFor = Exception.class)

Examples:                             Examples:
• findAll()                          • save()
• findById()                         • create()
• search()                           • update()
• count()                            • delete()
• exists()                           • batch operations

Use when:                            Use when:  
• Getting data                       • Changing data
• Searching                          • Creating records
• Generating reports                 • Updating records
• Counting records                   • Deleting records
```

---

## 🏆 Conclusion

You now have a complete understanding of our read/write datasource implementation! This guide covered:

✅ **What it is** and why we use it  
✅ **How we implemented it** in our project  
✅ **Step-by-step usage** for new developers  
✅ **Practical examples** from real scenarios  
✅ **Best practices** and common pitfalls  
✅ **Troubleshooting** common issues  
✅ **Reusing the pattern** in new projects  

### Next Steps:
1. **Practice** with the examples in this guide
2. **Review** existing services in your project
3. **Apply** the patterns to new services you create
4. **Ask questions** when you're unsure

Remember: The goal is to write **faster, more scalable applications** while keeping the code **clean and maintainable**. This pattern helps us achieve both! 🚀

---

*Happy coding! 🎉*
