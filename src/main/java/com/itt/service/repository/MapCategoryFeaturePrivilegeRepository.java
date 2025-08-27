package com.itt.service.repository;

import com.itt.service.entity.MapCategoryFeaturePrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MapCategoryFeaturePrivilegeRepository
    extends JpaRepository<MapCategoryFeaturePrivilege, Integer> {

  @Query("""
    SELECT m 
    FROM MapCategoryFeaturePrivilege m
    JOIN FETCH m.featureCategory cat
    JOIN FETCH m.feature feat
    JOIN FETCH m.privilege priv
    ORDER BY cat.intValue, feat.intValue, priv.intValue
  """)
  List<MapCategoryFeaturePrivilege> findAllFull();
  
  /**
   * Batch query to find all existing privilege IDs from the given set.
   * This is optimized for the validator to reduce database round trips.
   * 
   * @param privilegeIds Set of privilege IDs to check
   * @return List of existing privilege IDs
   */
  @Query("SELECT m.id FROM MapCategoryFeaturePrivilege m WHERE m.id IN :privilegeIds")
  List<Integer> findAllExistingIds(@Param("privilegeIds") Set<Integer> privilegeIds);
}