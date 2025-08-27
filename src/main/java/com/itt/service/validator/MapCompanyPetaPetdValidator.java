package com.itt.service.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component("mapCompanyPetaPetdValidator")
public class MapCompanyPetaPetdValidator implements SortFieldValidator {

    private static final Set<String> VALID_FIELDS = Set.of(
        "id",
        "companyId",
        "petaPetdEnabledFlag",
        "oceanFrequency",
        "airFrequency",
        "railRoadFrequency",
        "createdOn",
        "createdById",
        "updatedOn",
        "updatedById"
    );

    @Override
    public boolean isValidSortField(String field) {
        return VALID_FIELDS.contains(field);
    }

    @Override
    public Set<String> getValidSortFields() {
        return VALID_FIELDS;
    }
}