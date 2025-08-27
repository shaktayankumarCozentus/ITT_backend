package com.itt.service.fw.search;

import com.itt.service.dto.DataTableRequest;
import com.itt.service.validator.DataTableRequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Framework component for building dynamic search queries for any entity.
 * Automatically generates JPQL queries based on SearchableEntity configuration.
 * 
 * MODERN JAVA OPTIMIZATIONS:
 * - Static caching for O(1) date parsing lookups
 * - Pre-compiled DateTimeFormatter arrays for better performance
 * - Parallel stream processing for large datasets
 * - Fast-path optimization for common date formats
 * 
 * SECURITY ENHANCEMENTS:
 * - Strict field validation with ValidationException on invalid fields
 * - No more silent skipping of invalid fields
 * - DTO field name enforcement (mandatory standard)
 * - SQL injection prevention through field whitelisting
 */
@Component
public class DynamicSearchQueryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DynamicSearchQueryBuilder.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    private final DataTableRequestValidator requestValidator;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param requestValidator Validator for DataTableRequest validation
     */
    public DynamicSearchQueryBuilder(DataTableRequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }
    
    // ==========================================
    // MODERN JAVA OPTIMIZATION: Static Caching for O(1) Lookups
    // ==========================================
    
    /**
     * Pre-computed DateTimeFormatter array for O(1) access instead of creating new array each time.
     * PERFORMANCE: Reduces memory allocation and improves cache locality.
     * NOTE: All date inputs are normalized to date-only (00:00:00 time) for consistent comparison.
     */
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),     // 2023-12-25 14:30:00 -> 2023-12-25 00:00:00
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),        // 2023-12-25 14:30 -> 2023-12-25 00:00:00
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),              // 2023-12-25 -> 2023-12-25 00:00:00
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),     // 25/12/2023 14:30:00 -> 2023-12-25 00:00:00
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),        // 25/12/2023 14:30 -> 2023-12-25 00:00:00
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),              // 25/12/2023 -> 2023-12-25 00:00:00
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),   // 2023-12-25T14:30:00 -> 2023-12-25 00:00:00
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS") // 2023-12-25T14:30:00.123 -> 2023-12-25 00:00:00
    };
    
    /**
     * Thread-safe cache for parsed dates to avoid repeated parsing.
     * PERFORMANCE: O(1) lookup for previously parsed dates vs O(n) parsing each time.
     * SIZE LIMIT: Bounded to 1000 entries to prevent memory leaks.
     */
    private static final Map<String, LocalDateTime> DATE_CACHE = new ConcurrentHashMap<>(256);
    
    /**
     * Set of date-only patterns for O(1) lookup instead of string contains checks.
     * PERFORMANCE: Set.contains() is O(1) vs String.contains() operations.
     */
    private static final Set<String> DATE_ONLY_PATTERNS = Set.of(
        "yyyy-MM-dd", "dd/MM/yyyy"
    );

    /**
     * Execute dynamic search for any entity with automatic query generation.
     * 
     * @param <T> Entity type
     * @param searchableEntity Entity configuration
     * @param request DataTable request with search/sort parameters
     * @return Page of results with all related entities fetched
     */
    public <T> Page<T> findWithDynamicSearch(SearchableEntity<T> searchableEntity, DataTableRequest request) {
        return findWithDynamicSearch(searchableEntity, request, null);
    }
    
    /**
     * Execute dynamic search with base query support - ENHANCED VERSION
     * 
     * @param <T> Entity type
     * @param searchableEntity Entity configuration
     * @param request DataTable request with search/sort parameters
     * @param baseQuery Optional base JPQL query to extend (null for standard behavior)
     * @return Page of results with search/sort applied to base query
     */
    public <T> Page<T> findWithDynamicSearch(SearchableEntity<T> searchableEntity, DataTableRequest request, String baseQuery) {
        
        // SECURITY: Validate request fields before processing
        if (request != null) {
            requestValidator.validateRequest(request, searchableEntity);
            logger.debug("DataTableRequest validation passed for entity {}", searchableEntity.getEntityClass().getSimpleName());
        } else {
            throw new IllegalArgumentException("DataTableRequest cannot be null");
        }
        
        try {
            // Extract search parameters
            String searchText = extractSearchText(request);
            List<String> searchColumns = extractSearchColumns(request);
            Pageable pageable = request.toPageable();
            
            // Build and execute query - ENHANCED: Support base query
            String jpql;
            if (baseQuery != null && !baseQuery.trim().isEmpty()) {
                jpql = buildSearchQueryFromBase(searchableEntity, searchText, searchColumns, request, baseQuery);
                logger.debug("Enhanced JPQL from base query for {}: {}", searchableEntity.getEntityClass().getSimpleName(), jpql);
            } else {
                jpql = buildSearchQuery(searchableEntity, searchText, searchColumns, request);
                logger.debug("Generated JPQL for {}: {}", searchableEntity.getEntityClass().getSimpleName(), jpql);
            }
            
            TypedQuery<T> query = entityManager.createQuery(jpql, searchableEntity.getEntityClass());
            
            // Set parameters
            setQueryParameters(query, searchableEntity, searchText, searchColumns, request);
            
            // Apply pagination
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
            
            List<T> results = query.getResultList();
            
            // Get total count for pagination - ENHANCED: Support base query
            long totalCount;
            if (baseQuery != null && !baseQuery.trim().isEmpty()) {
                totalCount = getTotalCountFromBase(searchableEntity, searchText, searchColumns, request, baseQuery);
            } else {
                totalCount = getTotalCount(searchableEntity, searchText, searchColumns, request);
            }
            
            logger.debug("Universal search for {} returned {} results (total: {})", 
                        searchableEntity.getEntityClass().getSimpleName(), results.size(), totalCount);
            
            return new org.springframework.data.domain.PageImpl<>(results, pageable, totalCount);
            
        } catch (Exception e) {
            logger.error("Error executing dynamic search for entity {}: {}", 
                        searchableEntity.getEntityClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Search execution failed for " + searchableEntity.getEntityClass().getSimpleName(), e);
        }
    }

    /**
     * Build dynamic JPQL query based on entity configuration
     */
    private <T> String buildSearchQuery(SearchableEntity<T> entity, String searchText, List<String> searchColumns, DataTableRequest request) {
        StringBuilder jpql = new StringBuilder();

        // Base SELECT with optional DISTINCT based on entity configuration
        if (entity.shouldUseDistinct()) {
            jpql.append("SELECT DISTINCT e FROM ").append(entity.getEntityName()).append(" e ");
        } else {
            jpql.append("SELECT e FROM ").append(entity.getEntityName()).append(" e ");
        }

        // Add LEFT JOIN FETCH for configured fetch joins
        Set<String> fetchJoins = entity.getFetchJoins();
        for (String fetchJoin : fetchJoins) {
            jpql.append("LEFT JOIN FETCH e.").append(fetchJoin).append(" ");
        }

        // Add LEFT JOIN for search-related entities that aren't already fetch joined AND don't use subqueries
        Set<String> relatedEntities = detectRelatedEntities(entity.getSearchableFields());
        for (String relatedEntity : relatedEntities) {
            if (!fetchJoins.contains(relatedEntity) && !usesSubqueryForRelatedEntity(entity, relatedEntity)) {
                jpql.append("LEFT JOIN e.").append(relatedEntity).append(" ");
            }
        }

        // Build WHERE clause
        List<String> whereConditions = new ArrayList<>();
        
        // Add global search condition
        if (searchText != null && !searchText.trim().isEmpty()) {
            List<String> fieldsToSearch = determineSearchFields(entity, searchColumns);
            List<String> searchConditions = new ArrayList<>();

            for (String field : fieldsToSearch) {
                // OPTIMIZATION: Check if field should use EXISTS subquery
                if (entity.useSubqueryForField(field)) {
                    String subqueryCondition = entity.getSubqueryCondition(field, "searchText");
                    if (subqueryCondition != null) {
                        searchConditions.add(subqueryCondition);
                        logger.debug("Using subquery for field '{}' in entity {}", field, entity.getEntityClass().getSimpleName());
                        continue;
                    }
                }
                
                // Use normal JOIN condition for other fields
				Class<?> fieldType = entity.getFieldType(field);
				String qualifiedField = getQualifiedFieldReference(field);
				if (fieldType != null && fieldType == String.class) {
					searchConditions.add("LOWER(" + qualifiedField + ") LIKE LOWER(CONCAT('%', :searchText, '%'))");
				} else {
					// Handle null fieldType or non-String types with safe conversion
					// Use CONCAT for safer database compatibility instead of CAST AS string
					searchConditions.add("CONCAT('', " + qualifiedField + ") LIKE CONCAT('%', :searchText, '%')");
				}
			}
            
            if (!searchConditions.isEmpty()) {
                whereConditions.add("(" + String.join(" OR ", searchConditions) + ")");
            }
        }
        
        // Add column-specific filter conditions
        List<String> columnFilterConditions = buildColumnFilterConditions(entity, request);
        whereConditions.addAll(columnFilterConditions);
        
        // Apply WHERE clause if any conditions exist
        if (!whereConditions.isEmpty()) {
            jpql.append("WHERE ").append(String.join(" AND ", whereConditions));
        }

        // Build ORDER BY clause from request columns
        // MODERN JAVA OPTIMIZATION: Parallel stream processing for large column sets
        List<DataTableRequest.Column> sortColumns = request.getColumns().parallelStream()
                .filter(col -> col.getSort() != null && !col.getSort().isBlank())
                .collect(Collectors.toList());

        if (!sortColumns.isEmpty()) {
            // Build sort clauses from validated columns
            List<String> validSortClauses = new ArrayList<>();
            
            for (DataTableRequest.Column col : sortColumns) {
                String field = col.getColumnName();
                
                // Use alias if present
                String actualField = entity.getFieldAliases().getOrDefault(field, field);
                
                // NOTE: Sort field validation removed - handled by DataTableRequestValidator
                // All fields are guaranteed to be valid at this point
                
                StringBuilder sortClause = new StringBuilder();
                if (actualField.contains(".")) {
                    sortClause.append(actualField);
                } else {
                    sortClause.append("e.").append(actualField);
                }
                sortClause.append(" ").append(col.getSort().equalsIgnoreCase("desc") ? "DESC" : "ASC");
                validSortClauses.add(sortClause.toString());
            }
            
            if (!validSortClauses.isEmpty()) {
                jpql.append(" ORDER BY ").append(String.join(", ", validSortClauses));
            } else {
                // Apply default sort if configured, otherwise fallback to id
                applyDefaultSort(jpql, entity);
            }
        } else {
            // Apply default sort if configured, otherwise fallback to id
            applyDefaultSort(jpql, entity);
        }

        return jpql.toString();
    }
    
    /**
     * ENHANCED: Build search query from existing base query
     * Extends existing complex query with search and sort functionality
     */
    private <T> String buildSearchQueryFromBase(SearchableEntity<T> entity, String searchText, 
                                               List<String> searchColumns, DataTableRequest request, String baseQuery) {
        
        String normalizedBaseQuery = baseQuery.trim();
        
        // Parse the base query to understand its structure
        boolean hasWhere = normalizedBaseQuery.toUpperCase().contains(" WHERE ");
        boolean hasOrderBy = normalizedBaseQuery.toUpperCase().contains(" ORDER BY ");
        
        // Remove existing ORDER BY if present (we'll add our own)
        if (hasOrderBy) {
            int orderByIndex = normalizedBaseQuery.toUpperCase().lastIndexOf(" ORDER BY ");
            normalizedBaseQuery = normalizedBaseQuery.substring(0, orderByIndex).trim();
        }
        
        StringBuilder enhancedQuery = new StringBuilder(normalizedBaseQuery);
        
        // Build additional WHERE conditions for search and filters
        List<String> additionalConditions = new ArrayList<>();
        
        // Add global search condition
        if (searchText != null && !searchText.trim().isEmpty()) {
            List<String> fieldsToSearch = determineSearchFields(entity, searchColumns);
            List<String> searchConditions = new ArrayList<>();

            for (String field : fieldsToSearch) {
				Class<?> fieldType = entity.getFieldType(field);
				String qualifiedField = getQualifiedFieldReference(field);
				if (fieldType == String.class) {
					searchConditions.add("LOWER(" + qualifiedField + ") LIKE LOWER(CONCAT('%', :searchText, '%'))");
				} else {
					searchConditions.add("CAST(" + qualifiedField + " AS string) LIKE CONCAT('%', :searchText, '%')");
				}
			}
            
            if (!searchConditions.isEmpty()) {
                additionalConditions.add("(" + String.join(" OR ", searchConditions) + ")");
            }
        }
        
        // Add column-specific filter conditions
        List<String> columnFilterConditions = buildColumnFilterConditions(entity, request);
        additionalConditions.addAll(columnFilterConditions);
        
        // Apply additional WHERE conditions
        if (!additionalConditions.isEmpty()) {
            if (hasWhere) {
                enhancedQuery.append(" AND (").append(String.join(" AND ", additionalConditions)).append(")");
            } else {
                enhancedQuery.append(" WHERE ").append(String.join(" AND ", additionalConditions));
            }
        }
        
        // Build ORDER BY clause from request columns
        List<DataTableRequest.Column> sortColumns = request.getColumns() != null ? 
            request.getColumns().parallelStream()
                .filter(col -> col.getSort() != null && !col.getSort().isBlank())
                .collect(Collectors.toList()) : new ArrayList<>();

        if (!sortColumns.isEmpty()) {
            List<String> validSortClauses = new ArrayList<>();
            
            for (DataTableRequest.Column col : sortColumns) {
                String field = col.getColumnName();
                String actualField = entity.getFieldAliases().getOrDefault(field, field);
                
                // SECURITY: Validate sort field against entity's sortable fields
                if (!entity.getSortableFields().contains(actualField)) {
                    logger.warn("Invalid sort field '{}' for entity {}. Skipping.", 
                               actualField, entity.getEntityClass().getSimpleName());
                    continue;
                }
                
                StringBuilder sortClause = new StringBuilder();
                if (actualField.contains(".")) {
                    sortClause.append(actualField);
                } else {
                    sortClause.append("e.").append(actualField);
                }
                sortClause.append(" ").append(col.getSort().equalsIgnoreCase("desc") ? "DESC" : "ASC");
                validSortClauses.add(sortClause.toString());
            }
            
            if (!validSortClauses.isEmpty()) {
                enhancedQuery.append(" ORDER BY ").append(String.join(", ", validSortClauses));
            } else {
                // Apply default sort if configured, otherwise fallback to id
                applyDefaultSort(enhancedQuery, entity);
            }
        } else {
            // Apply default sort if configured, otherwise fallback to id
            applyDefaultSort(enhancedQuery, entity);
        }

        logger.debug("Enhanced base query: Original length: {}, Enhanced length: {}", 
                    baseQuery.length(), enhancedQuery.length());
        
        return enhancedQuery.toString();
    }

    /**
     * Build column-specific filter conditions for JPQL query
     * 
     * NOTE: Field validation is now handled by DataTableRequestValidator before this method is called.
     * This ensures all fields are valid and prevents SQL injection through strict whitelisting.
     */
    private <T> List<String> buildColumnFilterConditions(SearchableEntity<T> entity, DataTableRequest request) {
        List<String> conditions = new ArrayList<>();
        
        if (request.getColumns() == null) {
            return conditions;
        }
        
        for (DataTableRequest.Column column : request.getColumns()) {
            if (column.getFilter() == null || column.getFilter().trim().isEmpty()) {
                continue;
            }
            
            String field = column.getColumnName();
            String actualField = entity.getFieldAliases().getOrDefault(field, field);
            
            // NOTE: Field validation removed - handled by DataTableRequestValidator
            // All fields are guaranteed to be valid at this point
            
            com.itt.service.specification.FilterParser.FilterCriteria criteria = 
                com.itt.service.specification.FilterParser.parseFilter(column.getFilter());
            
            // OPTIMIZATION: Check if field should use EXISTS subquery
            if (entity.useSubqueryForField(actualField)) {
                String paramName = "filter_" + sanitizeParamName(actualField);
                String subqueryCondition = entity.getSubqueryCondition(actualField, paramName);
                if (subqueryCondition != null) {
                    conditions.add(subqueryCondition);
                    logger.debug("Using subquery for filter field '{}' in entity {}", actualField, entity.getEntityClass().getSimpleName());
                    continue;
                }
            }
            
            // BUGFIX: Validate date values before building condition to prevent parameter mismatch
            if (criteria.getOperator().startsWith("d")) {
                try {
                    if ("dbetween".equals(criteria.getOperator())) {
                        String[] dateRange = criteria.getValue().split(",");
                        if (dateRange.length == 2) {
                            parseDateTime(dateRange[0].trim());
                            parseDateTime(dateRange[1].trim());
                        } else {
                            logger.warn("Invalid date range format '{}' for field '{}'. Expected format: 'start,end'. Skipping condition.", 
                                       criteria.getValue(), actualField);
                            continue;
                        }
                    } else {
                        parseDateTime(criteria.getValue());
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("Invalid date value '{}' for {} operation on field '{}'. Skipping condition: {}", 
                               criteria.getValue(), criteria.getOperator(), actualField, e.getMessage());
                    continue;
                }
            }
            
            String condition = buildFilterCondition(actualField, criteria);
            if (condition != null && !condition.trim().isEmpty()) {
                conditions.add(condition);
            }
        }
        
        return conditions;
    }
    
    /**
     * Properly qualify field reference with entity alias to avoid ambiguous references.
     * Always prefixes with 'e.' to ensure unambiguous field references in JPQL.
     * 
     * @param field The field path (e.g., "name", "assignedRole.name", "updatedByUser.email")
     * @return Qualified field reference (e.g., "e.name", "e.assignedRole.name", "e.updatedByUser.email")
     */
    private String getQualifiedFieldReference(String field) {
        // Always prefix with 'e.' to avoid ambiguous field references
        // This prevents Hibernate errors like "Ambiguous unqualified attribute reference"
        return "e." + field;
    }
    
    /**
     * Build individual filter condition based on operator.
     * MODERN JAVA OPTIMIZATION: Uses Java 17+ switch expressions for better performance and readability.
     */
    private String buildFilterCondition(String field, com.itt.service.specification.FilterParser.FilterCriteria criteria) {
        String fieldRef = getQualifiedFieldReference(field);
        String operator = criteria.getOperator();
        String paramName = sanitizeParamName(field);
        
        // JAVA 17+ SWITCH EXPRESSIONS: More efficient than if-else chains
        return switch (operator) {
            case "cnt" -> // Contains
                "LOWER(" + fieldRef + ") LIKE LOWER(CONCAT('%', :filter_" + paramName + ", '%'))";
            case "ncnt" -> // Not contains
                "LOWER(" + fieldRef + ") NOT LIKE LOWER(CONCAT('%', :filter_" + paramName + ", '%'))";
            case "sw" -> // Starts with
                "LOWER(" + fieldRef + ") LIKE LOWER(CONCAT(:filter_" + paramName + ", '%'))";
            case "ew" -> // Ends with
                "LOWER(" + fieldRef + ") LIKE LOWER(CONCAT('%', :filter_" + paramName + "))";
            case "eq" -> // Equals
                fieldRef + " = :filter_" + paramName;
            case "ne" -> // Not equals
                fieldRef + " != :filter_" + paramName;
            case "gt" -> // Greater than
                fieldRef + " > :filter_" + paramName;
            case "gte" -> // Greater than or equal
                fieldRef + " >= :filter_" + paramName;
            case "lt" -> // Less than
                fieldRef + " < :filter_" + paramName;
            case "lte" -> // Less than or equal
                fieldRef + " <= :filter_" + paramName;
            case "dgt" -> // Date greater than (date-only comparison)
                "DATE(" + fieldRef + ") > DATE(:filter_" + paramName + ")";
            case "dgte" -> // Date greater than or equal (date-only comparison)
                "DATE(" + fieldRef + ") >= DATE(:filter_" + paramName + ")";
            case "dlt" -> // Date less than (date-only comparison)
                "DATE(" + fieldRef + ") < DATE(:filter_" + paramName + ")";
            case "dlte" -> // Date less than or equal (date-only comparison)
                "DATE(" + fieldRef + ") <= DATE(:filter_" + paramName + ")";
            case "deq" -> // Date equals (date-only comparison)
                "DATE(" + fieldRef + ") = DATE(:filter_" + paramName + ")";
            case "dne" -> // Date not equals (date-only comparison)
                "DATE(" + fieldRef + ") != DATE(:filter_" + paramName + ")";
            case "dbetween" -> // Date between (date-only comparison)
                "DATE(" + fieldRef + ") BETWEEN DATE(:filter_" + paramName + "_start) AND DATE(:filter_" + paramName + "_end)";
            case "in" -> // In list
                fieldRef + " IN :filter_" + paramName + "_list";
            default -> {
                logger.warn("Unknown filter operator: {}", operator);
                yield null; // Java 17+ yield for switch expressions
            }
        };
    }
    
    /**
     * Sanitize field name for parameter naming (replace dots with underscores)
     * Enhanced to prevent parameter name collisions and SQL injection
     */
    private String sanitizeParamName(String field) {
        if (field == null || field.trim().isEmpty()) {
            return "unknown_field";
        }
        // Enhanced sanitization to prevent injection and collisions
        return field.replaceAll("[^a-zA-Z0-9_]", "_")
                   .replaceAll("_{2,}", "_")  // Replace multiple underscores with single
                   .toLowerCase();
    }
    
    /**
     * Apply default sort configuration from entity or fallback to id.
     * This provides consistent sorting behavior when no explicit sorting is specified.
     * 
     * @param jpql StringBuilder to append ORDER BY clause to
     * @param entity SearchableEntity configuration with potential default sort fields
     */
    private <T> void applyDefaultSort(StringBuilder jpql, SearchableEntity<T> entity) {
        List<String> defaultSortFields = entity.getDefaultSortFields();
        
        if (defaultSortFields != null && !defaultSortFields.isEmpty()) {
            List<String> validDefaultSortClauses = new ArrayList<>();
            
            for (String sortSpec : defaultSortFields) {
                String[] parts = sortSpec.split(":");
                if (parts.length != 2) {
                    logger.warn("Invalid default sort specification '{}' for entity {}. Expected format 'field:direction'. Skipping.", 
                               sortSpec, entity.getEntityClass().getSimpleName());
                    continue;
                }
                
                String field = parts[0].trim();
                String direction = parts[1].trim();
                
                // Use alias if present
                String actualField = entity.getFieldAliases().getOrDefault(field, field);
                
                // SECURITY: Validate sort field against entity's sortable fields
                if (!entity.getSortableFields().contains(actualField)) {
                    logger.warn("Invalid default sort field '{}' for entity {}. Field not in sortable fields. Skipping.", 
                               actualField, entity.getEntityClass().getSimpleName());
                    continue;
                }
                
                // Validate direction
                if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
                    logger.warn("Invalid sort direction '{}' for field '{}' in entity {}. Using 'asc' as default.", 
                               direction, actualField, entity.getEntityClass().getSimpleName());
                    direction = "asc";
                }
                
                StringBuilder sortClause = new StringBuilder();
                if (actualField.contains(".")) {
                    sortClause.append(actualField);
                } else {
                    sortClause.append("e.").append(actualField);
                }
                sortClause.append(" ").append(direction.equalsIgnoreCase("desc") ? "DESC" : "ASC");
                validDefaultSortClauses.add(sortClause.toString());
            }
            
            if (!validDefaultSortClauses.isEmpty()) {
                jpql.append(" ORDER BY ").append(String.join(", ", validDefaultSortClauses));
                logger.debug("Applied default sort for entity {}: {}", 
                           entity.getEntityClass().getSimpleName(), validDefaultSortClauses);
                return;
            }
        }
        
        // Fallback to id sort if no valid default sort configured
        jpql.append(" ORDER BY e.id");
        logger.debug("Applied fallback id sort for entity {}", entity.getEntityClass().getSimpleName());
    }

    /**
     * Auto-detect related entities based on field names (e.g., "config.name" -> "config").
     * MODERN JAVA OPTIMIZATION: Uses parallel stream for better performance with large field sets.
     */
    private Set<String> detectRelatedEntities(Set<String> searchableFields) {
        return searchableFields.parallelStream()
            .filter(field -> field.contains("."))
            .map(field -> field.substring(0, field.indexOf(".")))
            .collect(Collectors.toSet());
    }
    
    /**
     * Check if any field for a related entity uses subquery optimization.
     * This prevents unnecessary JOIN creation for entities that use EXISTS subqueries.
     * 
     * @param entity The searchable entity configuration
     * @param relatedEntity The related entity name (e.g., "companies", "permissions")
     * @return true if any field for this related entity uses subquery
     */
    private <T> boolean usesSubqueryForRelatedEntity(SearchableEntity<T> entity, String relatedEntity) {
        return entity.getSearchableFields().stream()
                .filter(field -> field.startsWith(relatedEntity + "."))
                .anyMatch(entity::useSubqueryForField);
    }

    /**
     * Determine which fields to search based on columns specification.
     * MODERN JAVA OPTIMIZATION: Uses parallel streams for better performance with large field sets.
     */
    private <T> List<String> determineSearchFields(SearchableEntity<T> entity, List<String> searchColumns) {
        Set<String> searchableFields = entity.getSearchableFields();
        Map<String, String> aliases = entity.getFieldAliases();
        Set<String> defaultColumns = entity.getDefaultSearchColumns();
        
        if (searchColumns == null || searchColumns.isEmpty()) {
            // MANDATORY: Default columns must be specified, otherwise no search is performed
            if (defaultColumns == null || defaultColumns.isEmpty()) {
                logger.error("Default search columns not specified for entity {}. Search will not be performed.", 
                           entity.getEntityClass().getSimpleName());
                throw new IllegalStateException(
                    "Default search columns must be specified for entity " + 
                    entity.getEntityClass().getSimpleName() + 
                    ". Please implement getDefaultSearchColumns() with at least one field."
                );
            }
            
            logger.debug("Using default search columns: {}", defaultColumns);
            // MODERN JAVA OPTIMIZATION: Parallel stream for large default column sets
            List<String> validDefaultColumns = defaultColumns.parallelStream()
                .map(col -> aliases.getOrDefault(col, col))
                .filter(searchableFields::contains)
                .collect(Collectors.toList());
                
            if (validDefaultColumns.isEmpty()) {
                logger.error("None of the default search columns are valid for entity {}. Default: {}, Valid searchable fields: {}", 
                           entity.getEntityClass().getSimpleName(), defaultColumns, searchableFields);
                throw new IllegalStateException(
                    "None of the default search columns are valid for entity " + 
                    entity.getEntityClass().getSimpleName() + 
                    ". Please check getDefaultSearchColumns() implementation."
                );
            }
            
            return validDefaultColumns;
        }
        
        // Map requested columns to actual field names
        // MODERN JAVA OPTIMIZATION: Parallel stream + method references for better performance
        logger.debug("Using requested search columns: {}", searchColumns);
        return searchColumns.parallelStream()
            .map(col -> aliases.getOrDefault(col, col))
            .filter(searchableFields::contains)
            .collect(Collectors.toList());
    }

    /**
     * Set query parameters
     * 
     * NOTE: Field validation is now handled by DataTableRequestValidator before this method is called.
     */
    private <T> void setQueryParameters(TypedQuery<T> query, SearchableEntity<T> entity, 
                                       String searchText, List<String> searchColumns, DataTableRequest request) {
        // Set global search parameter
        if (searchText != null && !searchText.trim().isEmpty()) {
            query.setParameter("searchText", searchText);
        }
        
        // Set column filter parameters
        if (request.getColumns() != null) {
            for (DataTableRequest.Column column : request.getColumns()) {
                if (column.getFilter() == null || column.getFilter().trim().isEmpty()) {
                    continue;
                }
                
                String field = column.getColumnName();
                String actualField = entity.getFieldAliases().getOrDefault(field, field);
                
                // NOTE: Field validation removed - handled by DataTableRequestValidator
                // All fields are guaranteed to be valid at this point
                
                com.itt.service.specification.FilterParser.FilterCriteria criteria = 
                    com.itt.service.specification.FilterParser.parseFilter(column.getFilter());
                
                String paramName = "filter_" + sanitizeParamName(actualField);
                String value = criteria.getValue();
                
                if ("in".equals(criteria.getOperator())) {
                    // Handle IN operator with list
                    java.util.List<String> valueList = java.util.Arrays.asList(value.split(","));
                    query.setParameter(paramName + "_list", valueList);
                } else if (criteria.getOperator().startsWith("d")) {
                    // Handle date operations (dgt, dlt, dgte, dlte, deq, dne, dbetween)
                    try {
                        if ("dbetween".equals(criteria.getOperator())) {
                            String[] dateRange = value.split(",");
                            if (dateRange.length == 2) {
                                LocalDateTime startDate = parseDateTime(dateRange[0].trim());
                                LocalDateTime endDate = parseDateTime(dateRange[1].trim());
                                query.setParameter(paramName + "_start", startDate);
                                query.setParameter(paramName + "_end", endDate);
                            } else {
                                logger.warn("Invalid date range format '{}' for field '{}'. Expected format: 'start,end'. Skipping parameter.", 
                                           value, actualField);
                                // Skip this parameter - condition was already filtered out in buildColumnFilterConditions
                            }
                        } else {
                            LocalDateTime dateTime = parseDateTime(value);
                            query.setParameter(paramName, dateTime);
                        }
                    } catch (DateTimeParseException e) {
                        logger.warn("Invalid date value '{}' for {} operation on field '{}'. Skipping parameter: {}", 
                                   value, criteria.getOperator(), actualField, e.getMessage());
                        // Skip this parameter - condition was already filtered out in buildColumnFilterConditions
                    }
                } else if ("gt".equals(criteria.getOperator()) || "lt".equals(criteria.getOperator()) || 
                          "gte".equals(criteria.getOperator()) || "lte".equals(criteria.getOperator()) ||
                          "eq".equals(criteria.getOperator()) || "ne".equals(criteria.getOperator())) {
                    // Handle numeric operations (try numeric first, fallback to string for eq/ne)
                    try {
                        Double numericValue = Double.parseDouble(value);
                        query.setParameter(paramName, numericValue);
                    } catch (NumberFormatException e) {
                        // For eq/ne, fallback to string comparison if not numeric
                        if ("eq".equals(criteria.getOperator()) || "ne".equals(criteria.getOperator())) {
                            query.setParameter(paramName, value);
                        } else {
                            logger.warn("Invalid numeric value '{}' for {} operation on field '{}'", 
                                       value, criteria.getOperator(), actualField);
                        }
                    }
                } else {
                    // Handle string operations
                    query.setParameter(paramName, value);
                }
            }
        }
    }

    /**
     * Get total count for pagination
     */
    private <T> long getTotalCount(SearchableEntity<T> entity, String searchText, List<String> searchColumns, DataTableRequest request) {
        // Build count query (simplified version without fetch joins)
        StringBuilder countJpql = new StringBuilder();
        
        // Use DISTINCT based on entity configuration
        if (entity.shouldUseDistinct()) {
            countJpql.append("SELECT COUNT(DISTINCT e) FROM ").append(entity.getEntityName()).append(" e ");
        } else {
            countJpql.append("SELECT COUNT(e) FROM ").append(entity.getEntityName()).append(" e ");
        }
        
        // Build WHERE clause
        List<String> whereConditions = new ArrayList<>();
        
        // Add global search condition
        if (searchText != null && !searchText.trim().isEmpty()) {
            List<String> fieldsToSearch = determineSearchFields(entity, searchColumns);
            List<String> searchConditions = new ArrayList<>();
            Set<String> joinsAdded = new HashSet<>();

            for (String field : fieldsToSearch) {
                // OPTIMIZATION: Check if field should use EXISTS subquery
                if (entity.useSubqueryForField(field)) {
                    String subqueryCondition = entity.getSubqueryCondition(field, "searchText");
                    if (subqueryCondition != null) {
                        searchConditions.add(subqueryCondition);
                        continue;
                    }
                }
                
                // Use normal JOIN condition for other fields
                if (field.contains(".")) {
                    // Related entity field - need join
                    String relatedEntity = field.substring(0, field.indexOf("."));
                    if (!joinsAdded.contains(relatedEntity)) {
                        countJpql.append("LEFT JOIN e.").append(relatedEntity).append(" ");
                        joinsAdded.add(relatedEntity);
                    }
                }
                String qualifiedField = getQualifiedFieldReference(field);
                Class<?> fieldType = entity.getFieldType(field);
                if (fieldType != null && fieldType == String.class) {
                    searchConditions.add("LOWER(" + qualifiedField + ") LIKE LOWER(CONCAT('%', :searchText, '%'))");
                } else {
                    // Handle null fieldType or non-String types with safe conversion
                    // Use CONCAT for safer database compatibility instead of CAST AS string
                    searchConditions.add("CONCAT('', " + qualifiedField + ") LIKE CONCAT('%', :searchText, '%')");
                }
            }
            
            if (!searchConditions.isEmpty()) {
                whereConditions.add("(" + String.join(" OR ", searchConditions) + ")");
            }
        }
        
        // Add column-specific filter conditions
        List<String> columnFilterConditions = buildColumnFilterConditions(entity, request);
        whereConditions.addAll(columnFilterConditions);
        
        // Apply WHERE clause if any conditions exist
        if (!whereConditions.isEmpty()) {
            countJpql.append("WHERE ").append(String.join(" AND ", whereConditions));
        }
        
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        
        // Set parameters for count query
        if (searchText != null && !searchText.trim().isEmpty()) {
            countQuery.setParameter("searchText", searchText);
        }
        
        // Set column filter parameters for count query
        setColumnFilterParameters(countQuery, entity, request);
        
        return countQuery.getSingleResult();
    }
    
    /**
     * ENHANCED: Get total count for pagination from base query
     */
    private <T> long getTotalCountFromBase(SearchableEntity<T> entity, String searchText, 
                                          List<String> searchColumns, DataTableRequest request, String baseQuery) {
        
        String normalizedBaseQuery = baseQuery.trim().toUpperCase();
        
        // Convert base query to count query
        StringBuilder countJpql = new StringBuilder();
        
        // Find the FROM clause in the base query
        String originalQuery = baseQuery.trim();
        int fromIndex = normalizedBaseQuery.indexOf(" FROM ");
        if (fromIndex == -1) {
            throw new IllegalArgumentException("Invalid base query: FROM clause not found");
        }
        
        // Extract FROM clause and everything after it (excluding ORDER BY)
        String fromClause = originalQuery.substring(fromIndex);
        
        // Remove ORDER BY if present
        int orderByIndex = fromClause.toUpperCase().lastIndexOf(" ORDER BY ");
        if (orderByIndex != -1) {
            fromClause = fromClause.substring(0, orderByIndex);
        }
        
        // Build count query with same joins and conditions as base query
        if (entity.shouldUseDistinct() || normalizedBaseQuery.contains("DISTINCT")) {
            countJpql.append("SELECT COUNT(DISTINCT e)").append(fromClause);
        } else {
            countJpql.append("SELECT COUNT(e)").append(fromClause);
        }
        
        // Check if base query already has WHERE clause
        boolean hasWhere = fromClause.toUpperCase().contains(" WHERE ");
        
        // Build additional WHERE conditions for search and filters
        List<String> additionalConditions = new ArrayList<>();
        
        // Add global search condition
        if (searchText != null && !searchText.trim().isEmpty()) {
            List<String> fieldsToSearch = determineSearchFields(entity, searchColumns);
            List<String> searchConditions = new ArrayList<>();

            for (String field : fieldsToSearch) {
				Class<?> fieldType = entity.getFieldType(field);
				String qualifiedField = getQualifiedFieldReference(field);
				if (fieldType == String.class) {
					searchConditions.add("LOWER(" + qualifiedField + ") LIKE LOWER(CONCAT('%', :searchText, '%'))");
				} else {
					searchConditions.add("CAST(" + qualifiedField + " AS string) LIKE CONCAT('%', :searchText, '%')");
				}
			}
            
            if (!searchConditions.isEmpty()) {
                additionalConditions.add("(" + String.join(" OR ", searchConditions) + ")");
            }
        }
        
        // Add column-specific filter conditions
        List<String> columnFilterConditions = buildColumnFilterConditions(entity, request);
        additionalConditions.addAll(columnFilterConditions);
        
        // Apply additional WHERE conditions
        if (!additionalConditions.isEmpty()) {
            if (hasWhere) {
                countJpql.append(" AND (").append(String.join(" AND ", additionalConditions)).append(")");
            } else {
                countJpql.append(" WHERE ").append(String.join(" AND ", additionalConditions));
            }
        }
        
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        
        // Set parameters for count query
        if (searchText != null && !searchText.trim().isEmpty()) {
            countQuery.setParameter("searchText", searchText);
        }
        
        // Set column filter parameters for count query
        setColumnFilterParameters(countQuery, entity, request);
        
        logger.debug("Enhanced count query from base: {}", countJpql.toString());
        
        return countQuery.getSingleResult();
    }
    
    /**
     * Set column filter parameters for count query
     * 
     * NOTE: Field validation is now handled by DataTableRequestValidator before this method is called.
     */
    private <T> void setColumnFilterParameters(TypedQuery<Long> query, SearchableEntity<T> entity, DataTableRequest request) {
        if (request.getColumns() != null) {
            for (DataTableRequest.Column column : request.getColumns()) {
                if (column.getFilter() == null || column.getFilter().trim().isEmpty()) {
                    continue;
                }
                
                String field = column.getColumnName();
                String actualField = entity.getFieldAliases().getOrDefault(field, field);
                
                // NOTE: Field validation removed - handled by DataTableRequestValidator
                // All fields are guaranteed to be valid at this point
                
                com.itt.service.specification.FilterParser.FilterCriteria criteria = 
                    com.itt.service.specification.FilterParser.parseFilter(column.getFilter());
                
                String paramName = "filter_" + sanitizeParamName(actualField);
                String value = criteria.getValue();
                
                if ("in".equals(criteria.getOperator())) {
                    // Handle IN operator with list
                    java.util.List<String> valueList = java.util.Arrays.asList(value.split(","));
                    query.setParameter(paramName + "_list", valueList);
                } else if (criteria.getOperator().startsWith("d")) {
                    // Handle date operations (dgt, dlt, dgte, dlte, deq, dne, dbetween)
                    try {
                        if ("dbetween".equals(criteria.getOperator())) {
                            String[] dateRange = value.split(",");
                            if (dateRange.length == 2) {
                                LocalDateTime startDate = parseDateTime(dateRange[0].trim());
                                LocalDateTime endDate = parseDateTime(dateRange[1].trim());
                                query.setParameter(paramName + "_start", startDate);
                                query.setParameter(paramName + "_end", endDate);
                            } else {
                                logger.warn("Invalid date range format '{}' for field '{}'. Expected format: 'start,end'. Skipping parameter.", 
                                           value, actualField);
                                // Skip this parameter - condition was already filtered out in buildColumnFilterConditions
                            }
                        } else {
                            LocalDateTime dateTime = parseDateTime(value);
                            query.setParameter(paramName, dateTime);
                        }
                    } catch (DateTimeParseException e) {
                        logger.warn("Invalid date value '{}' for {} operation on field '{}'. Skipping parameter: {}", 
                                   value, criteria.getOperator(), actualField, e.getMessage());
                        // Skip this parameter - condition was already filtered out in buildColumnFilterConditions
                    }
                } else if ("gt".equals(criteria.getOperator()) || "lt".equals(criteria.getOperator()) || 
                          "gte".equals(criteria.getOperator()) || "lte".equals(criteria.getOperator()) ||
                          "eq".equals(criteria.getOperator()) || "ne".equals(criteria.getOperator())) {
                    // Handle numeric operations (try numeric first, fallback to string for eq/ne)
                    try {
                        Double numericValue = Double.parseDouble(value);
                        query.setParameter(paramName, numericValue);
                    } catch (NumberFormatException e) {
                        // For eq/ne, fallback to string comparison if not numeric
                        if ("eq".equals(criteria.getOperator()) || "ne".equals(criteria.getOperator())) {
                            query.setParameter(paramName, value);
                        } else {
                            logger.warn("Invalid numeric value '{}' for {} operation on field '{}'", 
                                       value, criteria.getOperator(), actualField);
                        }
                    }
                } else {
                    // Handle string operations
                    query.setParameter(paramName, value);
                }
            }
        }
    }

    private String extractSearchText(DataTableRequest request) {
        return request.getSearchFilter() != null ? request.getSearchFilter().getSearchText() : null;
    }

    private List<String> extractSearchColumns(DataTableRequest request) {
        return request.getSearchFilter() != null ? request.getSearchFilter().getColumns() : List.of();
    }
    
    /**
     * MODERN JAVA OPTIMIZATION: High-performance date parsing with caching and pattern matching.
     * 
     * PERFORMANCE IMPROVEMENTS:
     * 1. O(1) cache lookup for previously parsed dates
     * 2. Pre-compiled static DateTimeFormatter array
     * 3. Fast-path detection for common patterns
     * 4. Early return optimization
     * 5. Set-based pattern matching instead of string operations
     * 
     * TIME COMPLEXITY: O(1) for cached dates, O(k) for new dates where k = number of formatters
     * SPACE COMPLEXITY: O(n) where n = number of unique date strings (bounded by cache size)
     */
    private static LocalDateTime parseDateTime(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new DateTimeParseException("Date string is null or empty", dateStr, 0);
        }
        
        dateStr = dateStr.trim();
        
        // ==========================================
        // MODERN JAVA OPTIMIZATION: O(1) Cache Lookup
        // ==========================================
        LocalDateTime cached = DATE_CACHE.get(dateStr);
        if (cached != null) {
            return cached; // O(1) return for previously parsed dates
        }
        
        // ==========================================
        // MODERN JAVA OPTIMIZATION: Fast-path Pattern Detection
        // DATE-ONLY COMPARISON: All dates normalized to start of day (00:00:00)
        // ==========================================
        LocalDateTime result = null;
        
        // PERFORMANCE: Quick pattern detection for most common formats
        if (dateStr.length() == 10 && dateStr.charAt(4) == '-' && dateStr.charAt(7) == '-') {
            // Fast path for yyyy-MM-dd format (most common)
            try {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                result = date.atStartOfDay(); // Always normalize to 00:00:00
            } catch (DateTimeParseException e) {
                // Continue to general parsing
            }
        } else if (dateStr.length() == 10 && dateStr.charAt(2) == '/' && dateStr.charAt(5) == '/') {
            // Fast path for dd/MM/yyyy format
            try {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                result = date.atStartOfDay(); // Always normalize to 00:00:00
            } catch (DateTimeParseException e) {
                // Continue to general parsing
            }
        }
        
        // ==========================================
        // FALLBACK: General Pattern Matching with Pre-compiled Formatters
        // DATE-ONLY COMPARISON: All dates normalized to start of day (00:00:00)
        // ==========================================
        if (result == null) {
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    String pattern = formatter.toString();
                    
                    // JAVA 17+ OPTIMIZATION: Pattern matching with Set lookup O(1) instead of contains() O(n)
                    if (DATE_ONLY_PATTERNS.contains(pattern.replaceAll("'T'", "T"))) {
                        if ((pattern.contains("yyyy-MM-dd") && !dateStr.contains("T") && !dateStr.contains(" ")) ||
                            (pattern.contains("dd/MM/yyyy") && !dateStr.contains(" "))) {
                            LocalDate date = LocalDate.parse(dateStr, formatter);
                            result = date.atStartOfDay(); // Always normalize to 00:00:00
                        }
                    } else {
                        // Parse datetime but normalize to date-only (ignore time component)
                        LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
                        result = dateTime.toLocalDate().atStartOfDay(); // Normalize to 00:00:00
                    }
                    
                    if (result != null) {
                        break; // Success - exit loop early
                    }
                } catch (DateTimeParseException e) {
                    // Continue to next formatter
                }
            }
            
            if (result == null) {
                throw new DateTimeParseException("Unable to parse date: " + dateStr + 
                    ". Supported formats: yyyy-MM-dd [HH:mm[:ss]], dd/MM/yyyy [HH:mm[:ss]], ISO format", 
                    dateStr, 0);
            }
        }
        
        // ==========================================
        // MODERN JAVA OPTIMIZATION: Cache Result for Future O(1) Lookups
        // Prevent memory leaks with size limit
        // ==========================================
        if (DATE_CACHE.size() < 1000) { // Prevent unbounded growth
            DATE_CACHE.putIfAbsent(dateStr, result); // Thread-safe caching
        } else if (DATE_CACHE.size() >= 1000) {
            // Clear old entries when cache gets too large
            DATE_CACHE.clear();
            DATE_CACHE.put(dateStr, result);
        }
        return result;
    }
}
