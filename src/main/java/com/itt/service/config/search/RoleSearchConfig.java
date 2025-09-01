package com.itt.service.config.search;

import com.itt.service.entity.Role;
import com.itt.service.fw.search.SearchableEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Search configuration for Role entity using Universal Search Framework.
 * Supports both flat DTO fields and nested object fields for enhanced
 * responses.
 * 
 * @author Bineetbhusan Dasd
 * @since 2025-08-25
 */
@Component
public class RoleSearchConfig implements SearchableEntity<Role> {

	@Override
	public Set<String> getSearchableFields() {
		return Set.of(
				"isActive", "description", "landingPageConfig", "landingPageConfig.name",
				"name", "skinName", "skin.name", "featureName", "createdByUser.email", "createdOn",
				"updatedByUser.email", "updatedOn");
	}

	@Override
	public Set<String> getSortableFields() {
		// NOTE: skinName is searchable but NOT sortable (subquery complexity)
		return Set.of(
				"isActive", "description", "landingPageConfig", "landingPageConfig.name",
				"name", "createdByUser.email", "createdOn",
				"updatedByUser.email", "updatedOn");
	}

	@Override
	public Map<String, String> getFieldAliases() {
		return Map.of(
				// DTO → Entity mappings for allowed fields
				"roleName", "name",
				"roleDescription", "description",
				"createdBy", "createdByUser.email",
				"updatedBy", "updatedByUser.email",
				"landingPage.name", "landingPageConfig.name",
				"landingPage", "landingPageConfig"
				// NOTE: skinName and skin.name handling is done through subquery optimization
				// See useSubqueryForField() and getSubqueryCondition() methods
				// Both "skinName" and "skin.name" are supported for flexibility
		);
	}

	@Override
	public Set<String> getDefaultSearchColumns() {
		return Set.of("name", "description");
	}

	@Override
	public List<String> getDefaultSortFields() {
		return List.of("name:asc");
	}

	@Override
	public Class<Role> getEntityClass() {
		return Role.class;
	}

	@Override
	public Set<String> getFetchJoins() {
		// NOTE: Deliberately NOT including "skins" to avoid loading 7-10 skins per role
		// The framework will use LEFT JOIN for search/sort but won't fetch the
		// collection
		return Set.of(
				"landingPageConfig", "createdByUser", "updatedByUser");
	}

	@Override
	public boolean shouldUseDistinct() {
		return true;
	}

	// ✅ SKIN SEARCH OPTIMIZATION: Use subquery for efficient skin search
	@Override
	public boolean useSubqueryForField(String field) {
		// Use EXISTS subquery for feature and skin-related fields
    return "skinName".equals(field) || "skin.name".equals(field) || "featureName".equals(field);
	}

	@Override
	public String getSubqueryCondition(String field, String paramName) {
		if ("skinName".equals(field) || "skin.name".equals(field)) {
			// Generate EXISTS subquery for skin name search
			return "EXISTS (" +
					"SELECT 1 FROM MapRoleCategoryFeaturePrivilegeSkin mrcs " +
					"JOIN mrcs.skinConfig sc " +
					"WHERE mrcs.role = e " +
					"AND LOWER(sc.name) LIKE LOWER(CONCAT('%', :" + paramName + ", '%'))" +
					")";
		}

		 if ("featureName".equals(field)) {
        // ✅ Add subquery for feature name search via mapCategoryFeaturePrivilege
        return "EXISTS (" +
                "SELECT 1 FROM MapRoleCategoryFeaturePrivilegeSkin mrcs " +
                "JOIN mrcs.mapCategoryFeaturePrivilege mcfp " +
                "JOIN mcfp.feature f " +
                "WHERE mrcs.role = e " +
                "AND LOWER(f.name) LIKE LOWER(CONCAT('%', :" + paramName + ", '%'))" +
                ")";
    }
		return null; // Use default JOIN approach for other fields
	}

	/**
	 * Define field types for proper parameter handling.
	 * Critical for boolean (Integer 0/1) and nullable fields.
	 */
	@Override
	public Class<?> getFieldType(String fieldName) {
		// Map field aliases to actual field names first
		String actualField = getFieldAliases().getOrDefault(fieldName, fieldName);
		
		return switch (actualField) {
			case "isActive" -> Integer.class; // Boolean stored as Integer (0/1)
			case "landingPageConfig" -> com.itt.service.entity.MasterConfig.class;
			case "landingPageConfig.name", "landingPageConfig.keyCode" -> String.class;
			case "landingPageConfig.id" -> Long.class;
			case "name", "description" -> String.class;
			case "createdByUser.email", "updatedByUser.email" -> String.class;
			case "createdOn", "updatedOn" -> java.time.LocalDateTime.class;
			default -> String.class; // Default to String for unknown fields
		};
	}
}