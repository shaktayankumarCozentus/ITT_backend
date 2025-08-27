package com.itt.service.config.search;

import com.itt.service.entity.MasterUser;
import com.itt.service.fw.search.SearchableEntity;
import com.itt.service.repository.MasterUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Search configuration for MasterUser entity using Universal Search Framework.
 * Supports both flat DTO fields and nested assignedRole object fields.
 * 
 * @author System Generated
 * @since 2025-08-25
 */
@Component
public class UserSearchConfig implements SearchableEntity<MasterUser> {

	private final MasterUserRepository userRepository;

	public UserSearchConfig(MasterUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public Set<String> getSearchableFields() {
		return Set.of(
				// Entity primary fields
				"id", "fullName", "email", "userType", "isBdpEmployee", "createdOn", "updatedOn",

				// Entity relationship fields
				"assignedRole.name", "assignedRole.isActive", "companies.companyName", "updatedByUser.email");
	}

	@Override
	public Set<String> getSortableFields() {
		return Set.of(
				// Entity primary fields
				"id", "fullName", "email", "userType", "isBdpEmployee", "createdOn", "updatedOn",

				// Entity relationship fields
				"assignedRole.name", "assignedRole.isActive", "updatedByUser.email"
		// Note: Company name sorting not supported with EXISTS subquery optimization
		);
	}

	@Override
	public Set<String> getDefaultSearchColumns() {
		return Set.of("fullName", "email", "assignedRole.name", "companies.companyName");
	}

	@Override
	public List<String> getDefaultSortFields() {
		return List.of("fullName:asc");
	}

	@Override
	public Class<MasterUser> getEntityClass() {
		return MasterUser.class;
	}

	@Override
	public Map<String, String> getFieldAliases() {
		Map<String, String> aliases = new java.util.HashMap<>();

		// DTO → Entity mappings
		aliases.put("userId", "id");
		aliases.put("name", "fullName");
		aliases.put("updatedBy", "updatedByUser.email");
		aliases.put("company", "companies.companyName");
		aliases.put("assignedRoleName", "assignedRole.name");
		aliases.put("roleName", "assignedRole.name");
		aliases.put("isActiveRole", "assignedRole.isActive");
		aliases.put("companyName", "companies.companyName");
		aliases.put("companies", "companies.companyName");

		return aliases;
	}

	@Override
	public Set<String> getFetchJoins() {
		// Performance optimization: Remove companies fetch join to avoid loading 10K+
		// companies per user
		return Set.of("assignedRole", // Fetch assigned role (small data)
				"updatedByUser" // Fetch last modifier user details
		// "companies" - REMOVED: causes performance issues with 10K+ companies per user
		);
	}

	@Override
	public boolean shouldUseDistinct() {
		return true;
	}

	@Override
	public boolean useSubqueryForField(String field) {
		// Use EXISTS subquery for company-related fields to prevent loading 10K+
		// companies
		String actualField = getFieldAliases().getOrDefault(field, field);
		return "companies.companyName".equals(actualField) || field.startsWith("companies.")
				|| Set.of("company", "companyName", "companies").contains(field);
	}

	@Override
	public String getSubqueryCondition(String field, String paramName) {
		if (useSubqueryForField(field)) {
			return String.format("""
					EXISTS (
					    SELECT 1 FROM MapUserCompany muc
					    JOIN MasterCompany mc ON muc.companyId = mc.id
					    WHERE muc.userId = e.id
					    AND LOWER(mc.companyName) LIKE LOWER(CONCAT('%%', :%s, '%%'))
					)
					""", paramName);
		}
		return null;
	}

	/**
	 * Get the repository for additional operations like batch company count
	 * fetching. This is used for performance-optimized company count calculation.
	 */
	public MasterUserRepository getUserRepository() {
		return userRepository;
	}

}