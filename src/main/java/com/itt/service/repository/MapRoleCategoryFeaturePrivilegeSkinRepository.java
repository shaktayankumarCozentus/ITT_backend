package com.itt.service.repository;

import com.itt.service.entity.MapRoleCategoryFeaturePrivilegeSkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MapRoleCategoryFeaturePrivilegeSkinRepository
        extends JpaRepository<MapRoleCategoryFeaturePrivilegeSkin, Integer> {

    List<MapRoleCategoryFeaturePrivilegeSkin> findByRole_Id(Integer roleId);

    List<MapRoleCategoryFeaturePrivilegeSkin> findByRole_IdIn(List<Integer> roleIds);

    void deleteByRole_Id(int intValue);
    
    @Modifying
    @Query("DELETE FROM MapRoleCategoryFeaturePrivilegeSkin m WHERE m.role.id IN :roleIds")
    void deleteByRole_IdIn(@Param("roleIds") List<Integer> roleIds);
    
    /**
     * PERFORMANCE OPTIMIZATION: Fetch role mappings with all related data
     * Eliminates N+1 queries by eagerly loading all associations
     * 
     * Fixed: Uses LEFT JOIN for mapCategoryFeaturePrivilege to include skin mappings
     * where mapCategoryFeaturePrivilege is NULL
     * 
     * @param roleId Role ID to fetch mappings for
     * @return List of mappings with all related entities preloaded
     */
    @Query("""
        SELECT DISTINCT m FROM MapRoleCategoryFeaturePrivilegeSkin m
        JOIN FETCH m.role r
        LEFT JOIN FETCH m.mapCategoryFeaturePrivilege cfp
        LEFT JOIN FETCH cfp.featureCategory cat
        LEFT JOIN FETCH cfp.feature feat
        LEFT JOIN FETCH cfp.privilege priv
        LEFT JOIN FETCH m.skinConfig s
        WHERE r.id = :roleId
        ORDER BY CASE WHEN cfp IS NOT NULL THEN cat.intValue ELSE 999999 END,
                 CASE WHEN cfp IS NOT NULL THEN feat.intValue ELSE 999999 END,
                 CASE WHEN cfp IS NOT NULL THEN priv.intValue ELSE 999999 END,
                 CASE WHEN s IS NOT NULL THEN s.intValue ELSE 999999 END
        """)
    List<MapRoleCategoryFeaturePrivilegeSkin> findByRoleIdWithFetch(@Param("roleId") Integer roleId);
    
    /**
     * PERFORMANCE OPTIMIZATION: Batch fetch role mappings with all related data
     * Eliminates N+1 queries by eagerly loading all associations for multiple roles
     * 
     * Fixed: Uses LEFT JOIN for mapCategoryFeaturePrivilege to include skin mappings
     * where mapCategoryFeaturePrivilege is NULL
     * 
     * @param roleIds List of role IDs to fetch mappings for
     * @return List of mappings with all related entities preloaded
     */
    @Query("""
        SELECT DISTINCT m FROM MapRoleCategoryFeaturePrivilegeSkin m
        JOIN FETCH m.role r
        LEFT JOIN FETCH m.mapCategoryFeaturePrivilege cfp
        LEFT JOIN FETCH cfp.featureCategory cat
        LEFT JOIN FETCH cfp.feature feat
        LEFT JOIN FETCH cfp.privilege priv
        LEFT JOIN FETCH m.skinConfig s
        WHERE r.id IN :roleIds
        ORDER BY r.id, 
                 CASE WHEN cfp IS NOT NULL THEN cat.intValue ELSE 999999 END,
                 CASE WHEN cfp IS NOT NULL THEN feat.intValue ELSE 999999 END,
                 CASE WHEN cfp IS NOT NULL THEN priv.intValue ELSE 999999 END,
                 CASE WHEN s IS NOT NULL THEN s.intValue ELSE 999999 END
        """)
    List<MapRoleCategoryFeaturePrivilegeSkin> findByRole_IdInWithFetch(@Param("roleIds") List<Integer> roleIds);
}