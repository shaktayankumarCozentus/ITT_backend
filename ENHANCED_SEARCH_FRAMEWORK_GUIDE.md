# 🔍 Enhanced Universal Search Framework Guide

**Version 2.0** | **Updated: August 2025** | **Production Ready**

## 📖 Table of Contents

1. [🚀 Quick Start Guide](#-quick-start-guide)
2. [🎯 Framework Overview](#-framework-overview)
3. [📋 Best Practice Template](#-best-practice-template)
4. [🔧 Implementation Steps](#-implementation-steps)
5. [⚡ Advanced Features](#-advanced-features)
6. [🛠️ Supported Operations](#️-supported-operations)
7. [🔍 Request Format Reference](#-request-format-reference)
8. [⚠️ Troubleshooting Guide](#️-troubleshooting-guide)
9. [📊 Performance Optimization](#-performance-optimization)
10. [🛡️ Security & Validation](#️-security--validation)

---

## 🚀 Quick Start Guide

### ⏱️ 5-Minute Setup

Enable full search/sort/pagination for any entity in 5 minutes:

#### Step 1: Create SearchConfig (2 minutes)
```java
@Component
public class YourEntitySearchConfig implements SearchableEntity<YourEntity> {
    
    @Override
    public Set<String> getSearchableFields() {
        return Set.of(
            // Entity primary fields (use actual entity field names)
            "id", "name", "description", "createdOn",
            
            // Entity relationship fields (use actual entity field names)
            "category.id", "category.name", "createdBy.email"
        );
    }
    
    @Override
    public Set<String> getSortableFields() {
        // Copy-paste getSearchableFields for consistency
        return Set.of(
            "id", "name", "description", "createdOn",
            "category.id", "category.name", "createdBy.email"
        );
    }
    
    @Override
    public Map<String, String> getFieldAliases() {
        return Map.of(
            // DTO field → Entity field mappings ONLY
            "categoryName", "category.name",
            "createdBy", "createdBy.email"
        );
    }
    
    @Override
    public Set<String> getDefaultSearchColumns() {
        return Set.of("name", "description");
    }
    
    @Override
    public Class<YourEntity> getEntityClass() {
        return YourEntity.class;
    }
    
    @Override
    public Set<String> getFetchJoins() {
        return Set.of("category", "createdBy"); // Prevent N+1 queries
    }
    
    @Override
    public boolean shouldUseDistinct() {
        return true; // Use when joining collections
    }
}
```

#### Step 2: Enable in Service (1 minute)
```java
@Service
public class YourEntityService extends BaseService<YourEntity, YourEntityDto> {
    
    @Autowired
    private YourEntitySearchConfig searchConfig;
    
    @Override
    protected SearchableEntity<YourEntity> getSearchableEntity() {
        return searchConfig; // Framework activates automatically!
    }
    
    // search() method is inherited - no additional code needed!
}
```

#### Step 3: Use in Controller (already works)
```java
@PostMapping("/search")
public ResponseEntity<PaginationResponse<YourEntityDto>> search(@RequestBody DataTableRequest request) {
    return ResponseBuilder.dynamicResponse(yourService.search(request));
}
```

**✅ Done! Your entity now has enterprise-grade search capabilities!**

---

## 🎯 Framework Overview

### What Problem Does This Solve?

❌ **Before Framework:**
- Write complex JPQL queries for every entity
- Handle pagination manually
- Implement sorting logic repeatedly
- Manage N+1 query problems
- Build custom filtering for each field

✅ **After Framework:**
- Single configuration class per entity
- Automatic query generation
- Built-in pagination and sorting
- N+1 prevention with fetch joins
- 20+ filter operators out-of-the-box

### Core Components

| Component | Purpose | Lines of Code |
|-----------|---------|---------------|
| **DynamicSearchQueryBuilder** | Query generation engine | 700+ lines |
| **SearchableEntity Interface** | Configuration contract | 11 methods |
| **FilterParser** | Advanced filter parsing | 20+ operators |
| **BaseService Integration** | Service layer bridge | Auto-inherited |

### Key Benefits

🔥 **Performance:**
- Automatic N+1 prevention
- Optimized fetch joins
- Subquery optimization for large collections
- Batch operations support

🛡️ **Security:**
- SQL injection prevention
- Field validation
- Type safety

⚡ **Developer Experience:**
- 5-minute setup
- Copy-paste templates
- Auto-discovery
- Comprehensive error handling

---

## 📋 Best Practice Template

### 🔑 CRITICAL RULES

1. **Entity Fields EVERYWHERE**: Use actual entity field names in all methods
2. **DTO Mapping ONLY in Aliases**: Map DTO fields to entity fields in `getFieldAliases()`
3. **Copy-Paste Consistency**: `getSearchableFields()` = `getSortableFields()`

### 📝 Complete Template

```java
@Component
public class [Entity]SearchConfig implements SearchableEntity<[Entity]> {

    // ✅ RULE 1: Use ENTITY field names (mandatory)
    @Override
    public Set<String> getSearchableFields() {
        return Set.of(
            // Entity primary fields (check your entity class)
            "id", "name", "description", "isActive", "createdOn", "updatedOn",
            
            // Entity relationship fields (check your entity class)
            "category.id", "category.name", "category.code",
            "assignedUser.id", "assignedUser.fullName", "assignedUser.email",
            "createdBy.email", "updatedBy.email"
        );
    }

    // ✅ RULE 2: Copy-paste getSearchableFields for consistency
    @Override
    public Set<String> getSortableFields() {
        return Set.of(
            // Same as getSearchableFields() - copy/paste
            "id", "name", "description", "isActive", "createdOn", "updatedOn",
            "category.id", "category.name", "category.code",
            "assignedUser.id", "assignedUser.fullName", "assignedUser.email",
            "createdBy.email", "updatedBy.email"
        );
    }

    // ✅ RULE 3: DTO → Entity mapping ONLY
    @Override
    public Map<String, String> getFieldAliases() {
        return Map.of(
            // Map DTO field names to entity field names
            "categoryName", "category.name",
            "categoryCode", "category.code",
            "assignedTo", "assignedUser.fullName",
            "assignedUserEmail", "assignedUser.email",
            "createdBy", "createdBy.email",
            "updatedBy", "updatedBy.email"
        );
    }

    // ✅ REQUIRED: Entity field names for default search
    @Override
    public Set<String> getDefaultSearchColumns() {
        return Set.of("name", "description", "category.name");
    }

    // ✅ REQUIRED: Entity class for type safety
    @Override
    public Class<[Entity]> getEntityClass() {
        return [Entity].class;
    }

    // ✅ PERFORMANCE: Prevent N+1 queries
    @Override
    public Set<String> getFetchJoins() {
        return Set.of("category", "assignedUser", "createdBy", "updatedBy");
    }

    // ✅ OPTIMIZATION: Use when joining collections
    @Override
    public boolean shouldUseDistinct() {
        return true;
    }

    // ✅ OPTIONAL: Default sorting
    @Override
    public List<String> getDefaultSortFields() {
        return List.of("name:asc");
    }
}
```

### 🎯 How to Find Entity Field Names

```java
// Look at your entity class:
@Entity
public class Product {
    private String name;                    // ← Use: "name"
    private Category category;              // ← Use: "category.name"
    private Set<Tag> tags;                  // ← Use: "tags.name"
    private User createdBy;                 // ← Use: "createdBy.email"
}
```

---

## 🔧 Implementation Steps

### Step 1: Analyze Your Entity

```java
// Example: Role entity analysis
@Entity
public class Role {
    private String name;                         // ✅ "name"
    private String description;                  // ✅ "description"
    private MasterConfig roleTypeConfig;         // ✅ "roleTypeConfig.name"
    private MasterConfig landingPageConfig;      // ✅ "landingPageConfig.name"
    private MasterUser createdByUser;            // ✅ "createdByUser.email"
    private MasterUser updatedByUser;            // ✅ "updatedByUser.email"
}
```

### Step 2: Create SearchConfig

```java
@Component
public class RoleSearchConfig implements SearchableEntity<Role> {
    
    @Override
    public Set<String> getSearchableFields() {
        return Set.of(
            // Primary fields from entity
            "id", "name", "description", "isActive", "createdOn", "updatedOn",
            
            // Relationship fields from entity
            "roleTypeConfig.id", "roleTypeConfig.keyCode", "roleTypeConfig.name",
            "landingPageConfig.id", "landingPageConfig.keyCode", "landingPageConfig.name",
            "createdByUser.email", "updatedByUser.email"
        );
    }
    
    @Override
    public Set<String> getSortableFields() {
        // Copy-paste getSearchableFields
        return Set.of(
            "id", "name", "description", "isActive", "createdOn", "updatedOn",
            "roleTypeConfig.id", "roleTypeConfig.keyCode", "roleTypeConfig.name",
            "landingPageConfig.id", "landingPageConfig.keyCode", "landingPageConfig.name",
            "createdByUser.email", "updatedByUser.email"
        );
    }
    
    @Override
    public Map<String, String> getFieldAliases() {
        return Map.of(
            // DTO fields → Entity fields
            "createdBy", "createdByUser.email",
            "updatedBy", "updatedByUser.email",
            "roleType.id", "roleTypeConfig.id",
            "roleType.key", "roleTypeConfig.keyCode",
            "roleType.name", "roleTypeConfig.name",
            "landingPage.id", "landingPageConfig.id",
            "landingPage.key", "landingPageConfig.keyCode",
            "landingPage.name", "landingPageConfig.name"
        );
    }
    
    @Override
    public Set<String> getDefaultSearchColumns() {
        return Set.of("name", "description", "createdByUser.email", "updatedByUser.email");
    }
    
    @Override
    public Class<Role> getEntityClass() {
        return Role.class;
    }
    
    @Override
    public Set<String> getFetchJoins() {
        return Set.of("roleTypeConfig", "landingPageConfig", "createdByUser", "updatedByUser");
    }
    
    @Override
    public boolean shouldUseDistinct() {
        return true;
    }
}
```

### Step 3: Enable in Service

```java
@Service
public class RoleService extends BaseService<Role, RoleDto> {
    
    @Autowired
    private RoleSearchConfig roleSearchConfig;
    
    @Override
    protected SearchableEntity<Role> getSearchableEntity() {
        return roleSearchConfig;
    }
}
```

### Step 4: Test Your Implementation

```bash
# Compile to check for errors
./mvnw compile

# Test with simple request
POST /api/roles/search
{
    "pagination": { "page": 0, "size": 10 },
    "searchFilter": { "searchText": "admin" }
}

# Test with complex filters
{
    "pagination": { "page": 0, "size": 10 },
    "columns": [
        { "columnName": "roleType.name", "filter": "sw:PSA_", "sort": "asc" }
    ]
}
```

---

## ⚡ Advanced Features

### 🚀 Subquery Optimization for Large Collections

Use for entities with 1000+ related records:

```java
@Component
public class UserSearchConfig implements SearchableEntity<MasterUser> {
    
    @Override
    public boolean useSubqueryForField(String field) {
        // Use EXISTS subquery for large collections
        return field.startsWith("companies.");
    }
    
    @Override
    public String getSubqueryCondition(String field, String paramName) {
        if ("companies.companyName".equals(field)) {
            return """
                EXISTS (
                    SELECT 1 FROM MapUserCompany muc 
                    JOIN muc.company c 
                    WHERE muc.user = e 
                    AND LOWER(c.companyName) LIKE LOWER(CONCAT('%', :""" + paramName + """, '%'))
                )
                """;
        }
        return null;
    }
    
    @Override
    public Set<String> getFetchJoins() {
        // Don't fetch large collections
        return Set.of("assignedRole", "updatedByUser");
    }
}
```

### 🔄 Base Query Extension

Add search to existing complex queries:

```java
// Your existing complex query
String complexQuery = """
    SELECT DISTINCT e FROM Role e 
    LEFT JOIN e.permissions p 
    WHERE e.isActive = true 
    AND p.module = 'ADMIN'
    """;

// Framework adds search/sort/pagination automatically
Page<Role> results = dynamicSearchQueryBuilder.findWithDynamicSearch(
    roleSearchConfig, 
    request, 
    complexQuery
);
```

### 📊 Batch Operations

Optimize related data fetching:

```java
@Service
public class UserService {
    
    public PaginationResponse<UserDto> getUsers(DataTableRequest request) {
        // 1. Execute search
        Page<MasterUser> page = search(request);
        
        // 2. Batch fetch company counts (1 query instead of N queries)
        List<Integer> userIds = page.getContent().stream()
                .map(MasterUser::getId)
                .toList();
        
        Map<Integer, Long> companyCounts = mapRepo.countCompaniesByUserIds(userIds);
        
        // 3. Convert to DTOs with pre-fetched data
        List<UserDto> content = page.getContent().stream()
                .map(u -> convertToDto(u, companyCounts.get(u.getId())))
                .toList();
        
        return buildPaginationResponse(content, page);
    }
}
```

---

## 🛠️ Supported Operations

### String Operations
| Operator | Usage | Example |
|----------|-------|---------|
| `cnt` or none | Contains | `"admin"` or `"cnt:admin"` |
| `ncnt` | Not contains | `"ncnt:test"` |
| `sw` | Starts with | `"sw:Admin"` |
| `ew` | Ends with | `"ew:user"` |
| `eq` | Equals | `"eq:ACTIVE"` |
| `ne` | Not equals | `"ne:INACTIVE"` |

### Numeric Operations
| Operator | Usage | Example |
|----------|-------|---------|
| `gt` | Greater than | `"gt:100"` |
| `gte` | Greater/equal | `"gte:50"` |
| `lt` | Less than | `"lt:200"` |
| `lte` | Less/equal | `"lte:75"` |
| `eq` | Equals | `"eq:42"` |
| `ne` | Not equals | `"ne:0"` |

### Date Operations
| Operator | Usage | Example | Format |
|----------|-------|---------|--------|
| `dgt` | After | `"dgt:2024-01-01"` | yyyy-MM-dd |
| `dgte` | On/After | `"dgte:2024-01-01"` | yyyy-MM-dd |
| `dlt` | Before | `"dlt:2024-12-31"` | yyyy-MM-dd |
| `dlte` | On/Before | `"dlte:2024-12-31"` | yyyy-MM-dd |
| `deq` | Date equals | `"deq:2024-06-15"` | yyyy-MM-dd |
| `dne` | Date not equal | `"dne:2024-12-25"` | yyyy-MM-dd |
| `dbetween` | Date range | `"dbetween:2024-01-01,2024-12-31"` | start,end |

### List Operations
| Operator | Usage | Example |
|----------|-------|---------|
| `in` | In list | `"in:RED,BLUE,GREEN"` |

---

## 🔍 Request Format Reference

### Complete Request Structure

```json
{
    "pagination": {
        "page": 0,          // Zero-based page number
        "size": 10          // Records per page (1-100)
    },
    "searchFilter": {
        "searchText": "search term",        // Global search
        "columns": ["name", "description"]  // Specific fields (optional)
    },
    "columns": [
        {
            "columnName": "name",           // Entity field name
            "filter": "sw:Admin",           // operator:value
            "sort": "asc"                   // asc or desc
        },
        {
            "columnName": "createdOn",
            "filter": "dgte:2024-01-01",
            "sort": "desc"
        }
    ]
}
```

### TypeScript Interface

```typescript
interface DataTableRequest {
    pagination: {
        page: number;               // 0-based page index
        size: number;               // 1-100 records per page
    };
    searchFilter?: {
        searchText?: string;        // Global search term
        columns?: string[];         // Specific fields to search
    };
    columns?: Array<{
        columnName: string;         // Entity field name
        filter?: string;            // Format: "operator:value"
        sort?: "asc" | "desc";      // Sort direction
    }>;
}
```

### Real Examples

#### Basic Search
```json
{
    "pagination": { "page": 0, "size": 10 },
    "searchFilter": { "searchText": "admin" }
}
```

#### Advanced Filtering
```json
{
    "pagination": { "page": 0, "size": 10 },
    "columns": [
        { "columnName": "roleType.name", "filter": "sw:PSA_", "sort": "asc" },
        { "columnName": "createdOn", "filter": "dgte:2024-01-01", "sort": "desc" },
        { "columnName": "isActive", "filter": "eq:true" }
    ]
}
```

#### Date Range with Global Search
```json
{
    "pagination": { "page": 0, "size": 20 },
    "searchFilter": { 
        "searchText": "important",
        "columns": ["name", "description"] 
    },
    "columns": [
        { "columnName": "createdOn", "filter": "dbetween:2024-01-01,2024-12-31" }
    ]
}
```

---

## ⚠️ Troubleshooting Guide

### 🔍 Common Issues & Solutions

#### ❌ "Framework Not Working"
**Symptoms**: Search returns empty or errors
**Cause**: Framework not activated
**Solution**:
```java
// ✅ Enable framework in your service
@Override
protected SearchableEntity<YourEntity> getSearchableEntity() {
    return yourSearchConfig; // This line activates the framework
}
```

#### ❌ "Field Not Searchable"
**Symptoms**: Specific fields don't work in search/sort
**Cause**: Field missing from configuration
**Solution**:
```java
@Override
public Set<String> getSearchableFields() {
    return Set.of(
        "name", "description", 
        "category.name"  // ← Add missing field
    );
}
```

#### ❌ "Hibernate PathElementException"
**Symptoms**: `Could not resolve attribute 'fieldName'`
**Cause**: Using DTO field names instead of entity field names
**Solution**:
```java
// ❌ WRONG: DTO field names
"roleType.name", "landingPage.name"

// ✅ CORRECT: Entity field names
"roleTypeConfig.name", "landingPageConfig.name"
```

#### ❌ "N+1 Query Performance"
**Symptoms**: Multiple SELECT queries in logs
**Cause**: Missing fetch joins
**Solution**:
```java
@Override
public Set<String> getFetchJoins() {
    return Set.of("category", "assignedUser", "tags"); // Add all related entities
}

@Override
public boolean shouldUseDistinct() {
    return true; // Required when joining collections
}
```

#### ❌ "Date Filters Not Working"
**Symptoms**: Date operations return no results
**Cause**: Using wrong operators or format
**Solution**:
```java
// ✅ CORRECT: Date operators
{ "columnName": "createdOn", "filter": "dgte:2024-01-01" }
{ "columnName": "updatedOn", "filter": "dbetween:2024-01-01,2024-12-31" }

// ❌ WRONG: String operators on dates
{ "columnName": "createdOn", "filter": "gte:2024-01-01" } // Use 'dgte'
```

### 🔧 Debug Commands

#### Check Framework Status
```java
@PostConstruct
public void debugFramework() {
    SearchableEntity<?> config = getSearchableEntity();
    if (config != null) {
        log.info("✅ Framework ENABLED for {}", config.getEntityClass().getSimpleName());
        log.info("Searchable fields: {}", config.getSearchableFields());
        log.info("Fetch joins: {}", config.getFetchJoins());
    } else {
        log.warn("❌ Framework DISABLED - override getSearchableEntity()");
    }
}
```

#### Enable SQL Logging
```yaml
# application.yml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    com.itt.service.fw.search: DEBUG
```

---

## 📊 Performance Optimization

### 🔥 Database Level

#### Essential Indexes
```sql
-- Basic indexes for searchable fields
CREATE INDEX idx_role_name ON roles(name);
CREATE INDEX idx_role_created_on ON roles(created_on);

-- Composite indexes for common filter combinations
CREATE INDEX idx_role_active_type ON roles(is_active, role_type_config_id);
CREATE INDEX idx_role_created_updated ON roles(created_on, updated_on);

-- Foreign key indexes for joins
CREATE INDEX idx_role_type_config ON roles(role_type_config_id);
CREATE INDEX idx_role_landing_page ON roles(landing_page_config_id);
```

#### Index Strategy
- Index all frequently searched fields
- Create composite indexes for common filter combinations
- Monitor slow query logs for optimization opportunities

### ⚡ Configuration Level

```java
@Override
public Set<String> getFetchJoins() {
    // Include ALL related entities used in search/display
    return Set.of("category", "assignedUser", "createdBy", "updatedBy");
}

@Override
public boolean shouldUseDistinct() {
    // Only use when joining collections (Set, List fields)
    return true; // or false for simple relations
}

@Override
public Set<String> getSearchableFields() {
    // Limit to actually needed fields (don't expose everything)
    return Set.of("name", "description", "category.name"); // Be selective
}
```

### 🚀 Query Level

#### Subquery Optimization for Large Collections
```java
@Override
public boolean useSubqueryForField(String field) {
    // Use for entities with 1000+ related records
    return field.startsWith("companies.") || field.startsWith("permissions.");
}
```

#### Batch Operations
```java
// Instead of N queries, use 1 GROUP BY query
Map<Integer, Long> counts = repository.countRelatedEntitiesByIds(entityIds);
```

### 📈 Performance Checklist

- [ ] **Database indexes** on all searchable fields
- [ ] **Fetch joins** for all related entities in search/display
- [ ] **Distinct only when needed** (collection joins)
- [ ] **Subquery optimization** for large collections (1000+ records)
- [ ] **Batch operations** for related data fetching
- [ ] **Reasonable page sizes** (10-50 records)
- [ ] **Monitor slow queries** and optimize indexes

---

## 🛡️ Security & Validation

### Built-in Protection

✅ **SQL Injection Prevention**: All parameters use JPA parameterized queries  
✅ **Field Validation**: Only configured fields are processed  
✅ **Type Safety**: Automatic validation with graceful fallback  
✅ **Error Handling**: Comprehensive exception handling  
✅ **Auto-Discovery Security**: Only `@Component` classes are registered  

### Field Security Example

```java
@Override
public Set<String> getSearchableFields() {
    return Set.of("name", "description", "status"); // Only these can be searched
}

// ❌ MALICIOUS REQUEST: Automatically ignored
{
    "columns": [
        { "columnName": "password", "filter": "eq:hack" } // ← Ignored with warning
    ]
}

// ✅ SAFE QUERY: Only configured fields processed
// Generated: WHERE e.name LIKE :param1
// Parameters: {"param1": "%admin%"}
```

### Validation Rules

1. **Field Whitelist**: Only fields in `getSearchableFields()` are processed
2. **Operator Validation**: Invalid operators fall back to safe defaults
3. **Type Checking**: Automatic type validation for dates and numbers
4. **Parameter Escaping**: All values are properly parameterized

---

## 🎓 Development Workflow

### 1. **Entity Analysis**
```bash
# Step 1: Examine your entity class
# Look for field names and relationships
```

### 2. **SearchConfig Creation**
```bash
# Step 2: Create SearchConfig using template
# Use actual entity field names everywhere
```

### 3. **Service Integration**
```bash
# Step 3: Override getSearchableEntity() in service
# Framework activates automatically
```

### 4. **Testing**
```bash
# Step 4: Test with compilation and API calls
./mvnw compile
# Test basic search, then complex filters
```

### 5. **Optimization**
```bash
# Step 5: Add indexes and monitor performance
# Use SQL logging to identify slow queries
```

---

## 🌟 Framework Philosophy

### **Universal, Optimized, Developer-Friendly**

This framework provides a **single, unified solution** for all search needs with enterprise-grade performance:

✅ **Configuration Over Code**: Simple interface vs complex query building  
✅ **Performance First**: Built-in optimizations for real-world usage  
✅ **Type Safety**: Compile-time validation and runtime error handling  
✅ **Auto-Discovery**: Zero manual registration required  
✅ **Scalability**: Handles large datasets with subquery optimization  
✅ **Security**: Built-in SQL injection prevention and field validation  

### **Key Design Principles**

1. **Entity Field Names Everywhere**: Prevents Hibernate errors
2. **DTO Mapping in Aliases Only**: Clean separation of concerns
3. **Copy-Paste Consistency**: Reduces developer errors
4. **Performance by Default**: N+1 prevention and optimization built-in
5. **Security by Design**: Field validation and SQL injection prevention

---

**🎉 You're ready to build powerful search functionality!**

Follow this guide step-by-step and you'll have enterprise-grade search capabilities with minimal code and maximum performance.

### String Operations
- ✅ **Contains** (`cnt`) - Default behavior
- ✅ **Not Contains** (`ncnt`)
- ✅ **Starts With** (`sw`)
- ✅ **Ends With** (`ew`)
- ✅ **Equals** (`eq`)
- ✅ **Not Equals** (`ne`)

### Numeric Operations
- ✅ **Equals** (`eq`) - Auto-detects numeric vs string
- ✅ **Not Equals** (`ne`)
- ✅ **Greater Than** (`gt`)
- ✅ **Greater Than or Equal** (`gte`)
- ✅ **Less Than** (`lt`)
- ✅ **Less Than or Equal** (`lte`)

### Date Operations
- ✅ **Date Greater Than** (`dgt`)
- ✅ **Date Greater Than or Equal** (`dgte`)
- ✅ **Date Less Than** (`dlt`)
- ✅ **Date Less Than or Equal** (`dlte`)
- ✅ **Date Equals** (`deq`)
- ✅ **Date Not Equals** (`dne`)
- ✅ **Date Between** (`dbetween`) - Format: `start,end`

### List Operations
- ✅ **In List** (`in`) - Format: `value1,value2,value3`

---

## 📖 Usage Examples

### Basic Search
```json
POST /api/roles/search
{
    "searchFilter": {
        "searchText": "admin"
    },
    "pagination": {
        "page": 0,
        "size": 10
    }
}
```

### Advanced Filtering
```json
{
    "searchFilter": {
        "searchText": "admin",
        "columns": ["name", "description"]
    },
    "columns": [
        { "columnName": "name", "filter": "sw:Super", "sort": "asc" },
        { "columnName": "createdOn", "filter": "dgte:2024-01-01", "sort": "desc" },
        { "columnName": "status", "filter": "in:ACTIVE,PENDING" }
    ],
    "pagination": {
        "page": 0,
        "size": 10
    }
}
```

### Date Range Search
```json
{
    "columns": [
        { "columnName": "createdOn", "filter": "dbetween:2024-01-01,2024-12-31" }
    ],
    "pagination": {
        "page": 0,
        "size": 10
    }
}
```

---

## 🔧 Request Format Reference

### DataTableRequest Structure
```json
{
    "pagination": {
        "page": 0,      // Zero-based page number
        "size": 10      // Records per page (10-50)
    },
    "searchFilter": {
        "searchText": "search term",           // Global search text
        "columns": ["name", "description"]     // Fields to search (optional)
    },
    "columns": [
        {
            "columnName": "fieldName",         // Entity field name
            "filter": "operator:value",        // Column-specific filter
            "sort": "asc"                      // Sort direction (asc/desc)
        }
    ]
}
```

### TypeScript Interface
```typescript
interface DataTableRequest {
    pagination: {
        page: number;        // 0-based page index
        size: number;        // 10-50 records per page
    };
    searchFilter?: {
        searchText: string;  // Global search term
        columns?: string[];  // Fields to search (optional)
    };
    columns?: {
        columnName: string;  // Entity field name
        filter?: string;     // Format: "operator:value"
        sort?: "asc" | "desc";
    }[];
}
```

---

## 🏗️ Architecture Overview

### Core Components

#### 1. 🎯 **DynamicSearchQueryBuilder** - The Search Engine (700+ lines)
**Purpose**: Automatically generates optimized JPQL queries with advanced performance features
**Key Capabilities**:
- **Automatic JPQL Generation**: Converts DataTableRequest to optimized queries using `entity.getEntityName()`
- **Smart Field Resolution**: Uses `entity.getFieldAliases()` to map UI names to DB fields
- **Intelligent Joins**: Applies `entity.getFetchJoins()` for eager loading and N+1 prevention
- **DISTINCT Optimization**: Uses `entity.shouldUseDistinct()` for collection joins when needed

**Performance Optimizations**:
- **Static Caching**: `ConcurrentHashMap<String, LocalDateTime>` for O(1) date parsing
- **Pre-compiled DateTimeFormatter Arrays**: Reduces allocation overhead for common formats
- **Java 17+ Features**: Switch expressions, parallel streams, concurrent collections
- **Fast-path Optimization**: Direct pattern matching for common date formats (yyyy-MM-dd, dd/MM/yyyy)
- **Memory Efficiency**: Static formatters and cached date parsing minimize GC pressure

**Modern Implementation Details**:
```java
// Uses entity configuration methods extensively:
jpql.append("SELECT e FROM ").append(entity.getEntityName()).append(" e ");
String actualField = entity.getFieldAliases().getOrDefault(field, field);
Set<String> joinPaths = entity.getFetchJoins();
boolean useDistinct = entity.shouldUseDistinct();
```

#### 2. 🔧 **SearchableEntity Interface** - Configuration Contract
**Purpose**: Define comprehensive search configuration for any entity
**Complete Method Signature**:

```java
public interface SearchableEntity<T> {
    // ✅ REQUIRED METHODS (4)
    Set<String> getSearchableFields();     // Fields that support global search
    Set<String> getSortableFields();       // Fields that support column sorting
    Set<String> getDefaultSearchColumns(); // Fields used when no columns specified
    Class<T> getEntityClass();             // Entity class for type safety
    
    // ✅ OPTIONAL METHODS (7) - With intelligent defaults
    default String getEntityName() {       // Entity name for JPQL queries
        return getEntityClass().getSimpleName();
    }
    
    default Set<String> getFetchJoins() {  // Relations to fetch eagerly (prevents N+1)
        return Set.of();
    }
    
    default boolean shouldUseDistinct() {  // Use DISTINCT for collection joins
        return false;
    }
    
    default Map<String, String> getFieldAliases() { // Map UI names to DB fields
        return Map.of();
    }
    
    default List<String> getDefaultSortFields() {   // Default sorting when none specified
        return List.of(); // Empty = fallback to "ORDER BY e.id ASC"
    }
    
    // ✅ NEW: SUBQUERY OPTIMIZATION METHODS (2)
    default boolean useSubqueryForField(String field) {  // Use EXISTS subquery for large collections
        return false; // Default: use standard JOINs
    }
    
    default String getSubqueryCondition(String field, String operation, String value) {
        return null; // Default: no custom subquery (use standard JOIN)
    }
}
```

**Total Interface Methods**: **11 methods** (4 required + 7 with defaults)

**NEW: Subquery Optimization Methods**:
- `useSubqueryForField(String field)`: Returns true if field should use EXISTS subquery instead of JOIN
- `getSubqueryCondition(String field, String operation, String value)`: Returns custom EXISTS subquery template

**When to Use Subquery Optimization**:
- ✅ **Large Collections**: Entity has 1000+ related records (users with many companies)
- ✅ **Search Performance**: Need to search in large related collections
- ✅ **Memory Efficiency**: Avoid loading thousands of related entities
- ❌ **Small Collections**: Use standard JOINs for < 100 related records

#### 3. 🔍 **FilterParser** - Advanced Query Understanding
**Purpose**: Parse and validate complex filter expressions with 20+ operators
**Supported Operations**:
- **String Operations**: `cnt`, `ncnt`, `sw`, `ew`, `eq`, `ne`
- **Numeric Operations**: `gt`, `gte`, `lt`, `lte`, `eq`, `ne` (auto-detects numeric vs string)
- **Date Operations**: `dgt`, `dgte`, `dlt`, `dlte`, `deq`, `dne`, `dbetween`
- **List Operations**: `in` (comma-separated values)

**Smart Features**:
- **Auto Type Detection**: Automatically detects numeric vs string operations
- **Graceful Fallback**: Invalid operators fall back to string contains
- **Validation**: Ensures only fields from `entity.getSearchableFields()` are processed
- **Security**: Built-in SQL injection prevention with parameterized queries

#### 4. 🔧 **UniversalSearchFrameworkConfig** - Auto-Registration System
**Purpose**: Automatically discover and register all SearchableEntity implementations
**Implementation**:
```java
@Configuration
public class UniversalSearchFrameworkConfig {
    
    @PostConstruct
    public void registerSearchableEntities() {
        // Automatically finds and registers ALL @Component classes 
        // that implement SearchableEntity<T>
        // No manual registration needed!
    }
}
```

#### 5. 🏭 **BaseService Integration** - Service Layer Bridge
**Purpose**: Seamless integration between framework and service layer
**Key Method**:
```java
// Override this method in your service to enable framework
protected SearchableEntity<T> getSearchableEntity() {
    return yourEntitySearchConfig; // Enable Universal Search Framework
}
```

---

## 🚀 Advanced Features & Real Implementation Examples

## 🚀 Advanced Features & Real Implementation Examples

### ✨ **Subquery Optimization for Large Collections**
**Purpose**: Handle entities with thousands of related records using EXISTS subqueries instead of expensive JOINs
**Use Case**: Users with 10,000+ company assignments, products with many categories, etc.

```java
@Component
public class UserSearchConfig implements SearchableEntity<MasterUser> {
    
    // Standard configuration methods...
    
    /**
     * PERFORMANCE OPTIMIZATION: Use EXISTS subquery for company fields.
     * Instead of JOIN FETCH (loads all 10,000 companies), uses EXISTS (checks existence only).
     * 
     * Performance Impact:
     * - Before: JOIN companies → Loads 10,000+ company entities per user
     * - After: EXISTS subquery → Just checks if user has companies (fast)
     */
    @Override
    public boolean useSubqueryForField(String field) {
        return field.startsWith("companies."); // Companies fields use subquery
    }
    
    /**
     * PERFORMANCE OPTIMIZATION: Generate EXISTS subquery for company name search.
     * Enables efficient search by company name without loading all company entities.
     * 
     * Example Generated Query:
     * SELECT u FROM MasterUser u 
     * WHERE EXISTS (
     *     SELECT 1 FROM MapUserCompany muc 
     *     JOIN muc.company c 
     *     WHERE muc.user = u 
     *     AND c.companyName LIKE '%searchText%'
     * )
     */
    @Override
    public String getSubqueryCondition(String field, String operation, String value) {
        if ("companies.companyName".equals(field)) {
            return """
                EXISTS (
                    SELECT 1 FROM MapUserCompany muc 
                    JOIN muc.company c 
                    WHERE muc.user = e 
                    AND %s
                )
                """;
        }
        return null; // Use default JOIN approach
    }
    
    @Override
    public Set<String> getFetchJoins() {
        // REMOVED: "companies" - No longer fetch companies eagerly (too expensive)
        return Set.of("assignedRole", "updatedByUser"); // Only lightweight relations
    }
}
```

**Automatic Framework Integration**:
```java
// DynamicSearchQueryBuilder automatically detects and uses subqueries
public class DynamicSearchQueryBuilder {
    
    /**
     * Framework automatically checks if field should use subquery optimization
     */
    private boolean usesSubqueryForRelatedEntity(SearchableEntity<?> entity, String field) {
        return entity.useSubqueryForField(field);
    }
    
    /**
     * Generates optimized query based on field type
     */
    private void buildSearchCondition(String field, String value) {
        if (usesSubqueryForRelatedEntity(entity, field)) {
            // Use EXISTS subquery (fast for large collections)
            String subqueryTemplate = entity.getSubqueryCondition(field, "LIKE", value);
            // Build optimized EXISTS subquery...
        } else {
            // Use standard JOIN (efficient for small collections)
            // Build standard JOIN condition...
        }
    }
}
```

**Performance Benefits**:
- ✅ **Memory Efficiency**: Avoids loading 10,000+ entities per result row
- ✅ **Query Performance**: EXISTS subquery vs expensive JOIN FETCH  
- ✅ **Scalability**: Handles users with any number of companies efficiently
- ✅ **Automatic Detection**: Framework applies optimization transparently

### ✨ **Batch Operations for Related Data**
**Purpose**: Fetch related data counts/info in batches instead of N+1 queries
**Implementation**: Service layer optimization for display data

```java
@Service
public class UserManagementServiceImpl {
    
    /**
     * OPTIMIZATION: Batch fetch company counts for paginated results.
     * Instead of 50 individual COUNT queries, execute 1 GROUP BY query.
     */
    public PaginationResponse<SearchUsersResponseDto> getUsers(DataTableRequest dt) {
        // 1. Execute optimized search (uses EXISTS subquery for company search)
        Page<MasterUser> page = queryBuilder.findWithDynamicSearch(userSearchConfig, dt);
        
        // 2. BATCH OPTIMIZATION: Get company counts for all users in one query
        List<Integer> userIds = page.getContent().stream()
                .map(MasterUser::getId)
                .toList();
        
        Map<Integer, Long> companyCounts = getCompanyCountsForUsers(userIds);
        
        // 3. Convert to DTOs using pre-fetched counts (no more N+1 queries)
        List<SearchUsersResponseDto> content = page.getContent().stream()
                .map(u -> convertToSearchUsersResponseDto(u, companyCounts.getOrDefault(u.getId(), 0L)))
                .toList();
        
        // 4. Return optimized response
        return buildPaginationResponse(content, page);
    }
    
    /**
     * BATCH OPERATION: Single GROUP BY query instead of N individual queries.
     * Performance: 50 queries → 1 query
     */
    private Map<Integer, Long> getCompanyCountsForUsers(List<Integer> userIds) {
        if (userIds.isEmpty()) return Map.of();
        
        // Repository method: SELECT userId, COUNT(*) FROM MapUserCompany WHERE userId IN (...) GROUP BY userId
        List<Object[]> results = mapRepo.countCompaniesByUserIds(userIds);
        
        return results.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0],  // userId
                    row -> (Long) row[1]      // company count
                ));
    }
}
```

**Repository Enhancement**:
```java
@Repository
public interface MapUserCompanyRepository extends JpaRepository<MapUserCompany, Integer> {
    
    /**
     * BATCH OPTIMIZATION: Get company counts for multiple users in single query.
     * Replaces individual countByUserId() calls with batch GROUP BY operation.
     */
    @Query("""
        SELECT muc.user.id, COUNT(muc.company.id) 
        FROM MapUserCompany muc 
        WHERE muc.user.id IN :userIds 
        GROUP BY muc.user.id
        """)
    List<Object[]> countCompaniesByUserIds(@Param("userIds") List<Integer> userIds);
}
```

### ✨ **Base Query Extension Support**
**Purpose**: Add search/sort functionality to existing complex queries
**Use Case**: When you already have a sophisticated base query and want to add framework search capabilities

```java
// Your existing complex query
String complexBaseQuery = """
    SELECT DISTINCT e FROM Role e 
    LEFT JOIN e.permissions p 
    LEFT JOIN e.userRoles ur 
    LEFT JOIN ur.user u 
    WHERE e.isActive = true 
    AND p.module = 'ADMIN' 
    AND u.department = 'IT'
    """;

// Add search and sorting to your complex query
Page<Role> results = dynamicSearchQueryBuilder.findWithDynamicSearch(
    roleSearchConfig, 
    request, 
    complexBaseQuery  // ← Your base query gets enhanced!
);
```

**Framework automatically adds:**
- ✅ **Search conditions**: `AND (field1 LIKE '%search%' OR field2 LIKE '%search%')`
- ✅ **Column filters**: `AND field3 > value AND field4 = 'status'`
- ✅ **Dynamic sorting**: `ORDER BY field1 ASC, field2 DESC`
- ✅ **Pagination**: Automatically handled with count query

**Benefits**:
- ✅ **Keep existing logic**: Your complex WHERE clauses remain intact
- ✅ **Add framework power**: Get search/sort/pagination automatically
- ✅ **No query rewrite**: Framework intelligently extends your query
- ✅ **Performance maintained**: Uses same JOINs and optimizations

### Complete SearchableEntity Implementation (From RoleSearchConfig)
```java
@Component
public class RoleSearchConfig implements SearchableEntity<Role> {
    
    @Override
    public Set<String> getSearchableFields() {
        return Set.of(
            // Basic entity fields
            "name", "description", "isActive",
            // Related entity fields (automatic joins)
            "roleTypeConfig.name", "roleTypeConfig.description",
            "landingPageConfig.name", "landingPageConfig.description"
        );
    }
    
    @Override
    public Set<String> getSortableFields() {
        return Set.of(
            "id", "name", "description", "isActive", "createdOn", "updatedOn",
            "roleTypeConfig.name", "landingPageConfig.name"
        );
    }
    
    @Override
    public Set<String> getDefaultSearchColumns() {
        return Set.of("name", "description");
    }
    
    @Override
    public Class<Role> getEntityClass() {
        return Role.class;
    }
    
    @Override
    public Set<String> getFetchJoins() {
        // Prevents N+1 queries for related entities
        return Set.of("roleTypeConfig", "landingPageConfig");
    }
    
    @Override
    public boolean shouldUseDistinct() {
        return true; // Required when fetching related entities with filtering
    }
    
    @Override
    public Map<String, String> getFieldAliases() {
        return Map.of(
            "roleName", "name",
            "roleDescription", "description",
            "roleType", "roleTypeConfig.name",
            "landingPage", "landingPageConfig.name"
        );
    }
}
```

### Automatic Framework Integration
The framework uses sophisticated auto-discovery and integration:

```java
// 1. AUTO-REGISTRATION: UniversalSearchFrameworkConfig finds all SearchableEntity implementations
@PostConstruct
public void discoverSearchableEntities() {
    // Automatically registers: RoleSearchConfig, UserSearchConfig, etc.
}

// 2. SERVICE INTEGRATION: BaseService uses SearchableEntity
public PaginationResponse<DTO> search(DataTableRequest request) {
    SearchableEntity<ENTITY> searchConfig = getSearchableEntity();
    // Use Universal Search Framework with full optimization
    return dynamicSearchQueryBuilder.findWithDynamicSearch(request, searchConfig);
}

// 3. QUERY GENERATION: DynamicSearchQueryBuilder uses ALL interface methods
String jpql = "SELECT " + (entity.shouldUseDistinct() ? "DISTINCT " : "") + 
              "e FROM " + entity.getEntityName() + " e ";

// Apply field aliases for client-friendly naming
String actualField = entity.getFieldAliases().getOrDefault(clientField, clientField);

// Add performance-optimized joins
entity.getFetchJoins().forEach(join -> 
    jpql.append(" LEFT JOIN FETCH e.").append(join));
```
```

### Date Format Support
The framework automatically handles multiple date formats:
- `yyyy-MM-dd` (2024-01-15)
- `yyyy-MM-dd HH:mm:ss` (2024-01-15 14:30:45)
- `dd/MM/yyyy` (15/01/2024)
- ISO format: `yyyy-MM-ddTHH:mm:ss`

### Smart Type Detection
```java
// These all work automatically:
"score:gt:85"        // Numeric comparison
"score:eq:perfect"   // Falls back to string if not numeric
"count:in:1,2,3,4"   // List of numbers
"status:in:ACTIVE,PENDING,DRAFT" // List of strings
```

---

---

## 🛡️ Security & Validation

### Built-in Protection & Framework Intelligence
- ✅ **SQL Injection Prevention**: All parameters are properly escaped using JPA parameterized queries
- ✅ **Field Validation**: Only fields from `getSearchableFields()` are processed (others ignored with warning)
- ✅ **Type Safety**: Automatic validation and graceful fallback for invalid operations
- ✅ **Error Handling**: Comprehensive exception handling with meaningful error messages
- ✅ **Auto-Discovery Security**: Only `@Component` annotated configurations are registered

### Real Validation Examples (From Implementation)
```java
// ✅ FIELD SECURITY: Framework validates against configured fields
@Override
public Set<String> getSearchableFields() {
    return Set.of("name", "description", "status"); // Only these can be searched
}

// ❌ MALICIOUS REQUEST: Automatically filtered out
{
    "columns": [
        { "columnName": "password", "filter": "eq:hack" }  // ← Ignored with warning
    ]
}

// ✅ SAFE QUERY GENERATION: Framework uses parameterized queries
// Generated JPQL: "WHERE e.name LIKE :param1" 
// Parameters: {"param1": "%admin%"}

// ✅ TYPE VALIDATION: Invalid operators gracefully handled
{ "columnName": "score", "filter": "invalidOp:100" }  // ← Falls back to contains
```

### Framework Auto-Registration Security
```java
// ✅ CONTROLLED REGISTRATION: Only @Component classes are auto-discovered
@Component // ← Required for auto-registration
public class SecureEntityConfig implements SearchableEntity<Entity> {
    // This gets auto-registered by UniversalSearchFrameworkConfig
}

// ❌ NOT REGISTERED: Classes without @Component are ignored
public class UnsafeConfig implements SearchableEntity<Entity> {
    // This will NOT be auto-registered (safe by design)
}
```

---

## 📋 Implementation Patterns

### Minimal Configuration (4 Required Methods Only)
```java
@Component
public class SimpleEntityConfig implements SearchableEntity<SimpleEntity> {
    
    // ✅ REQUIRED: Minimum viable configuration
    @Override
    public Set<String> getSearchableFields() { 
        return Set.of("name"); 
    }
    
    @Override
    public Set<String> getSortableFields() { 
        return Set.of("id", "name"); 
    }
    
    @Override
    public Set<String> getDefaultSearchColumns() { 
        return Set.of("name"); 
    }
    
    @Override
    public Class<SimpleEntity> getEntityClass() { 
        return SimpleEntity.class; 
    }
    
    // ✅ AUTOMATIC: All other methods use intelligent defaults:
    // - getEntityName() → "SimpleEntity" (class simple name)
    // - getFetchJoins() → Set.of() (no eager joins)
    // - shouldUseDistinct() → false (no distinct needed)
    // - getFieldAliases() → Map.of() (no field mapping)
}
```

### Full-Featured Configuration (All 8 Methods)
```java
@Component
public class ComplexEntityConfig implements SearchableEntity<ComplexEntity> {
    
    // ✅ REQUIRED METHODS (4)
    @Override
    public Set<String> getSearchableFields() {
        return Set.of("name", "description", "category.name", "tags.name");
    }
    
    @Override
    public Set<String> getSortableFields() {
        return Set.of("id", "name", "createdOn", "category.name");
    }
    
    @Override
    public Set<String> getDefaultSearchColumns() {
        return Set.of("name", "description");
    }
    
    @Override
    public Class<ComplexEntity> getEntityClass() { 
        return ComplexEntity.class; 
    }
    
    // ✅ OPTIONAL METHODS (4) - Advanced features
    @Override
    public String getEntityName() {
        return "ComplexEntity"; // Custom entity name for JPQL
    }
    
    @Override
    public Set<String> getFetchJoins() {
        return Set.of("category", "tags"); // Optimize joins, prevent N+1
    }
    
    @Override
    public boolean shouldUseDistinct() {
        return true; // Required when fetch joining collections like "tags"
    }
    
    @Override
    public Map<String, String> getFieldAliases() {
        return Map.of(
            "categoryName", "category.name",    // UI field → DB field
            "tagNames", "tags.name",           // Collection field mapping
            "displayName", "name"              // Simple field alias
        );
    }
}
```

---

## 🔧 Troubleshooting & Implementation Guide

### Common Issues & Solutions

#### ❌ "Search Not Working" - Framework Not Activated
**Problem**: Search returns empty or framework not working properly
**Root Cause**: `getSearchableEntity()` not overridden in service
**Solution**:
```java
// ❌ WRONG: Framework not enabled
@Service
public class YourService extends BaseService<Entity, EntityDto> {
    // Missing getSearchableEntity() override
}

// ✅ CORRECT: Framework enabled
@Service  
public class YourService extends BaseService<Entity, EntityDto> {
    @Autowired
    private YourEntitySearchConfig searchConfig;
    
    @Override
    protected SearchableEntity<Entity> getSearchableEntity() {
        return searchConfig; // Framework activates automatically
    }
}
```

#### ❌ "Field Not Searchable" - Configuration Missing
**Problem**: Specific fields don't work in search/sort
**Diagnosis**: Check if field is in configuration sets
**Solution**:
```java
@Override
public Set<String> getSearchableFields() {
    return Set.of("name", "description", "category.name"); // ← Add missing field
}

@Override
public Set<String> getSortableFields() {
    return Set.of("id", "name", "createdOn", "category.name"); // ← Add for sorting
}
```

#### ❌ "N+1 Query Performance Issues"
**Problem**: Multiple database queries for related entities
**Diagnosis**: Check query logs for multiple SELECT statements
**Solution**:
```java
@Override
public Set<String> getFetchJoins() {
    return Set.of("category", "assignedUser", "tags"); // ← Add all related entities
}

@Override
public boolean shouldUseDistinct() {
    return true; // ← Required when joining collections
}
```

#### ❌ "Date Filters Not Working"
**Problem**: Date filtering returns no results or errors
**Solution**: Use correct date operators and format
```java
// ✅ CORRECT: Use date-specific operators
{ "columnName": "createdOn", "filter": "dgte:2024-01-01" }
{ "columnName": "updatedOn", "filter": "dbetween:2024-01-01,2024-12-31" }

// ❌ WRONG: Using string operators on dates
{ "columnName": "createdOn", "filter": "gte:2024-01-01" } // Use 'dgte' not 'gte'
```

#### ❌ "Field Aliases Not Working"
**Problem**: UI field names don't map to database fields
**Solution**: Implement `getFieldAliases()` correctly
```java
@Override
public Map<String, String> getFieldAliases() {
    return Map.of(
        "userName", "user.name",           // UI field → DB path
        "categoryName", "category.name",   // Nested entity field
        "displayName", "name"              // Simple alias
    );
}

// Frontend can now use friendly names:
{ "columnName": "userName", "filter": "sw:John" } // Maps to user.name
```

### Framework Diagnostic Commands

#### Check if SearchableEntity is Registered
```java
// Add this to your service for debugging:
@PostConstruct
public void debugSearchConfig() {
    SearchableEntity<?> config = getSearchableEntity();
    if (config != null) {
        log.info("✅ Framework ENABLED for {}", config.getEntityClass().getSimpleName());
        log.info("Searchable fields: {}", config.getSearchableFields());
        log.info("Sortable fields: {}", config.getSortableFields());
    } else {
        log.warn("❌ Framework DISABLED - no SearchableEntity configuration found");
    }
}
```

#### Test Query Generation
```java
// Add temporary debug logging in DynamicSearchQueryBuilder:
log.info("Generated JPQL: {}", jpql.toString());
log.info("Parameters: {}", parameters);
log.info("Using entity: {}", entity.getEntityName());
log.info("Fetch joins: {}", entity.getFetchJoins());
```

### Performance Optimization Checklist

#### ✅ Database Level
- [ ] Add indexes on frequently searched fields
- [ ] Add composite indexes for common filter combinations
- [ ] Monitor slow query logs for optimization opportunities

#### ✅ Configuration Level  
- [ ] Use `getFetchJoins()` for all related entities used in search/display
- [ ] Set `shouldUseDistinct(true)` only when necessary (collection joins)
- [ ] Limit `getSearchableFields()` to actually needed fields
- [ ] Use field aliases to avoid exposing internal field names

#### ✅ Query Level
- [ ] Use pagination with reasonable page sizes (10-50)
- [ ] Prefer specific field searches over global text search when possible
- [ ] Use date range operators instead of multiple date comparisons

### Advanced Debugging

#### Enable SQL Logging
```yaml
# application.yml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    com.itt.service.fw.search: DEBUG
```

#### Framework Health Check Endpoint
```java
@RestController
public class SearchFrameworkController {
    
    @GetMapping("/admin/search-framework/status")
    public Map<String, Object> getFrameworkStatus() {
        return Map.of(
            "registeredEntities", universalSearchFrameworkConfig.getRegisteredEntities(),
            "cacheStats", dynamicSearchQueryBuilder.getCacheStatistics(),
            "performanceMetrics", dynamicSearchQueryBuilder.getPerformanceMetrics()
        );
    }
}

---

## 📊 Operator Reference Card

| Operation | Operator | Example | Description |
|-----------|----------|---------|-------------|
| **String** | | | |
| Contains | `cnt` or none | `admin` or `cnt:admin` | Field contains text |
| Not Contains | `ncnt` | `ncnt:test` | Field doesn't contain text |
| Starts With | `sw` | `sw:Admin` | Field starts with text |
| Ends With | `ew` | `ew:user` | Field ends with text |
| Equals | `eq` | `eq:ACTIVE` | Field exactly equals |
| Not Equals | `ne` | `ne:INACTIVE` | Field doesn't equal |
| **Numeric** | | | |
| Greater Than | `gt` | `gt:100` | Number > value |
| Greater/Equal | `gte` | `gte:50` | Number >= value |
| Less Than | `lt` | `lt:200` | Number < value |
| Less/Equal | `lte` | `lte:75` | Number <= value |
| **Date** | | | |
| After | `dgt` | `dgt:2024-01-01` | Date > value |
| On/After | `dgte` | `dgte:2024-01-01` | Date >= value |
| Before | `dlt` | `dlt:2024-12-31` | Date < value |
| On/Before | `dlte` | `dlte:2024-12-31` | Date <= value |
| Date Equals | `deq` | `deq:2024-06-15` | Date = value |
| Date Not Equal | `dne` | `dne:2024-12-25` | Date != value |
| Date Range | `dbetween` | `dbetween:2024-01-01,2024-12-31` | Date in range |
| **List** | | | |
| In List | `in` | `in:RED,BLUE,GREEN` | Value in list |

---

## 🎓 Best Practices

### 1. **Start Simple, Scale Up**
```java
// Start with basic fields
getSearchableFields() → Set.of("name", "description")

// Add complexity as needed  
getSearchableFields() → Set.of("name", "description", "category.name", "tags.name")
```

### 2. **Optimize for Performance**
```java
// Always include fetch joins for related entities
@Override
public Set<String> getFetchJoins() {
    return Set.of("category", "assignedUser"); 
}
```

### 3. **Use Meaningful Aliases**
```java
// Map UI-friendly names to database fields
@Override
public Map<String, String> getFieldAliases() {
    return Map.of(
        "assignedTo", "assignedUser.name",
        "categoryName", "category.displayName"
    );
}
```

### 4. **Test Edge Cases**
```java
// Test these scenarios:
// - Empty search terms
// - Invalid field names
// - Invalid operators  
// - Invalid date formats
// - Very large result sets
// - Subquery optimization for large collections
// - Batch operations performance
```

---

## 🌟 Framework Philosophy

### **Universal Search Framework with Advanced Optimization**
This framework is designed as a **single, unified solution** for all search needs, now enhanced with advanced performance optimizations for large-scale applications. There's no legacy code or dual approaches - just one powerful, clean, and optimized framework that handles:

✅ **Simple Searches**: Basic text search across configured fields  
✅ **Complex Filtering**: Advanced operators for strings, numbers, dates, and lists  
✅ **Dynamic Sorting**: Multi-column sorting with configurable fields  
✅ **Smart Pagination**: Optimized count queries and page handling  
✅ **Performance Optimization**: Automatic fetch joins and N+1 prevention  
✅ **Base Query Extension**: Add search capabilities to existing complex queries  
✅ **Type Safety**: Compile-time validation and runtime error handling  
✅ **Subquery Optimization**: EXISTS subqueries for large collections (10,000+ records)  
✅ **Batch Operations**: Single GROUP BY queries instead of N individual queries  

### **Key Design Principles**
1. **Configuration Over Code**: Simple interface implementation vs complex query building
2. **Performance First**: Built-in optimizations for real-world usage including subquery optimization
3. **Type Safety**: Leverages Java generics and Spring's type system
4. **Auto-Discovery**: Zero manual registration required
5. **Extensibility**: Easy to add new operators and functionality
6. **Security**: Built-in SQL injection prevention and field validation
7. **Scalability**: Handles entities with thousands of related records efficiently

---

**🎉 Congratulations!** You now have a powerful, enterprise-grade search system that can handle any search requirement with minimal code and maximum performance!
