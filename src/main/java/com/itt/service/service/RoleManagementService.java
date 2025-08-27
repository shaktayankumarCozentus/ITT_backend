package com.itt.service.service;

import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.role.*;
import com.itt.service.dto.role.RoleWithPrivilegesDto.LandingPageDto;

import java.util.List;
import java.util.Optional;

public interface RoleManagementService {

    List<FeaturePrivilegeDTO> getPrivilegeHierarchy(Optional<Long> roleId);
    PaginationResponse<RoleWithPrivilegesDto> getPaginatedRolesWithFullPrivilegesAndConfigs(DataTableRequest req);
    List<LandingPageDto> getLandingPages();
    List<SkinGroupDto> getSkinGroups();
    RoleDto saveOrUpdateFull(SaveRoleRequest req);
    
}