# 🧪 Developer Testing Guide - Service Layer Focus

> **Simple Testing Strategy: Only test Service Layer business logic**

## 📋 Quick Commands
```bash
# Essential daily commands
.\mvnw.cmd test                              # Run all tests
.\mvnw.cmd test -Dtest=YourServiceTest      # Run specific test  
.\mvnw.cmd test jacoco:report               # Generate coverage
start target/site/jacoco/index.html        # View coverage report
```

## 🚀 Quick Start (2 minutes)
1. **Verify Setup**: `.\mvnw.cmd test-compile`
2. **Run Tests**: `.\mvnw.cmd test`
3. **Check Coverage**: `.\mvnw.cmd test jacoco:report`
4. **Create Test**: Copy template below

## 🎯 Testing Strategy: Service Layer Only

### ✅ What to Test
- **Service business logic**: Validation, calculations, orchestration
- **Error handling**: Exception scenarios and edge cases
- **Data transformation**: Input/output mapping with business rules

### ❌ What NOT to Test
- **Controllers**: Just HTTP mapping, no business logic
- **Repositories**: Spring Data JPA handles CRUD operations
- **DTOs/Entities**: Simple data containers
- **Configuration**: Spring Boot manages this

### 📁 Project Structure
```
src/test/java/com/itt/service/
├── service/                    # ✅ Service layer tests (FOCUS HERE)
│   └── RoleManagementServiceImplTest.java
└── shared/                     # ✅ Test utilities
    └── builders/
        └── ServiceTestDataBuilder.java
```

## 📖 Service Test Template

**Copy this template for any new service test:**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Your Service - Business Logic Tests")
class YourServiceTest {

    @Mock private YourRepository yourRepository;
    @Mock private ExternalService externalService;
    @InjectMocks private YourService yourService;

