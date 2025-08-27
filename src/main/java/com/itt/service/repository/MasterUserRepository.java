package com.itt.service.repository;


import com.itt.service.entity.MasterCompany;
import com.itt.service.entity.MasterUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MasterUser entity operations.
 * Provides CRUD operations, user search, and company assignment retrieval.
 */
@Repository
public interface MasterUserRepository
        extends JpaRepository<MasterUser, Integer>,
        JpaSpecificationExecutor<MasterUser> {

    /**
     * Find all users with pagination and specification support.
     * Uses EntityGraph to eagerly fetch companies and role relationships for performance.
     * @param spec the specification for filtering users
     * @param pageable the pagination information
     * @return paginated result with users and their related entities
     */
    @Override
    @EntityGraph(
            value = "MasterUser.withCompaniesAndRole",
            type  = EntityGraphType.LOAD
    )
    Page<MasterUser> findAll(
            org.springframework.data.jpa.domain.Specification<MasterUser> spec,
            Pageable pageable
    );

    /**
     * Fetch all companies assigned to a specific user.
     * @param userId the ID of the user to get assigned companies for
     * @return list of companies assigned to the user
     */
    @Query("""
      SELECT c
      FROM MasterUser u
      JOIN u.companies c
      WHERE u.id = :userId
    """)
    List<MasterCompany> findAssignedCompanies(@Param("userId") Integer userId);

    /**
     * Find user ID by email address.
     * Used for authentication and user lookup operations.
     * @param email the email address to search for
     * @return the user ID associated with the email
     */
    @Query("SELECT u.id FROM MasterUser u WHERE u.email = :email")
    Integer findUserIdByEmail(@Param("email") String email);

}