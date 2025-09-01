package com.itt.service.config.openapi;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.PaginationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

public interface RoleManagementDocumentation {

    @Operation(
        summary = "Search roles with full details",
        description = "Retrieve paginated list of roles with complete privilege hierarchy, skin configurations, and landing page settings. Supports filtering and sorting.",
        operationId = "searchRolesWithPrivileges",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Role search request",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.itt.service.dto.DataTableRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Search Request Example",
                    value = com.itt.service.config.openapi.ApiExamples.RoleManagement.SEARCH_REQUEST
                )
            )
        )
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<PaginationResponse<com.itt.service.dto.role.RoleWithPrivilegesDto>>> searchRolesWithPrivileges(
        @Valid @RequestBody com.itt.service.dto.DataTableRequest request
    );

    @Operation(
        summary = "Get global privilege hierarchy",
        description = "Retrieve the complete feature-privilege hierarchy tree for dropdown population. Returns all available categories, features, and privileges without role-specific selections.",
        operationId = "getGlobalPrivilegeHierarchy"
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.role.FeaturePrivilegeDTO>>> dropdownPrivileges();

    @Operation(
        summary = "Get role-specific privilege hierarchy",
        description = "Retrieve the complete feature-privilege hierarchy tree for a specific role. Returns all available privileges with isSelected flags indicating which ones are assigned to the role.",
        operationId = "getRolePrivilegeHierarchy"
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.role.FeaturePrivilegeDTO>>> rolePrivileges(
        @Parameter(
            description = "Unique identifier of the role (must be >= 1)",
            required = true,
            example = "1"
        )
        @PathVariable @Min(1) Long roleId
    );

    @Operation(
        summary = "Get available landing pages",
        description = "Retrieve all available landing page configurations for dropdown population in role management forms.",
        operationId = "getLandingPages"
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.role.RoleWithPrivilegesDto.LandingPageDto>>> getLandingPages();

    @Operation(
        summary = "Get available skin groups",
        description = "Retrieve all available skin group configurations organized by role type for dropdown population in role management forms. Each skin group includes both role type details (id, key, name) and associated skins.",
        operationId = "getSkinGroups"  
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.role.SkinGroupDto>>> getSkinGroups();

    @Operation(
        summary = "Create a new role",
        description = "Create a new role with complete configuration including privileges, landing page, skin settings, and role type. All assigned privileges and configurations will be saved.",
        operationId = "createRole",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Role creation request with all configuration details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.itt.service.dto.role.SaveRoleRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Create Role Request Example",
                    value = com.itt.service.config.openapi.ApiExamples.RoleManagement.CREATE_ROLE_REQUEST
                )
            )
        )
    )
    @CommonApiResponses.CreateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<?> createRoleFull(
        @Valid @RequestBody com.itt.service.dto.role.SaveRoleRequest req,
        @RequestAttribute(required = true) com.itt.service.dto.CurrentUserDto currentUser
    );

    @Operation(
        summary = "Update an existing role",
        description = "Update an existing role's configuration including name, description, privileges, landing page, skin settings, and role type. All privileges will be replaced with the new assignments.",
        operationId = "updateRole",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Role update request with all configuration details. The ID field will be automatically set to the path parameter.",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.itt.service.dto.role.SaveRoleRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Update Role Request Example",
                    value = com.itt.service.config.openapi.ApiExamples.RoleManagement.CREATE_ROLE_REQUEST
                )
            )
        )
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<?> updateRoleFull(
        @PathVariable @Min(1) Long roleId,
        @Valid @RequestBody com.itt.service.dto.role.SaveRoleRequest req,
        @RequestAttribute(required = true) com.itt.service.dto.CurrentUserDto currentUser
    );
}