    @Test
    @DisplayName("Should process request when valid input provided")
    void shouldProcessRequestWhenValidInput() {
        // Given
        YourRequest request = createValidRequest("TEST_DATA");
        YourEntity expectedEntity = createMockEntity("TEST_DATA");
        when(yourRepository.save(any())).thenReturn(expectedEntity);

        // When
        YourResponse result = yourService.processRequest(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TEST_DATA");
        verify(yourRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when invalid input")
    void shouldThrowExceptionWhenInvalidInput() {
        // Given
        YourRequest invalidRequest = createInvalidRequest();

        // When & Then
        assertThrows(ValidationException.class, 
            () -> yourService.processRequest(invalidRequest));
    }

    // Helper methods
    private YourRequest createValidRequest(String name) {
        YourRequest request = new YourRequest();
        request.setName(name);
        return request;
    }
}
```
        // Given
        String existingName = "EXISTING_ROLE";
        when(roleRepository.existsByName(existingName)).thenReturn(true);
        
        // When & Then
        assertThrows(BusinessValidationException.class, 
            () -> roleService.validateRoleName(existingName));
    }
    
    @Test
    @DisplayName("Should return privilege hierarchy with proper mapping")
    void shouldReturnPrivilegeHierarchyWithProperMapping() {
        // Given
        List<MapCategoryFeaturePrivilege> mockPrivileges = createMockPrivileges();
        when(privilegeRepository.findAllFull()).thenReturn(mockPrivileges);
        
        // When
        List<FeaturePrivilegeDTO> result = roleService.getPrivilegeHierarchy(Optional.empty());
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(privilegeRepository).findAllFull();
    }
    
    // Helper methods for test data creation
    private SaveRoleRequest createValidRoleRequest(String name) {
        SaveRoleRequest request = new SaveRoleRequest();
        request.setName(name);
        request.setDescription("Test role description");
        request.setIsActive(true);
        request.setPrivileges(List.of());
        return request;
    }
    
    private Role createMockRole(String name) {
        Role role = new Role();
        role.setId(1);
        role.setName(name);
        role.setDescription("Test role description");
        role.setIsActive(1);
        role.setCreatedOn(LocalDateTime.now());
        role.setCreatedById("SYSTEM");
        return role;
    }
}
```

### **❌ What NOT to Unit Test**

**Skip These Layers - No Unit Testing Needed:**

#### **1. Controller Layer**
```java
// ❌ DON'T unit test controllers
@RestController
public class RoleManagementController {
    
    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody SaveRoleRequest request) {
        // This is just HTTP mapping - no business logic
        RoleDto role = roleService.saveOrUpdateFull(request);
        return ResponseEntity.ok(ApiResponse.success(role));
    }
}
```
**Why Skip:** Controllers should be thin with no business logic. Just HTTP request/response mapping.

#### **2. Repository Layer**
```java
// ❌ DON'T unit test repositories  
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findByIsActive(Integer isActive);  // Spring Data handles this
    
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Optional<Role> findByName(@Param("name") String name);  // Database integration test instead
}
```
**Why Skip:** Spring Data JPA provides implementations. Database integration tests are more valuable.

#### **3. Configuration Classes**
```java
// ❌ DON'T unit test configuration
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        // Spring Boot handles configuration logic
    }
}
```
**Why Skip:** Infrastructure code. Spring Boot manages this.

#### **4. Simple DTOs/Entities**
```java
// ❌ DON'T unit test simple data objects
public class RoleDto {
    private String name;
    private String description;
    private boolean active;
    
    // Just getters/setters - no business logic
}
```
**Why Skip:** No business logic. Just data containers.

#### **5. Utility Classes (Simple)**
```java
// ❌ DON'T unit test simple utilities
public class DateUtils {
    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);  // No complex logic
    }
}
```
**Why Skip:** Simple formatting with no business rules.

### **🎯 Testing Coverage Strategy**

**Focus Areas for Service Layer:**
- ✅ **Business Logic Methods** - validation, calculations, transformations
- ✅ **Error Handling** - exception scenarios, edge cases
- ✅ **Integration Points** - how services interact with repositories
- ✅ **Conditional Logic** - if/else branches, switch statements

**Coverage Targets:**
- **Service Layer**: 80-85% coverage minimum
- **Overall Project**: 60-70% coverage (since we skip other layers)

**Quick Commands for Service-Only Testing:**
```bash
# Run only service layer tests
.\mvnw.cmd test -Dtest=**/service/**/*Test

# Generate coverage for service layer only
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test

# View service layer coverage
start target/site/jacoco/com.itt.service.service/index.html
```

---

## ⚡ Quick Fix for Current Issues

### 🆘 **Project Issue: AWS Configuration Blocking Tests**

**Problem**: Integration tests fail with "AWS security token expired"

**Quick Fix:**
```bash
# 1. Create test configuration (already done ✅)
# File: src/test/resources/application-test.yml

# 2. Run unit tests only (these work!)
.\mvnw.cmd test -Dtest=**/unit/**/*Test

# 3. Run service layer tests (working)
.\mvnw.cmd test -Dtest=**/*Service*Test

# 4. Skip integration tests temporarily
.\mvnw.cmd test -Dtest=!**/integration/**/*Test
```

**Permanent Solution:**
1. **Mock AWS Dependencies** in integration tests
2. **Use Test Profile** consistently
3. **Fix Validation Issues** with proper mocking

### 🎯 **Immediate Developer Actions**

**For New Developers (works right now):**
```bash
# ✅ These commands work immediately
.\mvnw.cmd test -Dtest=RoleManagementServiceImplTest    # 8 tests pass
.\mvnw.cmd test -Dtest=**/service/**/*Test              # Service layer works
.\mvnw.cmd test jacoco:report -Dtest=**/*Service*Test   # Coverage for services
```

**What Needs Fixing (for full workflow):**
```bash
# ❌ These currently fail (AWS issues)
.\mvnw.cmd test                                         # Some integration tests fail
.\mvnw.cmd test -Dtest=**/integration/**/*Test         # AWS token expired
```

---

## 🚀 Getting Started

### Essential Commands

```bash
# Run all tests
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=RoleManagementServiceImplTest

# Run tests and generate report
.\mvnw.cmd test jacoco:report

# View report
start target/site/jacoco/index.html
```

### Test Configuration

Create `src/test/resources/application-test.yml`:

```yaml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  jpa:
    hibernate:
      ddl-auto: create-drop
  cloud:
    aws:
      credentials:
        use-default-aws-credentials-chain: false
```

---

## 🗂️ Project Structure & Testing Organization

### Your Project's Simplified Test Folder Structure

**Focus Only on Service Layer:**
```
src/test/java/com/itt/service/
├── service/                          # ✅ Service layer unit tests (PRIMARY FOCUS)
│   ├── RoleManagementServiceImplTest.java      # Business logic testing
│   ├── CustomerSubscriptionServiceTest.java   # Service logic testing
│   ├── UserManagementServiceTest.java         # Core service testing
│   └── NotificationServiceTest.java           # Service behavior testing
└── shared/                           # ✅ Shared test utilities (SUPPORTING)
    ├── builders/                     # Test data builders (Builder pattern)
    │   ├── RoleTestDataBuilder.java  # Quick role data creation
    │   ├── UserTestDataBuilder.java  # Quick user data creation
    │   └── RequestTestDataBuilder.java # Quick request data creation
    ├── utils/                        # Test utility classes
    │   └── TestDataHelper.java       # Common test helper methods
    └── BaseServiceTest.java          # Common test configuration
```

**What We REMOVED (No Longer Needed):**
```
❌ controller/          # Skip - No business logic to test
❌ repository/          # Skip - Spring Data JPA handles CRUD
❌ integration/         # Skip - Focus on unit tests only
❌ functional/          # Skip - Unnecessary complexity
❌ api/                # Skip - HTTP testing not needed
```

### Testing Commands by Layer

**Service Layer (Our Primary Focus):**
```bash
# Test all services
.\mvnw.cmd test -Dtest=**/service/**/*Test

# Test specific service
.\mvnw.cmd test -Dtest=RoleManagementServiceImplTest

# Test services with coverage
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test
```

### Quick Test Creation by Service

**For a New Service Class:**
1. **Create**: `src/test/java/com/itt/service/service/YourServiceTest.java`
2. **Copy Template**: Use the simplified template from the strategy section above
3. **Mock Dependencies**: Only mock repositories and external services the service uses
4. **Test Business Logic**: Focus on validation, calculations, and error handling

**Simple Test Creation Process:**
```java
// 1. Copy this template for any new service
@ExtendWith(MockitoExtension.class)
@DisplayName("Your Service - Business Logic Tests")
class YourServiceTest {
    
    @Mock private YourRepository yourRepository;          // Mock data layer
    @Mock private AnotherService anotherService;          // Mock service dependencies
    
    @InjectMocks private YourService yourService;         // Service under test
    
    @Test
    @DisplayName("Should [action] when [condition]")
    void should[Action]When[Condition]() {
        // Given - Setup test data
        YourRequest request = TestDataBuilder.validRequest();
        YourEntity expectedEntity = TestDataBuilder.validEntity();
        when(yourRepository.save(any())).thenReturn(expectedEntity);
        
        // When - Execute business logic
        YourResponse result = yourService.processRequest(request);
        
        // Then - Verify results
        assertNotNull(result);
        assertEquals("expected", result.getSomeField());
        verify(yourRepository).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when [error condition]")
    void shouldThrowExceptionWhen[ErrorCondition]() {
        // Given - Setup error condition
        when(yourRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then - Verify exception
        assertThrows(BusinessException.class, 
            () -> yourService.getById(1L));
    }
}
```

---

## ⚙️ JaCoCo Coverage Configuration

### Your Project's Simplified JaCoCo Setup

**Maven Configuration (from pom.xml):**
- **Version**: 0.8.12
- **Focus**: Service layer coverage only
- **Minimum Coverage**: 70% (adjusted for service-only testing)
- **Coverage Level**: Class-level LINE coverage for services
- **Excluded Packages**: `**/config/**`, `**/controller/**`, `**/repository/**`, `**/dto/**`

### Simplified Coverage Commands

```bash
# Service layer coverage (our primary focus)
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test

# View service layer coverage report
start target/site/jacoco/com.itt.service.service/index.html

# Quick coverage check for services only
.\mvnw.cmd test jacoco:check -Dtest=**/service/**/*Test

# Skip unnecessary coverage checks temporarily
.\mvnw.cmd test -Djacoco.skip=true
```

### Understanding Service Layer Coverage

**HTML Report Navigation for Services:**
1. **Main Page** (`target/site/jacoco/index.html`)
   - Focus on `com.itt.service.service` package
   - Ignore controller/repository coverage percentages

2. **Service Package** (click on service package)
   - Class-level coverage for each service class
   - Red/Yellow/Green indicators for service methods

3. **Service Class Level** (click on service class name)
   - Method-level coverage within the service
   - Line-by-line business logic analysis
   - Branch coverage for if/else business rules

**Service Layer Coverage Metrics:**
- **Line Coverage**: Percentage of service business logic lines covered
- **Branch Coverage**: Percentage of if/else business branches covered  
- **Method Coverage**: Percentage of service methods called during tests

### Service-Specific Coverage Targets

```bash
# Check coverage for specific service
.\mvnw.cmd test jacoco:report -Dtest=RoleManagementServiceImplTest
start target/site/jacoco/com.itt.service.service/RoleManagementServiceImpl.html

# Check coverage for user-related services
.\mvnw.cmd test jacoco:report -Dtest=**/*User*ServiceTest

# Check coverage for all services together
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test
start target/site/jacoco/com.itt.service.service/index.html
```

### Simplified Coverage Troubleshooting

**Problem: "Coverage check failed on service" error**
```bash
# Check which service methods failed the 70% threshold
.\mvnw.cmd jacoco:check -X -Dtest=**/service/**/*Test

# Generate service-only report to see specific coverage
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test
start target/site/jacoco/com.itt.service.service/index.html
```

**Problem: Low coverage on new service class**
```bash
# Test specific new service
.\mvnw.cmd test -Dtest=YourNewServiceTest jacoco:report

# Check coverage for specific service package
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/YourNewServiceTest
```

**Problem: Exclude non-service classes from coverage requirements**
- Updated `pom.xml` under `<excludes>`:
```xml
<excludes>
    <exclude>**/config/**</exclude>
    <exclude>**/controller/**</exclude>
    <exclude>**/repository/**</exclude>
    <exclude>**/dto/**</exclude>
    <exclude>**/entity/**</exclude>
</excludes>
```

### Service Layer Coverage Strategy

**What to Measure in Services:**
- ✅ **Business Logic Methods** - validation, calculation methods
- ✅ **Error Handling** - exception throwing and handling
- ✅ **Conditional Logic** - if/else branches in business rules
- ✅ **Service Orchestration** - how services coordinate with repositories

**What NOT to Measure:**
- ❌ **Simple Getters/Setters** - no business value  
- ❌ **Framework Code** - Spring handles it
- ❌ **DTOs/Entities** - just data containers
- ❌ **Controllers** - thin HTTP mapping layer

**Target Coverage for Services:**
- **Individual Service Classes**: 80-85% line coverage
- **Service Package Overall**: 75-80% line coverage
- **Business Logic Methods**: 90%+ coverage
- **Error Handling Paths**: 80%+ coverage

---

## ⚡ Quick Testing Workflow

### 5-Minute Test Creation Process

```bash
# 1. Create test class (copy from template)
# 2. Run test to see it fail
.\mvnw.cmd test -Dtest=YourNewTest

# 3. Write minimal implementation
# 4. Run test to see it pass
.\mvnw.cmd test -Dtest=YourNewTest

# 5. Check coverage
.\mvnw.cmd test -Dtest=YourNewTest jacoco:report
start target/site/jacoco/index.html
```

### IDE Test Shortcuts

**IntelliJ IDEA:**
- `Ctrl+Shift+T` → Generate test class
- `Ctrl+Shift+F10` → Run current test
- `Alt+Enter` → Generate test method

**VS Code:**
- Right-click → "Generate Tests"
- Click "Run Test" above method
- Use Test Explorer panel

### Test-First Development Flow

```java
// 1. Write failing test first
@Test
void shouldCalculateDiscount() {
    // Given
    Customer customer = createPremiumCustomer();
    Order order = createOrder(1000.0);
    
    // When
    BigDecimal discount = discountService.calculateDiscount(customer, order);
    
    // Then
    assertEquals(BigDecimal.valueOf(100.0), discount);
}

// 2. Run test (should fail)
// 3. Write minimal code to make it pass
// 4. Refactor and improve
```

---

## �️ Test Data Builder

**Create reusable test data with ServiceTestDataBuilder:**

```java
public class ServiceTestDataBuilder {
    
    // Request builders
    public static SaveRoleRequest validRoleRequest(String name) {
        SaveRoleRequest request = new SaveRoleRequest();
        request.setName(name);
        request.setDescription(name + " Description");
        request.setIsActive(true);
        return request;
    }
    
    // Entity builders (for mocking)
    public static Role activeRole(String name) {
        Role role = new Role();
        role.setId(1);
        role.setName(name);
        role.setIsActive(1);
        role.setCreatedOn(LocalDateTime.now());
        return role;
    }
    
    // Usage in tests
    @Test
    void shouldCreateRole() {
        // Given
        SaveRoleRequest request = ServiceTestDataBuilder.validRoleRequest("ADMIN");
        Role savedRole = ServiceTestDataBuilder.activeRole("ADMIN");
        when(roleRepo.save(any())).thenReturn(savedRole);
        
        // When
        RoleDto result = roleService.createRole(request);
        
        // Then
        assertThat(result.getName()).isEqualTo("ADMIN");
    }
}
```

## 📊 Coverage & Reports

### Coverage Commands
```bash
# Generate coverage report
.\mvnw.cmd test jacoco:report

# View coverage in browser
start target/site/jacoco/index.html

# Run specific service tests with coverage
.\mvnw.cmd test -Dtest=*Service*Test jacoco:report
```

### Coverage Targets
- **Service Layer**: 80%+ line coverage
- **Overall Project**: 60%+ (since we skip other layers)
- **Focus on**: Business logic methods, error handling, conditional logic

## 🐛 Common Issues & Fixes

### Issue: AWS Configuration Error
```bash
# Problem: Tests fail with "AWS security token expired"
# Fix: Run service tests only (these work)
.\mvnw.cmd test -Dtest=*Service*Test
```

### Issue: Test Not Found
```bash
# Problem: "No tests matching pattern"
# Fix: Use correct test class name
.\mvnw.cmd test -Dtest=RoleManagementServiceImplTest
```

### Issue: Low Coverage
```bash
# Check which methods need tests
.\mvnw.cmd test jacoco:report
start target/site/jacoco/com.itt.service.service/index.html
```

## ⚡ Best Practices

### 1. Test Naming
```java
// ✅ Good - Clear intent
@DisplayName("Should create role when valid request provided")
void shouldCreateRoleWhenValidRequest()

// ❌ Avoid - Unclear purpose  
void testCreateRole()
```

### 2. Test Structure
```java
@Test
void shouldDoSomething() {
    // Given - Setup test data
    YourRequest request = createValidRequest();
    
    // When - Execute business logic
    YourResponse result = service.process(request);
    
    // Then - Verify outcome
    assertThat(result).isNotNull();
    verify(repository).save(any());
}
```

### 3. Mock Only Dependencies
```java
// ✅ Mock repositories and external services
@Mock private YourRepository repository;
@Mock private ExternalService externalService;

// ❌ Don't mock the service under test
// @Mock private YourService yourService; // WRONG!
@InjectMocks private YourService yourService; // ✅ Correct
```

## 📚 Quick Reference

### Essential Annotations
- `@ExtendWith(MockitoExtension.class)` - Enable Mockito
- `@Mock` - Mock dependencies  
- `@InjectMocks` - Inject mocks into service
- `@Test` - Test method
- `@DisplayName("...")` - Readable test names

### Common Assertions (AssertJ)
```java
assertThat(result).isNotNull();
assertThat(result.getName()).isEqualTo("expected");
assertThat(result.isActive()).isTrue();
assertThat(list).hasSize(3);
assertThat(list).isEmpty();
```

### Common Mockito Methods
```java
when(repository.save(any())).thenReturn(entity);
when(repository.findById(1L)).thenReturn(Optional.of(entity));
verify(repository).save(any());
verifyNoInteractions(repository);
```

---
*Simplified Testing Guide - Focus on Service Layer Business Logic Only*
class RoleManagementServiceImplTest {

    @Mock private RoleRepository roleRepo;
    @Mock private MapCategoryFeaturePrivilegeRepository mapCatFeatPrivRepo;
    @Mock private MapRoleCategoryFeaturePrivilegeSkinRepository rolePrivSkinRepo;
    @Mock private MasterConfigRepository masterConfigRepo;

    @InjectMocks
    private RoleManagementServiceImpl roleManagementService;

    @Test
    @DisplayName("Should return privilege hierarchy when no role ID provided")
    void shouldReturnGlobalPrivilegeHierarchy() {
        // Given - Setup test data for global privilege hierarchy
        List<MapCategoryFeaturePrivilege> mockMappings = Arrays.asList(
            createMockMapping(1, "USER_MANAGEMENT", "CREATE_USER", "Create User"),
            createMockMapping(2, "ROLE_MANAGEMENT", "CREATE_ROLE", "Create Role"),
            createMockMapping(3, "USER_MANAGEMENT", "DELETE_USER", "Delete User")
        );
        when(mapCatFeatPrivRepo.findAllFull()).thenReturn(mockMappings);

        // When - Get global privilege hierarchy
        List<FeaturePrivilegeDTO> result = roleManagementService
            .getPrivilegeHierarchy(Optional.empty());

        // Then - Verify hierarchy structure and grouping
        assertNotNull(result);
        assertEquals(2, result.size()); // Two categories: USER_MANAGEMENT, ROLE_MANAGEMENT
        
        // Verify USER_MANAGEMENT category has 2 privileges
        FeaturePrivilegeDTO userMgmt = result.stream()
            .filter(fp -> "USER_MANAGEMENT".equals(fp.getCategoryName()))
            .findFirst()
            .orElseThrow();
        assertEquals(2, userMgmt.getPrivileges().size());
        
        // Verify ROLE_MANAGEMENT category has 1 privilege
        FeaturePrivilegeDTO roleMgmt = result.stream()
            .filter(fp -> "ROLE_MANAGEMENT".equals(fp.getCategoryName()))
            .findFirst()
            .orElseThrow();
        assertEquals(1, roleMgmt.getPrivileges().size());
        
        verify(mapCatFeatPrivRepo).findAllFull();
        verifyNoInteractions(rolePrivSkinRepo); // Should not query role-specific data
    }

    @Test
    @DisplayName("Should create new role with privileges successfully")
    void shouldCreateNewRoleWithPrivilegesSuccessfully() {
        // Given - Setup complete role creation scenario
        SaveRoleRequest request = createSaveRoleRequest("ADMIN", "Administrator Role");
        request.setPrivileges(Arrays.asList(
            createPrivilegeRequest(1, "DEFAULT"),
            createPrivilegeRequest(2, "CUSTOM")
        ));
        
        Role savedRole = createMockRole(1, "ADMIN", "Administrator Role");
        
        when(roleRepo.save(any(Role.class))).thenReturn(savedRole);
        doNothing().when(rolePrivSkinRepo).deleteByRole_Id(anyInt());
        doNothing().when(rolePrivSkinRepo).saveAll(any());

        // When - Create the role
        RoleDto result = roleManagementService.saveOrUpdateFull(request);

        // Then - Verify role creation and privilege assignment
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        assertEquals("Administrator Role", result.getDescription());
        assertTrue(result.isActive());
        
        // Verify repository interactions
        verify(roleRepo).save(any(Role.class));
        verify(rolePrivSkinRepo).deleteByRole_Id(1); // Cleanup existing privileges
        verify(rolePrivSkinRepo).saveAll(any()); // Save new privileges
    }

    @Test
    @DisplayName("Should update existing role and preserve audit fields")
    void shouldUpdateExistingRoleAndPreserveAuditFields() {
        // Given - Existing role update scenario
        SaveRoleRequest request = createSaveRoleRequest("UPDATED_ADMIN", "Updated Description");
        request.setId(1); // Update existing role
        
        Role existingRole = createMockRole(1, "ADMIN", "Old Description");
        existingRole.setCreatedOn(LocalDateTime.now().minusDays(30));
        existingRole.setCreatedById("ORIGINAL_USER");
        
        Role updatedRole = createMockRole(1, "UPDATED_ADMIN", "Updated Description");
        updatedRole.setCreatedOn(existingRole.getCreatedOn()); // Preserve original
        updatedRole.setCreatedById(existingRole.getCreatedById()); // Preserve original
        
        when(roleRepo.save(any(Role.class))).thenReturn(updatedRole);

        // When - Update the role
        RoleDto result = roleManagementService.saveOrUpdateFull(request);

        // Then - Verify update and audit preservation
        assertEquals("UPDATED_ADMIN", result.getName());
        assertEquals("Updated Description", result.getDescription());
        
        // Verify save was called with proper audit handling
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepo).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();
        assertEquals(1, capturedRole.getId()); // ID preserved
        assertEquals("ORIGINAL_USER", capturedRole.getCreatedById()); // Audit preserved
    }

    @Test
    @DisplayName("Should throw exception when role name already exists")
    void shouldThrowExceptionWhenRoleNameAlreadyExists() {
        // Given - Duplicate role name scenario
        SaveRoleRequest request = createSaveRoleRequest("EXISTING_ROLE", "Description");
        when(roleRepo.existsByNameAndIdNot(eq("EXISTING_ROLE"), any()))
            .thenReturn(true);

        // When & Then - Verify validation exception
        BusinessValidationException exception = assertThrows(
            BusinessValidationException.class,
            () -> roleManagementService.validateRoleUniqueness(request)
        );
        
        assertEquals("Role name 'EXISTING_ROLE' already exists", exception.getMessage());
        verify(roleRepo).existsByNameAndIdNot("EXISTING_ROLE", request.getId());
        verifyNoMoreInteractions(roleRepo); // Save should not be called
    }

    @Test
    @DisplayName("Should return landing pages with proper sorting")
    void shouldReturnLandingPagesWithProperSorting() {
        // Given - Mock landing page configuration
        List<MasterConfig> mockConfigs = Arrays.asList(
            createMockConfig("LANDING_PAGE", "REPORTS", "Reports", 2),
            createMockConfig("LANDING_PAGE", "DASHBOARD", "Dashboard", 1),
            createMockConfig("LANDING_PAGE", "SETTINGS", "Settings", 3)
        );
        when(masterConfigRepo.findByConfigTypeOrderBySortOrderAsc("LANDING_PAGE"))
            .thenReturn(mockConfigs);

        // When - Get landing pages
        List<LandingPageDto> result = roleManagementService.getLandingPages();

        // Then - Verify sorting and mapping
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify correct sorting by sort order
        assertEquals("DASHBOARD", result.get(0).getCode()); // Sort order 1
        assertEquals("REPORTS", result.get(1).getCode());   // Sort order 2
        assertEquals("SETTINGS", result.get(2).getCode());  // Sort order 3
        
        verify(masterConfigRepo).findByConfigTypeOrderBySortOrderAsc("LANDING_PAGE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("Should reject blank role names")
    void shouldRejectBlankRoleNames(String blankName) {
        // Given - Various blank name scenarios
        SaveRoleRequest request = createSaveRoleRequest(blankName, "Description");

        // When & Then - Verify all blank variations are rejected
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> roleManagementService.validateRoleName(request.getName())
        );
        
        assertEquals("Role name cannot be blank or empty", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "1, true, ACTIVE",
        "0, false, INACTIVE",
        "-1, false, INACTIVE"
    })
    @DisplayName("Should map role status correctly based on isActive value")
    void shouldMapRoleStatusCorrectly(int isActiveValue, boolean expectedActive, String expectedStatus) {
        // Given - Role with different isActive values
        Role role = createMockRole(1, "TEST_ROLE", "Description");
        role.setIsActive(isActiveValue);

        // When - Map to DTO
        RoleDto result = roleManagementService.mapRoleToDto(role);

        // Then - Verify status mapping
        assertEquals(expectedActive, result.isActive());
        assertEquals(expectedStatus, result.getStatus());
    }

    // Helper methods for creating comprehensive test data
    private SaveRoleRequest createSaveRoleRequest(String name, String description) {
        SaveRoleRequest request = new SaveRoleRequest();
        request.setName(name);
        request.setDescription(description);
        request.setIsActive(true);
        request.setPrivileges(new ArrayList<>());
        return request;
    }

    private PrivilegeRequest createPrivilegeRequest(Integer privilegeId, String skinGroup) {
        PrivilegeRequest privilege = new PrivilegeRequest();
        privilege.setPrivilegeId(privilegeId);
        privilege.setSkinGroup(skinGroup);
        return privilege;
    }

    private Role createMockRole(Integer id, String name, String description) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setDescription(description);
        role.setIsActive(1);
        role.setCreatedOn(LocalDateTime.now());
        role.setCreatedById("SYSTEM");
        role.setUpdatedOn(LocalDateTime.now());
        role.setUpdatedById("SYSTEM");
        return role;
    }

    private MapCategoryFeaturePrivilege createMockMapping(int id, String category, String feature, String privilege) {
        MapCategoryFeaturePrivilege mapping = new MapCategoryFeaturePrivilege();
        mapping.setId(id);
        mapping.setCategoryName(category);
        mapping.setFeatureName(feature);
        mapping.setPrivilegeName(privilege);
        return mapping;
    }

    private MasterConfig createMockConfig(String type, String code, String description, int sortOrder) {
        MasterConfig config = new MasterConfig();
        config.setConfigType(type);
        config.setConfigCode(code);
        config.setConfigDescription(description);
        config.setSortOrder(sortOrder);
        return config;
    }
}
```

### Service Test - CustomerSubscriptionService (Simplified Example)

**Template for Simple Service Testing:**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Subscription Service - Business Logic Tests")
class CustomerSubscriptionServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private CustomerSubscriptionService subscriptionService;

    @Test
    @DisplayName("Should create subscription when customer exists and plan is valid")
    void shouldCreateSubscriptionWhenCustomerExistsAndPlanIsValid() {
        // Given
        Long customerId = 1L;
        String planCode = "PREMIUM";
        Customer existingCustomer = createMockCustomer(customerId, "john@example.com");
        Subscription savedSubscription = createMockSubscription(customerId, planCode);
        
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);
        doNothing().when(notificationService).sendWelcomeEmail(any());

        // When
        SubscriptionDto result = subscriptionService.createSubscription(customerId, planCode);

        // Then
        assertNotNull(result);
        assertEquals(planCode, result.getPlanCode());
        assertEquals(customerId, result.getCustomerId());
        assertTrue(result.isActive());
        
        verify(customerRepository).findById(customerId);
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(notificationService).sendWelcomeEmail(existingCustomer);
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        Long nonExistentCustomerId = 999L;
        when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
            CustomerNotFoundException.class,
            () -> subscriptionService.createSubscription(nonExistentCustomerId, "PREMIUM")
        );
        
        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(customerRepository).findById(nonExistentCustomerId);
        verifyNoInteractions(subscriptionRepository, notificationService);
    }

    @Test
    @DisplayName("Should calculate prorated amount for mid-month subscription")
    void shouldCalculateProRatedAmountForMidMonthSubscription() {
        // Given
        LocalDate subscriptionDate = LocalDate.of(2024, 1, 15); // Mid-month
        BigDecimal monthlyAmount = new BigDecimal("100.00");
        int daysInMonth = 31;
        int remainingDays = 17; // From 15th to end of month

        // When
        BigDecimal proRatedAmount = subscriptionService.calculateProRatedAmount(
            subscriptionDate, monthlyAmount);

        // Then
        BigDecimal expectedAmount = monthlyAmount
            .multiply(BigDecimal.valueOf(remainingDays))
            .divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);
        
        assertEquals(expectedAmount, proRatedAmount);
    }

    // Helper methods
    private Customer createMockCustomer(Long id, String email) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setEmail(email);
        customer.setActive(true);
        return customer;
    }

    private Subscription createMockSubscription(Long customerId, String planCode) {
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setCustomerId(customerId);
        subscription.setPlanCode(planCode);
        subscription.setActive(true);
        subscription.setStartDate(LocalDate.now());
        return subscription;
    }
}
```

---

## 🏗️ Test Data Builders - Service Layer Focus

### **Service Test Data Creation Helpers**

**Create a ServiceTestDataBuilder utility class:**

```java
public class ServiceTestDataBuilder {
    
    // Service Request builders
    public static SaveRoleRequest.SaveRoleRequestBuilder aRoleRequest() {
        return SaveRoleRequest.builder()
            .name("DEFAULT_ROLE")
            .description("Default Role Description")
            .isActive(true)
            .privileges(new ArrayList<>());
    }
    
    public static SaveRoleRequest validRoleRequest(String name) {
        return aRoleRequest()
            .name(name)
            .description(name + " Description")
            .build();
    }
    
    public static SaveRoleRequest invalidRoleRequest() {
        return aRoleRequest()
            .name(null) // Invalid - null name
            .description("")
            .build();
    }
    
    // Entity builders for mocking repository responses
    public static Role.RoleBuilder aRole() {
        return Role.builder()
            .name("DEFAULT_ROLE")
            .description("Default Role Description")
            .isActive(1)
            .createdOn(LocalDateTime.now())
            .createdById("SYSTEM")
            .updatedOn(LocalDateTime.now())
            .updatedById("SYSTEM");
    }
    
    public static Role activeRole(String name) {
        return aRole()
            .name(name)
            .description(name + " Description")
            .isActive(1)
            .build();
    }
    
    public static Role inactiveRole(String name) {
        return aRole()
            .name(name)
            .description(name + " Description")
            .isActive(0)
            .build();
    }
    
    // Service Response builders for verification
    public static RoleDto.RoleDtoBuilder aRoleDto() {
        return RoleDto.builder()
            .name("DEFAULT_ROLE")
            .description("Default Role Description")
            .active(true);
    }
    
    public static RoleDto validRoleDto(String name) {
        return aRoleDto()
            .name(name)
            .description(name + " Description")
            .build();
    }
    
    // User-related builders for other services
    public static User.UserBuilder aUser() {
        return User.builder()
            .name("Test User")
            .email("test@example.com")
            .active(true)
            .createdOn(LocalDateTime.now())
            .createdById("SYSTEM");
    }
    
    public static User activeUser(String email) {
        return aUser()
            .email(email)
            .name(email.split("@")[0])
            .active(true)
            .build();
    }
    
    // Subscription builders for subscription service tests
    public static CreateSubscriptionRequest.CreateSubscriptionRequestBuilder aSubscriptionRequest() {
        return CreateSubscriptionRequest.builder()
            .customerId(1L)
            .planCode("BASIC")
            .startDate(LocalDate.now())
            .autoRenew(true);
    }
    
    public static CreateSubscriptionRequest validSubscriptionRequest(Long customerId, String planCode) {
        return aSubscriptionRequest()
            .customerId(customerId)
            .planCode(planCode)
            .build();
    }
    
    // Privilege builders for role management tests
    public static MapCategoryFeaturePrivilege aPrivilege() {
        MapCategoryFeaturePrivilege privilege = new MapCategoryFeaturePrivilege();
        privilege.setId(1);
        privilege.setCategoryName("DEFAULT_CATEGORY");
        privilege.setFeatureName("DEFAULT_FEATURE");
        privilege.setPrivilegeName("DEFAULT_PRIVILEGE");
        privilege.setCreatedOn(LocalDateTime.now());
        privilege.setCreatedById("SYSTEM");
        return privilege;
    }
    
    public static MapCategoryFeaturePrivilege privilege(String category, String feature, String privilege) {
        MapCategoryFeaturePrivilege mapping = aPrivilege();
        mapping.setCategoryName(category);
        mapping.setFeatureName(feature);
        mapping.setPrivilegeName(privilege);
        return mapping;
    }
    
    // Configuration builders for configuration-dependent services
    public static MasterConfig aMasterConfig() {
        MasterConfig config = new MasterConfig();
        config.setConfigType("DEFAULT_TYPE");
        config.setConfigCode("DEFAULT_CODE");
        config.setConfigDescription("Default Description");
        config.setSortOrder(1);
        config.setActive(true);
        return config;
    }
    
    public static MasterConfig landingPageConfig(String code, String description, int sortOrder) {
        MasterConfig config = aMasterConfig();
        config.setConfigType("LANDING_PAGE");
        config.setConfigCode(code);
        config.setConfigDescription(description);
        config.setSortOrder(sortOrder);
        return config;
    }
    
    // List builders for bulk operations
    public static List<MapCategoryFeaturePrivilege> multiplePrivileges() {
        return Arrays.asList(
            privilege("USER_MANAGEMENT", "CREATE_USER", "Create User"),
            privilege("USER_MANAGEMENT", "UPDATE_USER", "Update User"),
            privilege("ROLE_MANAGEMENT", "CREATE_ROLE", "Create Role"),
            privilege("ROLE_MANAGEMENT", "UPDATE_ROLE", "Update Role")
        );
    }
    
    public static List<Role> multipleRoles() {
        return Arrays.asList(
            activeRole("ADMIN"),
            activeRole("USER"),
            inactiveRole("GUEST")
        );
    }
}
```

### **Using Test Data Builders in Service Tests**

**Quick Service Test Data Creation:**

```java
@ExtendWith(MockitoExtension.class)
class RoleManagementServiceTest {

    @Mock private RoleRepository roleRepository;
    @InjectMocks private RoleManagementService roleService;

    @Test
    void shouldCreateActiveRole() {
        // Given - Super quick test data creation
        SaveRoleRequest request = ServiceTestDataBuilder.validRoleRequest("ADMIN");
        Role savedRole = ServiceTestDataBuilder.activeRole("ADMIN");
        
        when(roleRepository.save(any())).thenReturn(savedRole);
        
        // When
        RoleDto result = roleService.createRole(request);
        
        // Then
        assertEquals("ADMIN", result.getName());
        assertTrue(result.isActive());
    }

    @Test
    void shouldHandleMultiplePrivileges() {
        // Given - Easy list creation
        List<MapCategoryFeaturePrivilege> privileges = ServiceTestDataBuilder.multiplePrivileges();
        when(privilegeRepository.findAll()).thenReturn(privileges);
        
        // When
        List<FeaturePrivilegeDTO> result = roleService.getPrivilegeHierarchy(Optional.empty());
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // USER_MANAGEMENT and ROLE_MANAGEMENT categories
    }
    
    @Test
    void shouldRejectInvalidRequest() {
        // Given - Quick invalid data creation
        SaveRoleRequest invalidRequest = ServiceTestDataBuilder.invalidRoleRequest();
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> roleService.createRole(invalidRequest));
    }
}
```

### **Advanced Service Test Data Patterns**

**Scenario-Based Builders:**

```java
public class ServiceTestScenarios {
    
    // Role management scenarios
    public static class RoleScenarios {
        
        public static SaveRoleRequest newAdminRoleRequest() {
            return ServiceTestDataBuilder.aRoleRequest()
                .name("ADMIN")
                .description("System Administrator")
                .isActive(true)
                .privileges(adminPrivileges())
                .build();
        }
        
        public static SaveRoleRequest updateExistingRoleRequest(Integer roleId) {
            return ServiceTestDataBuilder.aRoleRequest()
                .id(roleId)
                .name("UPDATED_ROLE")
                .description("Updated role description")
                .isActive(true)
                .build();
        }
        
        public static SaveRoleRequest duplicateNameRoleRequest() {
            return ServiceTestDataBuilder.aRoleRequest()
                .name("EXISTING_ROLE") // This name already exists
                .build();
        }
        
        private static List<PrivilegeRequest> adminPrivileges() {
            return Arrays.asList(
                createPrivilegeRequest(1, "ALL"),
                createPrivilegeRequest(2, "ALL"),
                createPrivilegeRequest(3, "ALL")
            );
        }
        
        private static PrivilegeRequest createPrivilegeRequest(Integer id, String skinGroup) {
            PrivilegeRequest privilege = new PrivilegeRequest();
            privilege.setPrivilegeId(id);
            privilege.setSkinGroup(skinGroup);
            return privilege;
        }
    }
    
    // User management scenarios
    public static class UserScenarios {
        
        public static CreateUserRequest newUserRequest() {
            return CreateUserRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .active(true)
                .roleIds(Arrays.asList(1, 2))
                .build();
        }
        
        public static CreateUserRequest existingEmailRequest() {
            return CreateUserRequest.builder()
                .name("Jane Doe")
                .email("existing@example.com") // Email already in use
                .active(true)
                .build();
        }
        
        public static User existingUser(String email) {
            return ServiceTestDataBuilder.aUser()
                .email(email)
                .name(email.split("@")[0])
                .createdOn(LocalDateTime.now().minusDays(30))
                .build();
        }
    }
    
    // Error scenarios
    public static class ErrorScenarios {
        
        public static void setupRepositoryError(RoleRepository mockRepo) {
            when(mockRepo.save(any())).thenThrow(new DataAccessException("Database connection failed"));
        }
        
        public static void setupValidationError(ValidationService mockValidator) {
            when(mockValidator.validateRole(any())).thenThrow(new ValidationException("Invalid role data"));
        }
        
        public static void setupNotFoundScenario(RoleRepository mockRepo, Integer roleId) {
            when(mockRepo.findById(roleId)).thenReturn(Optional.empty());
        }
    }
}
```

**Usage in Complex Service Tests:**

```java
@Test
@DisplayName("Should handle complete role creation workflow")
void shouldHandleCompleteRoleCreationWorkflow() {
    // Given - Complex scenario setup with builders
    SaveRoleRequest request = RoleScenarios.newAdminRoleRequest();
    Role savedRole = ServiceTestDataBuilder.activeRole("ADMIN");
    List<MapCategoryFeaturePrivilege> allPrivileges = ServiceTestDataBuilder.multiplePrivileges();
    
    when(roleRepository.save(any())).thenReturn(savedRole);
    when(privilegeRepository.findByIdIn(any())).thenReturn(allPrivileges);
    doNothing().when(rolePrivilegeRepository).saveAll(any());
    
    // When
    RoleDto result = roleService.createRoleWithPrivileges(request);
    
    // Then
    assertNotNull(result);
    assertEquals("ADMIN", result.getName());
    assertTrue(result.isActive());
    verify(roleRepository).save(any());
    verify(rolePrivilegeRepository).saveAll(any());
}

@Test
@DisplayName("Should handle duplicate role name scenario")
void shouldHandleDuplicateRoleNameScenario() {
    // Given - Error scenario with builder
    SaveRoleRequest request = RoleScenarios.duplicateNameRoleRequest();
    when(roleRepository.existsByName("EXISTING_ROLE")).thenReturn(true);
    
    // When & Then
    assertThrows(DuplicateRoleException.class, 
        () -> roleService.createRole(request));
    
    verify(roleRepository).existsByName("EXISTING_ROLE");
    verifyNoMoreInteractions(roleRepository);
}
```

### **Service Test Data Best Practices**

**1. Keep Builders Simple and Focused:**
```java
// ✅ Good - focused on single concern
public static SaveRoleRequest basicRoleRequest(String name) {
    return SaveRoleRequest.builder()
        .name(name)
        .description(name + " Description")
        .isActive(true)
        .build();
}

// ❌ Avoid - too many optional parameters
public static SaveRoleRequest complexRoleRequest(String name, String desc, Boolean active, 
    List<PrivilegeRequest> privs, Integer id, String createdBy) {
    // Too complex for most tests
}
```

**2. Use Method Chaining for Variations:**
```java
@Test
void testDifferentRoleStates() {
    // Easy variations with method chaining
    Role activeRole = ServiceTestDataBuilder.aRole().name("ACTIVE").isActive(1).build();
    Role inactiveRole = ServiceTestDataBuilder.aRole().name("INACTIVE").isActive(0).build();
    Role adminRole = ServiceTestDataBuilder.aRole().name("ADMIN").description("Administrator").build();
}
```

**3. Create Domain-Specific Builders:**
```java
// Domain-specific builders for business contexts
public static SaveRoleRequest supervisorRoleRequest() {
    return aRoleRequest()
        .name("SUPERVISOR")
        .description("Department Supervisor")
        .privileges(supervisorPrivileges())
        .build();
}

public static SaveRoleRequest customerSupportRoleRequest() {
    return aRoleRequest()
        .name("CUSTOMER_SUPPORT")  
        .description("Customer Support Representative")
        .privileges(customerSupportPrivileges())
        .build();
}
```

---

## ✍️ Writing Test Cases - Service Layer Focus

### **Service Layer Tests - The ONLY Focus**

**Template for Service Business Logic Testing:**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Your Service - Business Logic Tests")
class YourServiceTest {

    // Mock dependencies (repositories and other services only)
    @Mock private YourRepository yourRepository;
    @Mock private ExternalService externalService;
    @Mock private AnotherInternalService anotherService;

    // Service under test
    @InjectMocks
    private YourService yourService;

    @Test
    @DisplayName("Should process request successfully when valid input provided")
    void shouldProcessRequestSuccessfullyWhenValidInput() {
        // Given (Arrange) - Setup test data and mocks
        YourRequest request = createValidRequest("TEST_DATA");
        YourEntity expectedEntity = createMockEntity("TEST_DATA");
        when(yourRepository.save(any(YourEntity.class))).thenReturn(expectedEntity);
        when(externalService.validate(any())).thenReturn(true);

        // When (Act) - Execute the business logic
        YourResponse result = yourService.processRequest(request);

        // Then (Assert) - Verify the outcome
        assertNotNull(result);
        assertEquals("TEST_DATA", result.getName());
        assertTrue(result.isSuccess());
        
        // Verify interactions with dependencies
        verify(yourRepository).save(any(YourEntity.class));
        verify(externalService).validate(request);
        verifyNoMoreInteractions(yourRepository, externalService);
    }

    @Test
    @DisplayName("Should throw BusinessException when invalid input provided")
    void shouldThrowBusinessExceptionWhenInvalidInput() {
        // Given - Setup invalid condition
        YourRequest invalidRequest = createInvalidRequest();
        when(externalService.validate(any())).thenReturn(false);

        // When & Then - Verify exception is thrown
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> yourService.processRequest(invalidRequest));
        
        assertEquals("Invalid request data", exception.getMessage());
        
        // Verify that save was not called due to validation failure
        verify(externalService).validate(invalidRequest);
        verifyNoInteractions(yourRepository);
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void shouldHandleRepositoryExceptionGracefully() {
        // Given - Setup repository to throw exception
        YourRequest request = createValidRequest("TEST_DATA");
        when(yourRepository.save(any())).thenThrow(new DataAccessException("DB Error"));

        // When & Then - Verify service handles exception properly
        ServiceException exception = assertThrows(ServiceException.class,
            () -> yourService.processRequest(request));
        
        assertEquals("Failed to save data", exception.getMessage());
        assertTrue(exception.getCause() instanceof DataAccessException);
    }

    @Test
    @DisplayName("Should return empty result when no data found")
    void shouldReturnEmptyResultWhenNoDataFound() {
        // Given - Setup empty repository response
        String searchTerm = "NON_EXISTENT";
        when(yourRepository.findByName(searchTerm)).thenReturn(Optional.empty());

        // When
        Optional<YourResponse> result = yourService.findByName(searchTerm);

        // Then
        assertFalse(result.isPresent());
        verify(yourRepository).findByName(searchTerm);
    }

    // Helper methods for creating test data
    private YourRequest createValidRequest(String name) {
        YourRequest request = new YourRequest();
        request.setName(name);
        request.setDescription("Test description");
        request.setActive(true);
        return request;
    }

    private YourRequest createInvalidRequest() {
        YourRequest request = new YourRequest();
        request.setName(null);  // Invalid - name is required
        return request;
    }

    private YourEntity createMockEntity(String name) {
        YourEntity entity = new YourEntity();
        entity.setId(1L);
        entity.setName(name);
        entity.setDescription("Test description");
        entity.setCreatedOn(LocalDateTime.now());
        entity.setCreatedBy("SYSTEM");
        return entity;
    }
}
```

### **Key Testing Patterns for Services**

#### **1. Happy Path Testing**
```java
@Test
@DisplayName("Should create user when all validation passes")
void shouldCreateUserWhenAllValidationPasses() {
    // Given - Valid input and successful dependencies
    CreateUserRequest request = validUserRequest();
    User savedUser = mockUser();
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    
    // When - Execute business operation
    UserDto result = userService.createUser(request);
    
    // Then - Verify successful outcome
    assertNotNull(result);
    assertEquals(request.getEmail(), result.getEmail());
    verify(userRepository).existsByEmail(request.getEmail());
    verify(userRepository).save(any(User.class));
}
```

#### **2. Validation Error Testing**
```java
@Test
@DisplayName("Should throw ValidationException when email already exists")
void shouldThrowValidationExceptionWhenEmailAlreadyExists() {
    // Given - Duplicate email condition
    CreateUserRequest request = validUserRequest();
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
    
    // When & Then - Verify validation failure
    ValidationException exception = assertThrows(ValidationException.class,
        () -> userService.createUser(request));
    
    assertEquals("Email already exists", exception.getMessage());
    verify(userRepository).existsByEmail(request.getEmail());
    verifyNoMoreInteractions(userRepository); // Save should not be called
}
```

#### **3. Business Rule Testing**
```java
@Test
@DisplayName("Should apply discount when user is premium member")
void shouldApplyDiscountWhenUserIsPremiumMember() {
    // Given - Premium user and order
    User premiumUser = createPremiumUser();
    Order order = createOrder(1000.0);
    when(discountService.calculateDiscount(premiumUser, order)).thenReturn(100.0);
    
    // When - Apply business rule
    OrderTotal total = orderService.calculateTotal(premiumUser, order);
    
    // Then - Verify discount applied
    assertEquals(900.0, total.getFinalAmount());
    assertEquals(100.0, total.getDiscountAmount());
    verify(discountService).calculateDiscount(premiumUser, order);
}
```

#### **4. Edge Case Testing**
```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "   ", "\t", "\n"})
@DisplayName("Should reject blank or whitespace-only names")
void shouldRejectBlankOrWhitespaceOnlyNames(String invalidName) {
    // Given - Invalid name variations
    CreateRoleRequest request = new CreateRoleRequest();
    request.setName(invalidName);
    
    // When & Then - Verify all variations are rejected
    ValidationException exception = assertThrows(ValidationException.class,
        () -> roleService.createRole(request));
    
    assertEquals("Role name cannot be blank", exception.getMessage());
}

