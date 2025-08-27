package com.itt.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.itt.service.entity.Role;

/**
 * Repository for Role entity operations.
 * Provides CRUD operations, role validation, and user privilege retrieval.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Find all roles by active status.
     * @param flag the active status (1 for active, 0 for inactive)
     * @return list of roles matching the active status
     */
    List<Role> findByIsActive(Integer flag);

    /**
     * Check if a role name already exists (case-insensitive).
     * @param name the role name to check
     * @return true if role name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Check if a role name exists for a different role ID (case-insensitive).
     * Used for update operations to prevent duplicates.
     * @param name the role name to check
     * @param id the current role ID to exclude from check
     * @return true if role name exists for different ID, false otherwise
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE UPPER(r.name) = UPPER(:name) AND r.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Integer id);

    /**
     * Get user feature privileges by concatenating feature and privilege key codes.
     * Returns formatted strings like "USER_MANAGEMENT_CREATE" for authorization checks.
     * @param userEmail the email of the user to get privileges for
     * @return list of feature_privilege combinations
     */
    @Query("""
            SELECT CONCAT(f.keyCode, '_', p.keyCode)
            FROM MapRoleCategoryFeaturePrivilegeSkin rcfps
            JOIN rcfps.mapCategoryFeaturePrivilege cfp
            JOIN cfp.feature f
            JOIN cfp.privilege p
            JOIN rcfps.role r
            JOIN MasterUser mu ON mu.assignedRole.id = r.id
            WHERE mu.email = :userEmail
            AND r.isActive = 1
            AND mu.isActive = true
            AND f.configType = 'FEATURE'
            AND p.configType = 'PRIVILEGE'
            ORDER BY f.keyCode, p.keyCode
        """)
    List<String> getUserFeaturePrivileges(@Param("userEmail") String userEmail);
}