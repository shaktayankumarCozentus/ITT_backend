package com.itt.service.specification;

import java.util.Set;

import org.springframework.util.StringUtils;

public class FilterParser {
    public static class FilterCriteria {
        private String operator;
        private String value;

        public FilterCriteria(String operator, String value) {
            this.operator = operator;
            this.value = value;
        }

        public String getOperator() { return operator; }
        public String getValue() { return value; }
    }

    public static FilterCriteria parseFilter(String filter) {
        if (!StringUtils.hasText(filter)) {
            return new FilterCriteria("eq", filter);
        }
        if (filter.contains(":")) {
            String[] parts = filter.split(":", 2);
            String operator = parts[0].toLowerCase();
            String value = parts[1];
            if (Set.of("eq", "ne", "gt", "lt", "gte", "lte", "in", "cnt", "ncnt", "sw", "ew", 
                      "dgt", "dlt", "dgte", "dlte", "deq", "dne", "dbetween").contains(operator)) {
                return new FilterCriteria(operator, value);
            }
        }
        return new FilterCriteria("cnt", filter);
    }
}