@ParameterizedTest
@CsvSource({
    "1, ACTIVE, true",
    "0, INACTIVE, false",
    "-1, INACTIVE, false"
})
@DisplayName("Should map status codes correctly")
void shouldMapStatusCodesCorrectly(int statusCode, String expectedStatus, boolean expectedActive) {
    // Given - Different status codes
    Role role = new Role();
    role.setIsActive(statusCode);
    
    // When - Map to DTO
    RoleDto result = roleService.mapToDto(role);
    
    // Then - Verify correct mapping
    assertEquals(expectedStatus, result.getStatus());
    assertEquals(expectedActive, result.isActive());
}
```

### **What NOT to Test in Services**

#### **❌ Don't Test Framework/Library Code**
```java
// ❌ DON'T test Spring's @Transactional behavior
@Test
void shouldRollbackTransactionOnException() {
    // Spring handles transaction management - don't test it
}

// ❌ DON'T test JPA repository basic operations
@Test
void shouldSaveEntityToDatabase() {
    // Spring Data JPA handles save() - don't test it
}
```

#### **❌ Don't Test Simple Mappings Without Logic**
```java
// ❌ DON'T test simple getter/setter mappings
@Test
void shouldMapEntityToDto() {
    // If it's just copying fields, no need to test
    Entity entity = new Entity();
    entity.setName("Test");
    
    Dto dto = mapper.toDto(entity);
    assertEquals("Test", dto.getName()); // No business logic here
}
```

#### **❌ Don't Test External Service Implementations**
```java
// ❌ DON'T test what external services do
@Test
void shouldSendEmailCorrectly() {
    // Test that your service CALLS the email service
    // Don't test that the email service WORKS correctly
}
```

### **Service Test Best Practices**

#### **1. Test Data Creation**
```java
// ✅ Use builder pattern or factory methods
private CreateUserRequest validUserRequest() {
    return CreateUserRequest.builder()
        .name("John Doe")
        .email("john@example.com")
        .active(true)
        .build();
}

