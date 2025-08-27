package com.itt.service.repository;

import com.itt.service.dto.user_management.CompanyDtoPOc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CompanyRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private static final String FETCH_ALL_QUERY = """
    SELECT
        mc.id,
        mc.company_name AS companyName,
        mc.parent_id     AS parentId,
        mc.created_on    AS onboardedByDate,
        mc.updated_by_id AS updatedBy,
        mcfg.name        AS subscriptionType
         FROM master_company mc
    LEFT JOIN master_config mcfg
           ON mcfg.id = mc.subscription_type_config_id
    WHERE mc.psa_flag = :type
""";

    public List<CompanyDtoPOc> fetchAllCompanies(Integer type) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        return jdbcTemplate.query(
                FETCH_ALL_QUERY,
                params,
                new BeanPropertyRowMapper<>(CompanyDtoPOc.class)
        );
    }

    public List<CompanyDtoPOc> searchHierarchy(String searchText, Boolean isPsaBdp) {
        String sql = """
            WITH RECURSIVE company_hierarchy AS (
              SELECT
                id,
                company_name,
                parent_id,
                created_on AS onboarded_on,
                subscription_type_config_id
              FROM master_company
              WHERE company_name LIKE :searchText
              UNION ALL
              SELECT
                mc.id,
                mc.company_name,
                mc.parent_id,
                mc.created_on,
                mc.subscription_type_config_id
              FROM master_company mc
              JOIN company_hierarchy ch
                ON mc.id = ch.parent_id
            )
            SELECT
              id,
              company_name    AS companyName,
              parent_id       AS parentId,
              onboarded_on    AS onboardedOn,
              subscription_type_config_id AS subscriptionTypeConfigId
            FROM company_hierarchy
            """;

        Map<String,Object> params = new HashMap<>();
        params.put("searchText", "%" + searchText + "%");

        // inject PSA filter if needed
        if (isPsaBdp != null) {
            sql = sql.replace(
                    "/**%PSA_FILTER%**/",
                    "AND psa_flag = :psaFlag"
            );
            params.put("psaFlag", isPsaBdp ? 1 : 0);
        } else {
            sql = sql.replace("/**%PSA_FILTER%**/", "");
        }

        return jdbcTemplate.query(
                sql,
                params,
                BeanPropertyRowMapper.newInstance(CompanyDtoPOc.class)
        );
    }
}
