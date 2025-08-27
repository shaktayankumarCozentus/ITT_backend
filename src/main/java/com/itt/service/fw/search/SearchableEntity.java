package com.itt.service.fw.search;

import java.util.Set;

/**
 * Framework interface for defining searchable fields for any entity.
 * Implement this interface for each entity to enable automatic search functionality.
 * 
 * @param <T> Entity type
 */
public interface SearchableEntity<T> {
    
    /**
     * Define which fields are searchable for this entity
     * @return Set of searchable field names
     */
    Set<String> getSearchableFields();
    
    /**
     * Define which fields are sortable for this entity
     * @return Set of sortable field names
     */
    Set<String> getSortableFields();
    
    /**
     * Define field aliases for easier client usage
     * @return Map of alias -> actual field name
     */
    default java.util.Map<String, String> getFieldAliases() {
        return java.util.Map.of();
    }
    
    /**
     * Define default search columns when no specific columns are provided.
     * This is MANDATORY - if empty, search will not be performed.
     * Forces developers to explicitly define which fields should be searched by default.
     * @return Set of default field names to search when no columns specified (cannot be empty)
     */
    Set<String> getDefaultSearchColumns();
    
    /**
     * Define default sort fields and directions when no sorting is provided in the request.
     * This provides consistent, predictable sorting behavior for each entity.
     * If empty, framework will fall back to "ORDER BY e.id ASC".
     * 
     * Format: "fieldName:direction" where direction is "asc" or "desc"
     * Examples: ["name:asc", "createdOn:desc"], ["fullName:asc"], ["priority:desc", "name:asc"]
     * 
     * @return List of default sort specifications in order of priority (empty = use id fallback)
     */
    default java.util.List<String> getDefaultSortFields() {
        return java.util.List.of();
    }
    
    /**
     * Determines if a field should use EXISTS subquery instead of JOIN for search operations.
     * This is critical for performance optimization with large collections (1000+ related entities).
     * 
     * Use Cases:
     * - User -> Companies relationship (user can have 10,000+ companies)
     * - Role -> Permissions relationship (role can have 100+ permissions)
     * - Order -> LineItems relationship (order can have 1000+ line items)
     * 
     * Performance Impact:
     * - JOIN FETCH: Loads all related entities into memory (expensive)
     * - EXISTS subquery: Only checks existence (very fast)
     * 
     * @param field The field name to check (supports dot notation like "companies.companyName")
     * @return true if field should use EXISTS subquery, false for normal JOIN
     */
    default boolean useSubqueryForField(String field) {
        return false;
    }
    
    /**
     * Generate custom EXISTS subquery condition for specific fields.
     * Used when useSubqueryForField() returns true for performance optimization.
     * 
     * The subquery should:
     * 1. Use EXISTS for optimal performance
     * 2. Reference the main entity as 'e' 
     * 3. Use the provided paramName for parameter binding
     * 4. Follow security best practices (parameterized queries)
     * 
     * Example Implementation:
     * <pre>
     * &#64;Override
     * public String getSubqueryCondition(String field, String paramName) {
     *     if (field.equals("companies.companyName")) {
     *         return String.format("""
     *             EXISTS (
     *                 SELECT 1 FROM MapUserCompany muc 
     *                 JOIN MasterCompany mc ON muc.companyId = mc.id 
     *                 WHERE muc.userId = e.id 
     *                 AND LOWER(mc.companyName) LIKE LOWER(CONCAT('%%', :%s, '%%'))
     *             )
     *             """, paramName);
     *     }
     *     return null;
     * }
     * </pre>
     * 
     * @param field The field name that requires subquery
     * @param paramName The parameter name to use in the subquery (already sanitized)
     * @return Custom EXISTS subquery condition or null to use default behavior
     */
    default String getSubqueryCondition(String field, String paramName) {
        return null;
    }
    
    /**
     * Get the entity class for reflection and query building
     * @return Entity class
     */
    Class<T> getEntityClass();
    
    /**
     * Get the entity name for JPQL queries
     * @return Entity name used in JPQL
     */
    default String getEntityName() {
        return getEntityClass().getSimpleName();
    }
    
    /**
     * Define which associations to fetch join to avoid N+1 queries.
     * These will be automatically added to the query as LEFT JOIN FETCH.
     * @return Set of association field names to fetch join
     */
    default Set<String> getFetchJoins() {
        return Set.of();
    }
    
    /**
     * Whether to use DISTINCT in queries with fetch joins.
     * Should return true when fetch joining to avoid duplicate rows.
     * @return true if DISTINCT should be used, false otherwise
     */
    default boolean shouldUseDistinct() {
        return false;
    }
    
    /**
	 * Get the type of a field by its name, supporting nested fields using dot notation.
	 * Implements enhanced security checks to prevent unauthorized field access.
	 * 
	 * @param fieldName The name of the field, can be nested (e.g., "company.companyName")
	 * @return The Class type of the field, or null if not found or access denied
	 */
    default Class<?> getFieldType(String fieldName) {
		try {
			Class<?> currentClass = getEntityClass();
			String[] parts = fieldName.split("\\.");
			
			for (String part : parts) {
				java.lang.reflect.Field field = currentClass.getDeclaredField(part);
				
				// SECURITY: Enhanced field access validation
				if (!isFieldAccessAllowed(field)) {
					return null; // Block access to restricted fields
				}
				
				currentClass = field.getType();
			}
			return currentClass;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException e) {
			// Silently handle reflection errors for security
			return null;
		}
	}

	/**
	 * Determines if field access is allowed based on security policies.
	 * Replaces the problematic field.canAccess(null) check with safer validation.
	 * 
	 * @param field The field to validate
	 * @return true if field access is permitted, false otherwise
	 */
	private boolean isFieldAccessAllowed(java.lang.reflect.Field field) {
		Class<?> fieldType = field.getType();
		String fieldTypeName = fieldType.getName();
		String packageName = fieldType.getPackageName();
		
		// Allow primitive types (int, long, boolean, etc.)
		if (fieldType.isPrimitive()) {
			return true;
		}
		
		// Allow standard Java types
		if (fieldTypeName.startsWith("java.") || fieldTypeName.startsWith("javax.")) {
			return true;
		}
		
		// Allow entity package types - our domain entities
		if (packageName.startsWith("com.itt.service.entity")) {
			return true;
		}
		
		// Allow common data types used in entities
		if (isCommonDataType(fieldTypeName)) {
			return true;
		}
		
		// Block access to potentially sensitive fields from other packages
		return false;
	}

	/**
	 * Checks if the field type is a commonly used safe data type.
	 * 
	 * @param fieldTypeName The fully qualified name of the field type
	 * @return true if it's a safe common data type
	 */
	private boolean isCommonDataType(String fieldTypeName) {
		return fieldTypeName.equals("java.time.LocalDateTime") || 
			   fieldTypeName.equals("java.time.LocalDate") ||
			   fieldTypeName.equals("java.time.LocalTime") ||
			   fieldTypeName.equals("java.time.ZonedDateTime") ||
			   fieldTypeName.equals("java.math.BigDecimal") ||
			   fieldTypeName.equals("java.math.BigInteger") ||
			   fieldTypeName.equals("java.util.List") ||
			   fieldTypeName.equals("java.util.Set") ||
			   fieldTypeName.equals("java.util.Map") ||
			   fieldTypeName.equals("java.util.UUID");
	}
}