// ✅ Create focused test data for each scenario
private User createPremiumUser() {
    User user = new User();
    user.setMembershipType(MembershipType.PREMIUM);
    user.setMemberSince(LocalDate.now().minusYears(1));
    return user;
}
```

#### **2. Clear Test Structure**
```java
@Test
@DisplayName("Should calculate shipping cost based on weight and distance")
void shouldCalculateShippingCostBasedOnWeightAndDistance() {
    // Given - Clear setup with meaningful variable names
    double packageWeight = 5.0; // kg
    double deliveryDistance = 100.0; // km
    ShippingRequest request = new ShippingRequest(packageWeight, deliveryDistance);
    
    // When - Single action being tested
    ShippingCost result = shippingService.calculateCost(request);
    
    // Then - Clear assertions about the outcome
    assertNotNull(result);
    assertEquals(25.0, result.getAmount()); // 5kg * 100km * $0.05/kg/km
    assertEquals("STANDARD", result.getServiceType());
}
```

#### **3. Verify Business Logic, Not Implementation**
```java
// ✅ Test WHAT the service does (business outcome)
@Test
void shouldCreateActiveUserByDefault() {
    CreateUserRequest request = validUserRequest();
    when(userRepository.save(any())).thenReturn(mockActiveUser());
    
    UserDto result = userService.createUser(request);
    
    assertTrue(result.isActive()); // Testing business rule
}

