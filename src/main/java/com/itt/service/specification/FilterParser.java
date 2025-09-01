package com.itt.service.specification;

import java.util.Set;

import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterParser {
    private static final Logger logger = LoggerFactory.getLogger(FilterParser.class);
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
        logger.debug("FilterParser: Parsing filter '{}'", filter);
        
        if (!StringUtils.hasText(filter)) {
            logger.debug("FilterParser: Empty filter, returning eq:'{}'", filter);
            return new FilterCriteria("eq", filter);
        }
        
        // Handle special null cases
        if ("null".equalsIgnoreCase(filter)) {
            logger.debug("FilterParser: Null filter, returning null operator");
            return new FilterCriteria("null", null);
        }
        if ("notnull".equalsIgnoreCase(filter)) {
            logger.debug("FilterParser: NotNull filter, returning notnull operator");
            return new FilterCriteria("notnull", null);
        }
        
        if (filter.contains(":")) {
            String[] parts = filter.split(":", 2);
            String operator = parts[0].toLowerCase();
            String value = parts[1];
            logger.debug("FilterParser: Split filter '{}' -> operator='{}', value='{}'", filter, operator, value);
            
            // Handle null operators
            if ("null".equals(operator) || "isnull".equals(operator)) {
                logger.debug("FilterParser: Null operator detected");
                return new FilterCriteria("null", null);
            }
            if ("notnull".equals(operator) || "isnotnull".equals(operator)) {
                logger.debug("FilterParser: NotNull operator detected");
                return new FilterCriteria("notnull", null);
            }
            
            // Handle eq:null and eq:notnull cases
            if ("eq".equals(operator) && "null".equalsIgnoreCase(value)) {
                logger.debug("FilterParser: eq:null case");
                return new FilterCriteria("null", null);
            }
            if ("ne".equals(operator) && "null".equalsIgnoreCase(value)) {
                logger.debug("FilterParser: ne:null case");
                return new FilterCriteria("notnull", null);
            }
            if ("eq".equals(operator) && "notnull".equalsIgnoreCase(value)) {
                logger.debug("FilterParser: eq:notnull case");
                return new FilterCriteria("notnull", null);
            }
            if ("ne".equals(operator) && "notnull".equalsIgnoreCase(value)) {
                logger.debug("FilterParser: ne:notnull case");
                return new FilterCriteria("null", null);
            }
            
            Set<String> validOperators = Set.of("eq", "ne", "gt", "lt", "gte", "lte", "in", "cnt", "ncnt", "sw", "ew", 
                      "dgt", "dlt", "dgte", "dlte", "deq", "dne", "dbetween", "null", "notnull", "activeornorole");
            if (validOperators.contains(operator)) {
                logger.debug("FilterParser: Valid operator '{}' found, returning operator='{}', value='{}'", operator, operator, value);
                return new FilterCriteria(operator, value);
            } else {
                logger.debug("FilterParser: Invalid operator '{}', valid operators: {}", operator, validOperators);
            }
        }
        logger.debug("FilterParser: No colon or invalid operator, returning cnt:'{}'", filter);
        return new FilterCriteria("cnt", filter);
    }
}