package com.itt.service.config.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.itt.service.entity.MapUserCompany;
import com.itt.service.fw.search.SearchableEntity;

/**
 * Search configuration for MapUserCompany entity using Universal Search
 * Framework.
 * 
 * This configuration enables sophisticated searching and filtering of companies
 * assigned to users.
 * Supports nested field searches through company relationships with optimized
 * fetch joins.
 * 
 * Key Features:
 * - Company ID and name search capabilities
 * - User-specific filtering with authorization
 * - Optimized database queries with fetch joins
 * - Field alias mapping for DTO compatibility
 * - Distinct results to handle join duplicates
 * 
 * @author Bineetbhusan Das
 * @since 2025-08-26
 * @version 1.0
 */
@Component
public class UserCompanySearchConfig implements SearchableEntity<MapUserCompany> {

    /**
     * Defines fields that can be searched using the Universal Search Framework.
     * Limited to company name and company ID only as per requirements.
     */
    @Override
    public Set<String> getSearchableFields() {
        return Set.of(
                "company.id", // Company ID for exact matching
                "company.companyName", // Company name for text search
                "userId" // User ID for relationship filtering (internal use)
        );
    }

    /**
     * Defines fields that can be used for sorting results.
     * Only default company name sorting allowed as per requirements.
     */
    @Override
    public Set<String> getSortableFields() {
        return Set.of(
                "company.companyName" // Only company name sorting allowed
        );
    }

    /**
     * Default columns to search when performing global text search.
     * Only company name search as per requirements.
     */
    @Override
    public Set<String> getDefaultSearchColumns() {
        return Set.of("company.companyName");
    }

    /**
     * Default sorting order for results.
     * Orders by company name alphabetically for user-friendly display.
     */
    @Override
    public List<String> getDefaultSortFields() {
        return List.of("company.companyName:asc");
    }

    /**
     * Field aliases to map DTO fields to entity fields.
     * Verified mapping: UserCompanyDto fields -> MapUserCompany entity paths.
     */
    @Override
    public Map<String, String> getFieldAliases() {
        return Map.of(
                // UserCompanyDto.companyId -> MapUserCompany.company.id
                "companyId", "company.id",
                // UserCompanyDto.companyName -> MapUserCompany.company.companyName
                "companyName", "company.companyName");
    }

    /**
     * Returns the entity class for type safety and reflection.
     */
    @Override
    public Class<MapUserCompany> getEntityClass() {
        return MapUserCompany.class;
    }

    /**
     * Specifies fetch joins to optimize database queries.
     * Eagerly loads company data to avoid N+1 queries.
     */
    @Override
    public Set<String> getFetchJoins() {
        return Set.of("company");
    }

    /**
     * Uses DISTINCT to handle duplicates from join operations.
     * Essential when fetch joining to prevent duplicate rows.
     */
    @Override
    public boolean shouldUseDistinct() {
        return true;
    }
}