// ❌ Don't test HOW the service does it (implementation details)
@Test
void shouldCallRepositorySaveMethod() {
    // This tests implementation, not business value
    verify(userRepository).save(any()); // Too focused on internals
}
```

---

## 🔧 Running Tests

### Basic Test Execution

```bash
# Run all tests
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=ClassName

# Run specific test method
.\mvnw.cmd test -Dtest=ClassName#methodName

# Run tests with pattern
.\mvnw.cmd test -Dtest="*Service*"

# Run tests with profile
.\mvnw.cmd test -Dspring.profiles.active=test
```

### Verbose Testing (for debugging)

```bash
# Run with detailed output
.\mvnw.cmd test -X

# Run with debug logging
.\mvnw.cmd test -Dlogging.level.org.springframework.test=DEBUG

# Run with Mockito debugging
.\mvnw.cmd test -Dmockito.verboseLogging=true
```

### Running Tests in IDE

**IntelliJ IDEA:**
- Right-click test class → Run 'TestClassName'
- Right-click test method → Run 'methodName'
- Ctrl+Shift+F10 to run current test

**VS Code:**
- Use Java Test Runner extension
- Click "Run Test" above test methods
- Use Command Palette: "Java: Run Tests"

---

## 🐛 Debugging Tests

### Common Issues and Solutions

#### 1. Mock Not Working

**Problem:** Mock returns null instead of expected value

```java
// ❌ Common mistake
@Test
void test() {
    when(repository.findById(1L)).thenReturn(Optional.of(new Entity()));
    // Mock returns null
}
```

**Solution:** Add proper annotations

```java
// ✅ Correct approach
@ExtendWith(MockitoExtension.class)  // Don't forget this!
class MyTest {
    @Mock private MyRepository repository;
    @InjectMocks private MyService service;  // This injects the mock
    
