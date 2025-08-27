package com.itt.service.config.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.itt.service.entity.MapCompanyPetaPetd;
import com.itt.service.fw.search.SearchableEntity;

/**
 * Search configuration for PetaPetd entity using Universal Search Framework.
 * Supports both flat DTO fields and nested object fields for enhanced
 * responses.
 * 
 * @author Bineetbhusan Das
 * @since 2025-08-25
 */
@Component
public class PetaPetdSearchConfig implements SearchableEntity<MapCompanyPetaPetd> {

	@Override
	public Set<String> getSearchableFields() {
		return Set.of(
				"company.id", "company.companyName", "updatedBy.email", "updatedOn");
	}

	@Override
	public Set<String> getSortableFields() {
		return Set.of(
				"company.id", "company.companyName", "updatedBy.email", "updatedOn");
	}

	@Override
	public Set<String> getDefaultSearchColumns() {
		return Set.of("company.id", "company.companyName");
	}

	@Override
	public List<String> getDefaultSortFields() {
		return List.of("company.companyName:asc");
	}

	@Override
	public Map<String, String> getFieldAliases() {
		return Map.of(
				// DTO field mappings for the 4 allowed fields
				"companyName", "company.companyName",
				"companyCode", "company.id",
				"updatedBy", "updatedBy.email");
	}

	@Override
	public Class<MapCompanyPetaPetd> getEntityClass() {
		return MapCompanyPetaPetd.class;
	}

	@Override
	public Set<String> getFetchJoins() {
		return Set.of("company", "updatedBy");
	}

	@Override
	public boolean shouldUseDistinct() {
		return true;
	}
}