package com.itt.service.validator;

import java.util.Set;

public interface SortFieldValidator {
    boolean isValidSortField(String field);
    Set<String> getValidSortFields();
}