    @Test
    void test() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Entity()));
        // Now mock works correctly
    }
}
```

#### 2. Spring Context Issues

**Problem:** Tests fail with bean creation errors

```java
// ❌ Problem
@SpringBootTest  // Tries to load full context
class MyTest {
    // Context loading fails due to AWS/DB issues
}
```

**Solution:** Use specific test annotations

```java
// ✅ Solution
@WebMvcTest(MyController.class)  // Only web layer
// OR
@DataJpaTest  // Only JPA layer
// OR
@ExtendWith(MockitoExtension.class)  // Pure unit test
class MyTest {
    // Loads minimal context
}
```

#### 3. Test Data Issues

**Problem:** Tests fail with constraint violations

```java
// ❌ Incomplete test data
@Test
void test() {
    Entity entity = new Entity();
    entity.setName("Test");  // Missing required fields
    repository.save(entity);  // Fails with constraint violation
}
```

**Solution:** Create complete test data

```java
// ✅ Complete test data
@Test
void test() {
    Entity entity = new Entity();
    entity.setName("Test");
    entity.setCreatedOn(LocalDateTime.now());
    entity.setCreatedById("SYSTEM");
    entity.setActive(true);  // Set ALL required fields
    repository.save(entity);
}
```

#### 4. Date/Time Issues

**Problem:** Tests fail due to timing issues

```java
// ❌ Flaky test
@Test
void test() {
    entity.setCreatedOn(LocalDateTime.now());
    assertEquals(LocalDateTime.now(), entity.getCreatedOn());  // Can fail!
}
```

**Solution:** Use time ranges or fixed dates

```java
// ✅ Reliable test
@Test
void test() {
    LocalDateTime before = LocalDateTime.now();
    entity.setCreatedOn(LocalDateTime.now());
    LocalDateTime after = LocalDateTime.now();
    
    assertTrue(entity.getCreatedOn().isAfter(before));
    assertTrue(entity.getCreatedOn().isBefore(after));
}
```

### Debugging Techniques

#### 1. Add Debug Output

```java
@Test
void debugTest() {
    System.out.println("Starting test");
    
    YourEntity entity = service.findById(1L);
    System.out.println("Found entity: " + entity);
    
    assertNotNull(entity);
}
```

#### 2. Verify Mock Interactions

```java
@Test
void verifyMockCalls() {
    service.performAction();
    
    // Check what was called
    verify(repository).findById(anyLong());
    verify(repository).save(any());
    
    // Check what wasn't called
    verify(repository, never()).deleteById(anyLong());
    
    // Check exact arguments
    ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
    verify(repository).save(captor.capture());
    assertEquals("Expected Name", captor.getValue().getName());
}
```

#### 3. Test Specific Scenarios

```java
@Test
void testErrorScenario() {
    // Setup error condition
    when(repository.findById(1L)).thenThrow(new RuntimeException("DB Error"));
    
    // Verify error handling
    assertThrows(ServiceException.class, () -> service.performAction(1L));
}
```

---

## 📊 Generating Reports

### Basic Report Generation

```bash
# Generate test report with coverage
.\mvnw.cmd test jacoco:report

# Report location
start target/site/jacoco/index.html

# Generate with coverage verification (enforces 80% minimum)
.\mvnw.cmd verify
```

### Report Types

1. **HTML Report** (for developers)
   - Location: `target/site/jacoco/index.html`
   - Interactive, class-by-class breakdown
   - Line-by-line coverage visualization
   - Color-coded coverage indicators

2. **XML Report** (for CI/CD)
   - Location: `target/site/jacoco/jacoco.xml`
   - Machine-readable format
   - Used by build systems and IDEs

3. **CSV Report** (for analysis)
   - Location: `target/site/jacoco/jacoco.csv`
   - Spreadsheet-compatible format
   - Good for coverage trend analysis

### Advanced Report Generation

**Package-Specific Reports:**
```bash
# Controller layer coverage
.\mvnw.cmd test jacoco:report -Dtest=**/controller/**/*Test

# Service layer coverage  
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test

# Repository layer coverage
.\mvnw.cmd test jacoco:report -Dtest=**/repository/**/*Test

# Role management component coverage
.\mvnw.cmd test jacoco:report -Dtest=**/*Role*Test
```

**Coverage by Test Type:**
```bash
# Unit tests coverage only
.\mvnw.cmd test jacoco:report -Dtest=**/unit/**/*Test

# Integration tests coverage
.\mvnw.cmd test jacoco:report -Dtest=**/integration/**/*Test

# API tests coverage
.\mvnw.cmd test jacoco:report -Dtest=**/api/**/*Test
```

### Report Analysis

**HTML Report Navigation:**
1. **Overview Page** - Shows overall project coverage percentage
2. **Package View** - Click package name to see class-level coverage
3. **Class View** - Click class name to see method and line coverage
4. **Source View** - Click line numbers to see exact coverage

**Color Coding:**
- 🟢 **Green**: Good coverage (80%+ for your project)
- 🟡 **Yellow**: Moderate coverage (50-79%)
- 🔴 **Red**: Low coverage (<50%)
- **Red bars**: Uncovered lines
- **Yellow bars**: Partially covered branches

**What to Look For:**
- Classes with low test coverage (below 80%)
- Methods never called during tests
- Branches (if/else) not covered
- Lines highlighted in red (not covered)
- Complex methods with low branch coverage

### Coverage Improvement Workflow

**Step 1: Identify Low Coverage Areas**
```bash
# Generate full report
.\mvnw.cmd test jacoco:report
start target/site/jacoco/index.html

# Look for red/yellow classes in the report
```

**Step 2: Focus on Specific Components**
```bash
# Test specific low-coverage class
.\mvnw.cmd test -Dtest=YourLowCoverageClassTest

# Check just that class coverage
.\mvnw.cmd test jacoco:report -Dtest=YourLowCoverageClassTest
```

**Step 3: Add Missing Tests**
1. Identify untested methods from report
2. Write tests for missing scenarios  
3. Test error conditions
4. Test edge cases (null, empty, boundary values)
5. Verify branch coverage for if/else statements

**Step 4: Verify Improvement**
```bash
# Re-run coverage for improved class
.\mvnw.cmd test jacoco:report -Dtest=YourImprovedClassTest

# Check overall project improvement
.\mvnw.cmd test jacoco:report
```

### Coverage Quality Guidelines

**Target Coverage by Layer:**
- **Controller Layer**: 85%+ (focus on endpoint coverage and error handling)
- **Service Layer**: 90%+ (focus on business logic and edge cases)
- **Repository Layer**: 80%+ (focus on custom queries and transactions)
- **Utility Classes**: 95%+ (focus on all utility methods)

**Coverage vs Quality Balance:**
- Don't just aim for numbers - test meaningful scenarios
- 80% with good edge case testing > 95% with only happy path
- Focus on business logic coverage over getter/setter coverage
- Test error conditions and exception handling

### Quick Coverage Commands Reference

```bash
# Essential commands for daily development
.\mvnw.cmd test jacoco:report                    # Full coverage report
.\mvnw.cmd verify                               # Test with coverage check
start target/site/jacoco/index.html           # View coverage report

# Package-specific coverage
.\mvnw.cmd test jacoco:report -Dtest=**/controller/**/*Test   # Controllers only
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test      # Services only

# Component-specific coverage
.\mvnw.cmd test jacoco:report -Dtest=**/*Role*Test            # Role management
.\mvnw.cmd test jacoco:report -Dtest=**/*User*Test            # User management

# Coverage troubleshooting
.\mvnw.cmd jacoco:check -X                      # Detailed coverage check info
.\mvnw.cmd test -Djacoco.skip=true             # Skip coverage temporarily
```

---

## 🏆 Best Practices - Service Layer Focus

### **1. Service Test Naming**

```java
// ❌ Bad names - unclear and generic
@Test void test1() { }
@Test void testRole() { }
@Test void roleTest() { }

// ✅ Good names - describe business behavior
@Test
@DisplayName("Should create role when valid role request provided")
void shouldCreateRoleWhenValidRoleRequestProvided() { }

@Test
@DisplayName("Should throw DuplicateRoleException when role name already exists")
void shouldThrowDuplicateRoleExceptionWhenRoleNameAlreadyExists() { }

@Test
@DisplayName("Should return privilege hierarchy grouped by category")
void shouldReturnPrivilegeHierarchyGroupedByCategory() { }
```

### **2. Service Test Structure (AAA Pattern)**

```java
@Test
@DisplayName("Should calculate subscription total with discount")
void shouldCalculateSubscriptionTotalWithDiscount() {
    // Arrange (Given) - Setup test scenario
    Customer premiumCustomer = ServiceTestDataBuilder.premiumCustomer();
    SubscriptionPlan plan = ServiceTestDataBuilder.monthlyPlan(100.0);
    when(discountService.calculateDiscount(premiumCustomer, plan)).thenReturn(10.0);
    
    // Act (When) - Execute business logic
    SubscriptionTotal total = subscriptionService.calculateTotal(premiumCustomer, plan);
    
    // Assert (Then) - Verify business outcome
    assertEquals(90.0, total.getAmount());
    assertEquals(10.0, total.getDiscountAmount());
    verify(discountService).calculateDiscount(premiumCustomer, plan);
}
```

### **3. Service Test Data Creation**

```java
// ✅ Use helper methods for readable test data creation
private SaveRoleRequest createValidRoleRequest(String name) {
    SaveRoleRequest request = new SaveRoleRequest();
    request.setName(name);
    request.setDescription(name + " role description");
    request.setIsActive(true);
    request.setPrivileges(createDefaultPrivileges());
    return request;
}

// ✅ Use builders for complex objects
private Role createMockRole(String name, boolean active) {
    return Role.builder()
        .name(name)
        .description(name + " description")
        .isActive(active ? 1 : 0)
        .createdOn(LocalDateTime.now())
        .createdById("SYSTEM")
        .build();
}

// ✅ Create domain-specific test data
private Customer createPremiumCustomer() {
    return Customer.builder()
        .membershipType(MembershipType.PREMIUM)
        .loyaltyPoints(1000)
        .memberSince(LocalDate.now().minusYears(2))
        .build();
}
```

### **4. Service Test Independence**

```java
// ❌ Tests depend on each other - Bad!
@Test void createRole() { 
    // Creates role that deleteRole depends on
    roleService.createRole(request);
}

@Test void deleteRole() { 
    // Fails if createRole doesn't run first
    roleService.deleteRole("ADMIN");
}

// ✅ Independent tests - Each test is self-contained
@BeforeEach
void setUp() {
    // Reset mocks for clean state
    Mockito.reset(roleRepository, privilegeRepository);
}

@Test void shouldCreateRole() { 
    // Given - Setup own test data
    SaveRoleRequest request = createValidRoleRequest("ADMIN");
    Role savedRole = createMockRole("ADMIN", true);
    when(roleRepository.save(any())).thenReturn(savedRole);
    
    // When & Then - Self-contained test
    RoleDto result = roleService.createRole(request);
    assertEquals("ADMIN", result.getName());
}

@Test void shouldDeleteRole() { 
    // Given - Setup own test data (independent of createRole test)
    Role existingRole = createMockRole("ADMIN", true);
    when(roleRepository.findById(1)).thenReturn(Optional.of(existingRole));
    
    // When & Then - Self-contained test
    assertDoesNotThrow(() -> roleService.deleteRole(1));
    verify(roleRepository).delete(existingRole);
}
```

### **5. Testing Service Business Logic Edge Cases**

```java
// Test multiple scenarios with parameterized tests
@ParameterizedTest
@ValueSource(strings = {"", " ", "   ", "\t", "\n", "null"})
@DisplayName("Should reject invalid role names")
void shouldRejectInvalidRoleNames(String invalidName) {
    // Handle null string explicitly
    String roleName = "null".equals(invalidName) ? null : invalidName;
    
    SaveRoleRequest request = createRoleRequest(roleName);
    
    ValidationException exception = assertThrows(ValidationException.class, 
        () -> roleService.validateRoleName(request));
    
    assertEquals("Role name cannot be blank or null", exception.getMessage());
}

