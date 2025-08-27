package com.itt.service.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component("masterCompanyValidator")
public class MasterCompanyValidator implements SortFieldValidator {

    private static final Set<String> VALID_FIELDS = Set.of(
        "id",
        "companyName",
        "companyCode",
        "createdOn",
        "createdById",
        "updatedOn",
        "isRmParentCompany"
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
