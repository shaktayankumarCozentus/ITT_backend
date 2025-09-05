package com.itt.service.validator;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component("releaseManualValidator")
public class ReleaseManualValidator implements SortFieldValidator {

    private static final Set<String> VALID_FIELDS = Set.of(
        "id",
        "noteType",
        "releaseUserManualName",
        "dateOfReleaseNote",
        "uploadedOn",
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