// Test business rules with CSV source
@ParameterizedTest
@CsvSource({
    "BASIC, 1, true, ACTIVE",
    "PREMIUM, 1, true, ACTIVE", 
    "BASIC, 0, false, INACTIVE",
    "GUEST, -1, false, DISABLED"
})
@DisplayName("Should map subscription status correctly based on business rules")
void shouldMapSubscriptionStatusCorrectly(String planType, int statusCode, 
    boolean expectedActive, String expectedStatus) {
    
    // Given
    Subscription subscription = createSubscription(planType, statusCode);
    
    // When
    SubscriptionDto result = subscriptionService.mapToDto(subscription);
    
    // Then
    assertEquals(expectedActive, result.isActive());
    assertEquals(expectedStatus, result.getStatus());
}
```

### **6. Service Error Handling Testing**

```java
@Test
@DisplayName("Should handle repository exception gracefully")
void shouldHandleRepositoryExceptionGracefully() {
    // Given - Setup repository to throw exception
    SaveRoleRequest request = createValidRoleRequest("ADMIN");
    when(roleRepository.save(any())).thenThrow(new DataAccessException("Database connection failed"));
    
    // When & Then - Verify service handles exception properly
    ServiceException exception = assertThrows(ServiceException.class,
        () -> roleService.createRole(request));
    
    assertEquals("Failed to create role due to database error", exception.getMessage());
    assertTrue(exception.getCause() instanceof DataAccessException);
    
    // Verify cleanup or rollback behavior if applicable
    verify(auditService, never()).logRoleCreation(any()); // Should not log on failure
}

@Test
@DisplayName("Should validate business rules before processing")
void shouldValidateBusinessRulesBeforeProcessing() {
    // Given - Setup invalid business scenario
    Customer inactiveCustomer = createInactiveCustomer();
    SubscriptionRequest request = createSubscriptionRequest("PREMIUM");
    
    // When & Then - Verify business validation
    BusinessRuleException exception = assertThrows(BusinessRuleException.class,
        () -> subscriptionService.createSubscription(inactiveCustomer, request));
    
    assertEquals("Cannot create premium subscription for inactive customer", exception.getMessage());
    
    // Verify that downstream operations were not called
    verifyNoInteractions(paymentService, notificationService);
}
```

### **7. Service Mock Verification Best Practices**

```java
@Test
@DisplayName("Should orchestrate multiple services correctly")
void shouldOrchestrateMultipleServicesCorrectly() {
    // Given
    CreateUserRequest request = createUserRequest("john@example.com");
    User savedUser = createMockUser("john@example.com");
    Role defaultRole = createMockRole("USER", true);
    
    when(userRepository.save(any())).thenReturn(savedUser);
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(defaultRole));
    when(emailService.sendWelcomeEmail(any())).thenReturn(true);
    
    // When
    UserDto result = userService.createUserWithDefaultRole(request);
    
    // Then - Verify business outcome
    assertNotNull(result);
    assertEquals("john@example.com", result.getEmail());
    
    // Verify service orchestration in correct order
    InOrder inOrder = inOrder(userRepository, roleRepository, emailService, auditService);
    inOrder.verify(userRepository).save(any(User.class));
    inOrder.verify(roleRepository).findByName("USER");
    inOrder.verify(emailService).sendWelcomeEmail(savedUser);
    inOrder.verify(auditService).logUserCreation(savedUser);
    
    // Verify exact interactions
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User capturedUser = userCaptor.getValue();
    assertEquals("john@example.com", capturedUser.getEmail());
    assertTrue(capturedUser.isActive());
}
```

### **8. Service Test Performance and Clarity**

```java
// ✅ Good - Fast, focused service test
@Test
@DisplayName("Should calculate order total with multiple line items")
void shouldCalculateOrderTotalWithMultipleLineItems() {
    // Given - Minimal test data for the scenario
    List<OrderItem> items = Arrays.asList(
        createOrderItem("Product A", 10.0, 2),
        createOrderItem("Product B", 15.0, 1)
    );
    Order order = createOrderWithItems(items);
    
    // When - Direct service call
    OrderTotal total = orderService.calculateTotal(order);
    
    // Then - Simple, clear assertions
    assertEquals(35.0, total.getSubtotal());
    assertEquals(3.5, total.getTax()); // 10% tax
    assertEquals(38.5, total.getGrandTotal());
}

// ❌ Avoid - Slow, complex test with unnecessary setup
@Test
void complexOrderTest() {
    // Don't setup entire application context for service test
    // Don't test multiple unrelated scenarios in one test
    // Don't use Thread.sleep() or time-dependent assertions
}
```

### **9. Service Testing Anti-Patterns to Avoid**

```java
// ❌ Don't test framework functionality
@Test
void shouldSaveToDatabase() {
    // DON'T test that JPA saves to database
    Role role = new Role();
    roleRepository.save(role);
    // This tests Spring Data JPA, not your business logic
}

// ❌ Don't test implementation details
@Test  
void shouldCallRepositoryExactlyOnce() {
    // DON'T focus on HOW the service works internally
    roleService.createRole(request);
    verify(roleRepository, times(1)).save(any()); // Too implementation-focused
}

// ❌ Don't test simple mappings without business logic
@Test
void shouldMapEntityToDto() {
    // DON'T test simple field copying
    Role role = new Role();
    role.setName("ADMIN");
    
    RoleDto dto = roleService.mapToDto(role);
    assertEquals("ADMIN", dto.getName()); // No business value
}

// ✅ DO test business behavior
@Test
@DisplayName("Should apply business discount rules when mapping to customer DTO")
void shouldApplyBusinessDiscountRulesWhenMappingToCustomerDto() {
    // Test business logic in the mapping
    Customer premiumCustomer = createPremiumCustomer();
    when(loyaltyService.calculateDiscount(premiumCustomer)).thenReturn(0.15);
    
    CustomerDto result = customerService.mapToDto(premiumCustomer);
    
    assertEquals(15.0, result.getDiscountPercentage()); // Business rule testing
}
```

### **10. Service Test Documentation**

```java
/**
 * Tests for RoleManagementService business logic.
 * 
 * Focus Areas:
 * - Role creation and validation
 * - Privilege hierarchy management
 * - Business rule enforcement
 * - Error handling and edge cases
 * 
 * Not Covered (by design):
 * - HTTP request/response handling (controller layer)
 * - Database operations (repository layer)
 * - Configuration management (configuration layer)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Management Service - Business Logic Tests")
class RoleManagementServiceImplTest {
    
    @Test
    @DisplayName("Business Rule: Should prevent duplicate role names within same organization")
    void shouldPreventDuplicateRoleNamesWithinSameOrganization() {
        // Test documents the business rule being validated
    }
    
    @Test
    @DisplayName("Edge Case: Should handle empty privilege list gracefully")
    void shouldHandleEmptyPrivilegeListGracefully() {
        // Test documents edge case handling
    }
}
```

### **Key Service Testing Principles:**

1. **Focus on Business Behavior** - Test what the service does, not how it does it
2. **Test Edge Cases** - Null values, empty collections, boundary conditions
3. **Mock External Dependencies** - Repositories, external services, configuration
4. **Verify Business Rules** - Validation logic, calculations, transformations
5. **Keep Tests Fast** - No database, no network, no file system
6. **Make Tests Readable** - Clear naming, good structure, helpful comments
7. **Test Error Scenarios** - Exception handling, validation failures, rollback behavior

---

## 🚀 Quick Reference - Service Layer Focus

### **Most Used Commands**

```bash
# Service layer testing (our primary focus)
.\mvnw.cmd test -Dtest=**/service/**/*Test     # Run all service tests
.\mvnw.cmd test -Dtest=RoleManagementServiceImplTest    # Run specific service test
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test # Service coverage report
start target/site/jacoco/com.itt.service.service/index.html # View service coverage
```

### **Essential Service Test Annotations**

```java
// Test lifecycle
@BeforeEach, @AfterEach           // Setup/cleanup for each test
@BeforeAll, @AfterAll             // One-time setup/cleanup

// Test execution  
@Test                             // Mark method as test
@DisplayName("Description")       // Readable test description
@Disabled                         // Skip test temporarily

// Parameterized tests for business scenarios
@ParameterizedTest                // Test with multiple inputs
@ValueSource(strings = {"A", "B"}) // String parameter values
@CsvSource({"1,true", "0,false"}) // CSV parameter combinations

// Service testing specific
@ExtendWith(MockitoExtension.class) // Enable Mockito mocking
@Mock                             // Create mock dependency
@InjectMocks                      // Inject mocks into service under test
```

### **Key Service Test Assertions**

```java
// Basic business logic assertions
assertEquals(expected, actual)          // Verify business calculations
assertNotNull(object)                  // Verify service returns result
assertTrue(condition)                  // Verify business conditions
assertFalse(condition)                 // Verify negative business conditions

// Exception testing for business rules
assertThrows(BusinessException.class, () -> service.method()) // Verify business rule violations
assertDoesNotThrow(() -> service.method())                   // Verify valid operations

// Collection assertions for service responses
assertEquals(3, list.size())           // Verify collection size
assertTrue(list.contains(item))        // Verify item in collection
assertThat(list).hasSize(3)           // Fluent assertion style
```

### **Service Test Template (Copy-Paste Ready)**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("[ServiceName] - Business Logic Tests")
class [ServiceName]Test {
    
    // Mock dependencies (repositories and external services)
    @Mock private [Repository] repository;
    @Mock private [ExternalService] externalService;
    
    // Service under test
    @InjectMocks
    private [ServiceClass] service;
    
    @Test
    @DisplayName("Should [action] when [condition]")
    void should[Action]When[Condition]() {
        // Given - Setup test scenario
        [RequestType] request = create[RequestType]();
        [EntityType] expectedEntity = create[EntityType]();
        when(repository.save(any())).thenReturn(expectedEntity);
        
        // When - Execute business logic
        [ResponseType] result = service.[methodName](request);
        
        // Then - Verify business outcome
        assertNotNull(result);
        assertEquals([expected], result.get[Field]());
        verify(repository).save(any());
    }
    
    @Test
    @DisplayName("Should throw [ExceptionType] when [error condition]")
    void shouldThrow[ExceptionType]When[ErrorCondition]() {
        // Given - Setup error scenario
        [RequestType] invalidRequest = createInvalid[RequestType]();
        
        // When & Then - Verify exception
        [ExceptionType] exception = assertThrows([ExceptionType].class,
            () -> service.[methodName](invalidRequest));
        
        assertEquals("[expected error message]", exception.getMessage());
    }
    
    // Helper methods for test data creation
    private [RequestType] create[RequestType]() {
        // Create valid request object
        [RequestType] request = new [RequestType]();
        // Set required fields
        return request;
    }
    
    private [EntityType] create[EntityType]() {
        // Create mock entity for repository responses
        [EntityType] entity = new [EntityType]();
        // Set required fields
        return entity;
    }
}
```

### **Quick Service Test Data Creation**

```java
// Quick builders for common test scenarios
public class ServiceTestBuilders {
    
    // Valid request builders
    public static SaveRoleRequest validRoleRequest(String name) {
        SaveRoleRequest request = new SaveRoleRequest();
        request.setName(name);
        request.setDescription(name + " Description");
        request.setIsActive(true);
        request.setPrivileges(List.of());
        return request;
    }
    
    // Mock entity builders
    public static Role mockRole(String name, boolean active) {
        Role role = new Role();
        role.setId(1);
        role.setName(name);
        role.setDescription(name + " Description");
        role.setIsActive(active ? 1 : 0);
        role.setCreatedOn(LocalDateTime.now());
        role.setCreatedById("SYSTEM");
        return role;
    }
    
    // Error scenario builders
    public static SaveRoleRequest invalidRoleRequest() {
        SaveRoleRequest request = new SaveRoleRequest();
        request.setName(null); // Invalid - null name
        return request;
    }
}
```

