package com.itt.service.fw.search;

import com.itt.service.validator.SortFieldValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal sort field validator that works with any SearchableEntity.
 * Automatically validates sort fields based on entity configuration.
 */
@Component
@Qualifier("universalSortFieldValidator")
public class UniversalSortFieldValidator implements SortFieldValidator {

    private final Map<Class<?>, Set<String>> entitySortableFields = new ConcurrentHashMap<>();

    /**
     * Register an entity's sortable fields
     */
    public <T> void registerEntity(SearchableEntity<T> searchableEntity) {
        entitySortableFields.put(searchableEntity.getEntityClass(), searchableEntity.getSortableFields());
    }

    /**
     * Validate sort field for a specific entity class
     */
    public boolean isValidSortField(String field, Class<?> entityClass) {
        Set<String> validFields = entitySortableFields.get(entityClass);
        return validFields != null && validFields.contains(field);
    }

    @Override
    public boolean isValidSortField(String field) {
        // Default implementation - checks against all registered entities
        return entitySortableFields.values().stream()
                .anyMatch(fields -> fields.contains(field));
    }

    @Override
    public Set<String> getValidSortFields() {
        // Return union of all registered entity sort fields
        return entitySortableFields.values().stream()
                .flatMap(Set::stream)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Get sort fields for a specific entity
     */
    public Set<String> getValidSortFields(Class<?> entityClass) {
        return entitySortableFields.getOrDefault(entityClass, Set.of());
    }
}
