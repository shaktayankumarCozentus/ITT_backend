package com.itt.service.repository;

import com.itt.service.entity.UserPlanMapping;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPlanMappingRepository extends JpaRepository<UserPlanMapping, Integer> {

    /**
     * Deactivates the current plan for the specified user.
     *
     * @param userId The unique identifier of the user
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value = """
        UPDATE user_plan_mappings
        SET is_active = false
        WHERE user_id = ?1 and is_active = true
        """)
    void deactivateCurrentPlan(final Integer userId);

    /**
     * Retrieves the active plan for the specified user.
     *
     * @param userId The unique identifier of the user
     * @return The active plan mapping for the user
     */
    @Query(nativeQuery = true, value = """
        SELECT * FROM user_plan_mappings
        WHERE user_id = ?1 AND is_active = true
        """)
    UserPlanMapping getActivePlan(final Integer userId);

    /**
     * Checks if the specified plan is active for the given user.
     *
     * @param userId The unique identifier of the user
     * @param planId The unique identifier of the plan
     * @return true if the plan is active for the user, false otherwise
     */
    @Query(value = """
        SELECT COUNT(id) = 1 FROM UserPlanMapping
        WHERE isActive = true
        AND userId = ?1
        AND planId = ?2
        """)
    boolean isActivePlan(final Integer userId, final Integer planId);

    List<UserPlanMapping> findAllByUserIdInAndIsActiveTrue(List<Integer> userIds);
}