### **Service Testing Quick Commands**

```bash
# Development workflow commands
.\mvnw.cmd test -Dtest=YourServiceTest                    # Test single service
.\mvnw.cmd test -Dtest=*Role*Test                        # Test role-related services
.\mvnw.cmd test -Dtest=**/service/**/*Test -X            # Debug service tests

# Coverage commands
.\mvnw.cmd test jacoco:report -Dtest=**/service/**/*Test # Service coverage only
.\mvnw.cmd jacoco:check -Dtest=**/service/**/*Test       # Check service coverage meets threshold

# Quick verification
.\mvnw.cmd test -Dtest=**/service/**/*Test --fail-fast   # Stop on first service test failure
```

### **Service Test Debugging**

```java
// Add debug output to service tests
@Test
void debugServiceTest() {
    System.out.println("Testing service method: " + methodName);
    
    // Your test logic
    YourResponse result = service.processRequest(request);
    
    System.out.println("Service result: " + result);
    assertNotNull(result);
}

// Mock interaction verification
@Test  
void verifyServiceInteractions() {
    service.performAction();
    
    // Verify mock calls
    verify(repository).findById(anyLong());
    verify(repository).save(any());
    verify(externalService).notify(any());
    
    // Check argument values
    ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
    verify(repository).save(captor.capture());
    assertEquals("expectedValue", captor.getValue().getName());
}
```

### **Service Testing Anti-Patterns (What NOT to Do)**

```java
// ❌ DON'T test framework code
@Test void shouldSaveToDatabase() { /* Tests Spring Data JPA */ }

// ❌ DON'T test simple getters/setters  
@Test void shouldGetName() { /* No business logic */ }

// ❌ DON'T test controller HTTP handling
@Test void shouldReturn200Status() { /* Controller responsibility */ }

// ❌ DON'T test configuration loading
@Test void shouldLoadProperties() { /* Spring Boot handles this */ }

// ✅ DO test business logic
@Test void shouldCalculateDiscountForPremiumCustomer() { /* Business rule */ }

// ✅ DO test validation logic
@Test void shouldRejectInvalidEmailFormat() { /* Business validation */ }

// ✅ DO test error handling
@Test void shouldHandleRepositoryException() { /* Business error handling */ }
```

### **Maximum Service Testing Efficiency**

**Focus on Business Value:**
1. **Test business rules and calculations** - Where bugs have real impact
2. **Test validation logic** - Prevents bad data from entering system  
3. **Test error handling** - Ensures graceful failure recovery
4. **Test service orchestration** - How services coordinate with dependencies

**Skip Non-Business Code:**
1. **Simple DTOs** - Just data containers
2. **Basic CRUD operations** - Repository layer handles these
3. **Configuration classes** - Framework responsibility
4. **HTTP mapping** - Controller layer responsibility

**Quick Service Testing Strategy:**
```bash
# 1. Create service test class
# 2. Mock all dependencies (@Mock)
# 3. Inject mocks into service (@InjectMocks)
# 4. Test happy path first
# 5. Add error scenarios
# 6. Add edge cases
# 7. Verify with coverage report
```

---

**Remember**: Focus on testing **business behavior** that delivers **customer value**. Skip infrastructure and framework code that's already tested by the framework providers! 🎯

---

*Last Updated: July 2025 - Simplified for Service Layer Focus*

---

## 🚀 Speed Tips

### Keyboard Shortcuts for Testing

**IntelliJ IDEA:**
- `Ctrl+Shift+F10` → Run test under cursor
- `Ctrl+F5` → Rerun last test
- `Alt+Shift+F10` → Run with options
- `Ctrl+Shift+T` → Navigate between test and source
- `Ctrl+Alt+R` → Run all tests in class

**VS Code:**
- `Ctrl+Shift+P` → "Test: Run All Tests"
- Click green triangles to run individual tests
- Use Test Explorer for visual test management

### Maven Speed Commands

```bash
# Run only failing tests
.\mvnw.cmd test -Dsurefire.runOrder=failedfirst

# Run tests in parallel
.\mvnw.cmd test -T 4

# Skip integration tests
.\mvnw.cmd test -DskipITs=true

# Run specific test method
.\mvnw.cmd test -Dtest=RoleManagementServiceImplTest#shouldCreateNewRoleSuccessfully

# Run with specific profile
.\mvnw.cmd test -Ptest

# Quick compile and test
.\mvnw.cmd compile test-compile surefire:test
```

### Quick Coverage Check

```bash
# Generate coverage for specific package
.\mvnw.cmd test jacoco:report -Dtest=**/role/**/*Test

# Open coverage report quickly
start target/site/jacoco/index.html

# Coverage for single class
.\mvnw.cmd test -Dtest=RoleManagementServiceImplTest jacoco:report
```

### Test Debugging Shortcuts

**Quick Debug Setup:**

```java
@Test
void debugTest() {
    // Add breakpoint here
    System.out.println("Debug point 1");
    
    // Your test logic
    
    System.out.println("Debug point 2");
}
```

**Use IDE Debugger:**
1. Set breakpoint in test
2. Right-click → "Debug test"
3. Step through code with F8/F7
4. Evaluate expressions with Alt+F8

### Copy-Paste Test Templates

**Minimal Service Test:**

```java
@Test
void should[Action]When[Condition]() {
    // Given
    [InputType] input = create[InputType]();
    [MockType] mockResult = create[MockType]();
    when([dependency].[method](any())).thenReturn(mockResult);
    
    // When
    [ResultType] result = [service].[methodUnderTest](input);
    
    // Then
    assertNotNull(result);
    assertEquals([expected], result.get[Property]());
    verify([dependency]).[method](any());
}
```

**Minimal Controller Test:**

```java
@Test
void should[Action]When[Condition]() {
    // Given
    [RequestType] request = create[RequestType]();
    [ServiceResponseType] serviceResponse = create[ServiceResponseType]();
    when([service].[method](any())).thenReturn(serviceResponse);
    
    // When
    ResponseEntity<ApiResponse<[ResponseType]>> response = 
        [controller].[method](request);
    
    // Then
    assertEquals(HttpStatus.[STATUS], response.getStatusCode());
    assertTrue(response.getBody().isSuccess());
    assertNotNull(response.getBody().getData());
}
```

### Quick Test Data Creation

**Use Static Factory Methods:**

```java
public class TestData {
    public static SaveRoleRequest roleRequest(String name) {
        SaveRoleRequest request = new SaveRoleRequest();
        request.setName(name);
        request.setDescription("Test description");
        request.setIsActive(true);
        request.setPrivileges(List.of());
        return request;
    }
    
    public static Role role(String name) {
        Role role = new Role();
        role.setName(name);
        role.setDescription("Test description");
        role.setIsActive(1);
        role.setCreatedOn(LocalDateTime.now());
        role.setCreatedById("SYSTEM");
        return role;
    }
}

// Usage in tests
@Test
void quickTest() {
    SaveRoleRequest request = TestData.roleRequest("ADMIN");
    Role role = TestData.role("ADMIN");
    // Test logic...
}
```

### Maximum Coverage Strategy

**Focus on Business Logic:**
1. Test all public methods
2. Test error conditions
3. Test edge cases (null, empty, boundary values)
4. Test different execution paths

**Quick Coverage Wins:**
```java
@Test
void coverAllBranches() {
    // Test happy path
    testMethod_HappyPath();
    
    // Test error cases
    testMethod_NullInput();
    testMethod_EmptyInput();
    testMethod_InvalidInput();
    
    // Test edge cases
    testMethod_BoundaryValues();
}
```

### Time-Saving Tools

**Use JUnit @ParameterizedTest for multiple scenarios:**

```java
@ParameterizedTest
@ValueSource(strings = {"ADMIN", "USER", "MANAGER"})
void shouldCreateRoleForDifferentNames(String roleName) {
    // Test logic that works for all role names
    SaveRoleRequest request = TestData.roleRequest(roleName);
    RoleDto result = roleService.createRole(request);
    assertEquals(roleName, result.getName());
}

@ParameterizedTest
@CsvSource({
    "ADMIN, true, 1",
    "USER, false, 0",
    "GUEST, true, 1"
})
void shouldCreateRoleWithDifferentStates(String name, boolean isActive, int expectedStatus) {
    // Test multiple combinations quickly
}
```

**Mock Multiple Objects Quickly:**

```java
@BeforeEach
void setup() {
    // Reset all mocks for clean state
    Mockito.reset(roleRepo, privilegeRepo, configRepo);
    
    // Setup common mock behaviors
    when(configRepo.findActiveConfig()).thenReturn(createDefaultConfig());
    when(messageResolver.getMessage(any())).thenReturn("Success");
}
```

---

## 🆘 Common Issues & FAQ

### ❓ Frequently Asked Questions

**Q: "Tests run fine locally but fail in CI/CD"**
```bash
# Solution: Ensure consistent environment
.\mvnw.cmd clean test -Dspring.profiles.active=test

# Check if AWS credentials are properly mocked
# Verify H2 database configuration
```

**Q: "Coverage is lower than expected"**
```bash
# Check what's actually tested
.\mvnw.cmd test jacoco:report
start target/site/jacoco/index.html

# Focus on untested methods (red lines in report)
# Add tests for edge cases and error conditions
```

**Q: "Tests are slow to run"**
```bash
# Run tests in parallel
.\mvnw.cmd test -T 4

# Run only specific layer
.\mvnw.cmd test -Dtest=**/service/**/*Test
```

**Q: "Mock doesn't work as expected"**
```java
// ✅ Correct mock setup
@ExtendWith(MockitoExtension.class)
class MyTest {
    @Mock private MyRepository repository;
    @InjectMocks private MyService service;
    
    @Test
    void test() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        // Mock will work correctly
    }
}
```

### 🔧 Quick Troubleshooting

**Issue: "No tests found"**
- ✅ Check test class name ends with `Test`
- ✅ Verify test methods have `@Test` annotation
- ✅ Ensure test is in correct package structure

**Issue: "Coverage check failed"**
- ✅ Run: `.\mvnw.cmd jacoco:check -X` to see details
- ✅ Add tests for uncovered lines
- ✅ Current threshold: 80% minimum

**Issue: "Bean creation failed in tests"**
- ✅ Use `@WebMvcTest` instead of `@SpringBootTest`
- ✅ Mock external dependencies with `@MockitoBean`
- ✅ Check `application-test.yml` configuration

### 🎯 Testing Strategy by Component

**For New Service Class:**
1. Create: `src/test/java/.../service/YourServiceTest.java`
2. Test: Business logic methods (90%+ coverage target)
3. Mock: All dependencies (repositories, external services)
4. Cover: Happy path + error cases + edge cases

**For New Controller Class:**
1. Create: `src/test/java/.../controller/YourControllerTest.java`
2. Test: HTTP endpoints (85%+ coverage target)
3. Mock: Service layer dependencies
4. Cover: Request validation + response formatting + error handling

**For New Repository Class:**
1. Create: `src/test/java/.../repository/YourRepositoryTest.java`
2. Test: Custom query methods (80%+ coverage target)
3. Use: `@DataJpaTest` with in-memory H2
4. Cover: Query logic + constraints + transactions

---

**Remember**: Focus on testing behavior, not implementation. Write tests that give you confidence to refactor and change code safely! 🧪

---

*Last Updated: July 2025*