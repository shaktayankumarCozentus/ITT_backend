package com.itt.service.controller;

import com.itt.service.config.openapi.RoleManagementDocumentation;
import com.itt.service.dto.*;
import com.itt.service.dto.role.FeaturePrivilegeDTO;
import com.itt.service.dto.role.RoleWithPrivilegesDto;
import com.itt.service.dto.role.RoleWithPrivilegesDto.LandingPageDto;
import com.itt.service.dto.role.SaveRoleRequest;
import com.itt.service.dto.role.SkinGroupDto;
import com.itt.service.service.RoleManagementService;
import com.itt.service.util.ResponseBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Clean REST controller for Role Management operations.
 * <p>
 * This controller focuses purely on HTTP request/response handling and
 * delegates
 * business logic to the service layer. OpenAPI documentation is separated into
 * the RoleManagementDocumentation interface for better maintainability.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Validated
@Tag(name = "Role Management", description = "APIs for managing user roles, privileges, and configurations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RoleManagementController implements RoleManagementDocumentation {

    private final RoleManagementService managementService;

    /**
     * Search roles with pagination and full privilege details.
     */
    @PostMapping("/all-details")
    @PreAuthorize("hasRole('ROLE_ROLE_MANAGEMENT_VIEW')")
    @Override
    public ResponseEntity<ApiResponse<PaginationResponse<RoleWithPrivilegesDto>>> searchRolesWithPrivileges(
            @Valid @RequestBody DataTableRequest request) {
        var page = managementService.getPaginatedRolesWithFullPrivilegesAndConfigs(request);
        return ResponseBuilder.dynamicResponse(page);
    }

    /**
     * Get global privilege hierarchy for dropdowns.
     */
    @GetMapping("/privilege-hierarchy")
    @PreAuthorize("hasRole('ROLE_ROLE_MANAGEMENT_VIEW')")
    @Override
    public ResponseEntity<ApiResponse<List<FeaturePrivilegeDTO>>> dropdownPrivileges() {
        var tree = managementService.getPrivilegeHierarchy(Optional.empty());
        return ResponseBuilder.success(tree);
    }

    /**
     * Get role-specific privilege hierarchy with selection flags.
     */
    @GetMapping("/{roleId}/privilege-hierarchy")
    @PreAuthorize("hasRole('ROLE_ROLE_MANAGEMENT_VIEW')")
    @Override
    public ResponseEntity<ApiResponse<List<FeaturePrivilegeDTO>>> rolePrivileges(
            @PathVariable @Min(1) Long roleId) {
        var tree = managementService.getPrivilegeHierarchy(Optional.of(roleId));
        return ResponseBuilder.success(tree);
    }

    /**
     * Get available landing page configurations.
     */
    @GetMapping("/landing-pages")
    @PreAuthorize("hasRole('ROLE_ROLE_MANAGEMENT_VIEW')")
    @Override
    public ResponseEntity<ApiResponse<List<LandingPageDto>>> getLandingPages() {
        var landingPages = managementService.getLandingPages();
        return ResponseBuilder.success(landingPages);
    }

    /**
     * Get skin group configurations organized by role type.
     */
    @GetMapping("/skins")
    @PreAuthorize("hasRole('ROLE_ROLE_MANAGEMENT_VIEW')")
    @Override
    public ResponseEntity<ApiResponse<List<SkinGroupDto>>> getSkinGroups() {
        var skinGroups = managementService.getSkinGroups();
        return ResponseBuilder.success(skinGroups);
    }

    /**
     * Create a new role with full configuration.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ROLE_MANAGEMENT_EDIT')")
    @Override
    public ResponseEntity<?> createRoleFull(
            @Valid @RequestBody SaveRoleRequest req,
            @RequestAttribute(required = true) CurrentUserDto currentUser) {
        req.setCreatedById(currentUser.getUserId());
        req.setUpdatedById(currentUser.getUserId());
        var dto = managementService.saveOrUpdateFull(req);
        return ResponseBuilder.created("Role created successfully", dto);
    } 

    /**
     * Update an existing role with new configuration.
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('ROLE_ROLE_MANAGEMENT_EDIT')")
    @Override
    public ResponseEntity<?> updateRoleFull(
            @PathVariable @Min(1) Long roleId,
            @Valid @RequestBody SaveRoleRequest req,
            @RequestAttribute(required = true) CurrentUserDto currentUser) {
        req.setId(roleId);
        req.setUpdatedById(currentUser.getUserId());
        var dto = managementService.saveOrUpdateFull(req);
        return ResponseBuilder.success("Role updated successfully", dto);
    }
}
