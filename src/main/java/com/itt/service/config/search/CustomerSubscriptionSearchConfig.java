package com.itt.service.config.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.itt.service.entity.MasterCompany;
import com.itt.service.fw.search.SearchableEntity;

/**
 * Search configuration for Customer Subscription (MasterCompany) entity using
 * Universal Search Framework.
 * Supports both flat DTO fields and nested object fields for enhanced
 * responses.
 * 
 * @author Bineetbhusan Das
 * @since 2025-08-25
 */
@Component
public class CustomerSubscriptionSearchConfig implements SearchableEntity<MasterCompany> {

	@Override
	public Set<String> getSearchableFields() {
		return Set.of(
				"id", "companyName", "subscriptionTypeConfig.name", "onboardedBySource.name",
				"createdOn", "updatedOn", "updatedBy.email", "subscriptionTypeConfigId");
	}

	@Override
	public Set<String> getSortableFields() {
		return Set.of(
				"id", "companyName", "subscriptionTypeConfig.name", "onboardedBySource.name",
				"createdOn", "updatedOn", "updatedBy.email", "subscriptionTypeConfigId");
	}

	@Override
	public Set<String> getDefaultSearchColumns() {
		return Set.of("id", "companyName");
	}

	@Override
	public List<String> getDefaultSortFields() {
		return List.of("companyName:asc");
	}

	@Override
	public Class<MasterCompany> getEntityClass() {
		return MasterCompany.class;
	}

	@Override
	public Set<String> getFetchJoins() {
		return Set.of(
				"subscriptionTypeConfig", "onboardedBySource", "updatedBy");
	}

	@Override
	public boolean shouldUseDistinct() {
		return true;
	}

	@Override
	public Map<String, String> getFieldAliases() {
		return Map.of(
				// DTO field mappings for the 7 allowed fields
				"customerCode", "id",
				"customerName", "companyName",
				"subscriptionTypeName", "subscriptionTypeConfig.name",
				"onboardedSourceName", "onboardedBySource.name",
				"onboardedOn", "createdOn",
				"updatedBy", "updatedBy.email",
				"subscriptionTypeConfigId", "subscriptionTypeConfigId"
				);
	}
}