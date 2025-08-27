package com.itt.service.shared.builders;

import com.itt.service.dto.role.SaveRoleRequest;
import com.itt.service.entity.Role;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Service Test Data Builder - Focused on Service Layer Testing
 * 
 * Provides quick test data creation for service layer unit tests.
 * Uses pure mock-based testing without database connectivity.
 */
public class ServiceTestDataBuilder {
    
    /**
     * Creates a valid SaveRoleRequest for testing.
     * 
     * @param name the role name
     * @return valid SaveRoleRequest with standard test data
     */
    public static SaveRoleRequest validRoleRequest(String name) {
        SaveRoleRequest request = new SaveRoleRequest();
        request.setName(name);
        request.setDescription(name + " Description");
        request.setIsActive(true);
        request.setRoleTypeConfigId(1L);
        request.setSkinConfigIds(Arrays.asList(1L));
        request.setPrivilegeIds(Arrays.asList(1L, 2L));
        request.setCreatedById(1);
        request.setUpdatedById(1);
        return request;
    }
    
    /**
     * Creates an active Role entity for mocking repository responses.
     * 
     * @param name the role name
     * @return active Role entity with standard test data
     */
    public static Role activeRole(String name) {
        Role role = new Role();
        role.setId(1);
        role.setName(name);
        role.setDescription(name + " Description");
        role.setIsActive(1);
        role.setCreatedOn(LocalDateTime.now());
        role.setCreatedById(1);
        role.setUpdatedOn(LocalDateTime.now());
        role.setUpdatedById(1);
        return role;
    }
}
