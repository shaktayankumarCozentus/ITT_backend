package com.itt.service.repository;


import com.itt.service.entity.MapUserCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface MapUserCompanyRepository extends JpaRepository<MapUserCompany, Integer> {

    /**
     * Bulk‐delete all assignments for a given user in one SQL.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM MapUserCompany m WHERE m.userId = :userId")
    void deleteByUserId(Integer userId);

    /**
     * PERFORMANCE OPTIMIZATION: Batch fetch company counts for multiple users.
     * Instead of N queries (one per user), this executes 1 GROUP BY query for all users.
     * 
     * @param userIds List of user IDs to get company counts for
     * @return List of Object arrays where [0] = userId (Integer), [1] = count (Long)
     */
    @Query("SELECT m.userId, COUNT(m.companyId) FROM MapUserCompany m WHERE m.userId IN :userIds GROUP BY m.userId")
    List<Object[]> countCompaniesByUserIds(@Param("userIds") List<Integer> userIds);
}