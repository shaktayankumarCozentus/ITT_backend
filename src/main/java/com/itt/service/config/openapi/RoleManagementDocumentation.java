package com.itt.service.config.openapi;

import com.itt.service.dto.*;
import com.itt.service.dto.role.FeaturePrivilegeDTO;
import com.itt.service.dto.role.RoleWithPrivilegesDto;
import com.itt.service.dto.role.SaveRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;

import static com.itt.service.config.openapi.ApiExamples.RoleManagement;

/**
 * OpenAPI documentation interface for Role Management Controller.
 * This interface separates the API documentation from the actual controller implementation,
 * making the controller cleaner and more focused on business logic.
 */
public interface RoleManagementDocumentation {

    @Operation(
        summary = "Search roles with full details",
        description = "Retrieve paginated list of roles with complete privilege hierarchy, skin configurations, and landing page settings. Supports filtering and sorting.",
        operationId = "searchRolesWithPrivileges"
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<PaginationResponse<RoleWithPrivilegesDto>>> searchRolesWithPrivileges(
        @Parameter(
            description = "Pagination and filtering criteria for role search",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DataTableRequest.class),
                examples = @ExampleObject(
                    name = "Search Request",
                    value = RoleManagement.SEARCH_REQUEST
                )
            )
        )
        @Valid @RequestBody DataTableRequest request
    );

    @Operation(
        summary = "Get global privilege hierarchy",
        description = "Retrieve the complete feature-privilege hierarchy tree for dropdown population. Returns all available categories, features, and privileges without role-specific selections.",
        operationId = "getGlobalPrivilegeHierarchy"
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<List<FeaturePrivilegeDTO>>> dropdownPrivileges();

    @Operation(
        summary = "Get role-specific privilege hierarchy",
        description = "Retrieve the complete feature-privilege hierarchy tree for a specific role. Returns all available privileges with isSelected flags indicating which ones are assigned to the role.",
        operationId = "getRolePrivilegeHierarchy"
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<List<FeaturePrivilegeDTO>>> rolePrivileges(
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
    ResponseEntity<ApiResponse<List<RoleWithPrivilegesDto.LandingPageDto>>> getLandingPages();

    @Operation(
        summary = "Get available skin groups",
        description = "Retrieve all available skin group configurations organized by role type for dropdown population in role management forms. Each skin group includes both role type details (id, key, name) and associated skins.",
        operationId = "getSkinGroups"  
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<List<com.itt.service.dto.role.SkinGroupDto>>> getSkinGroups();

    @Operation(
        summary = "Create a new role",
        description = "Create a new role with complete configuration including privileges, landing page, skin settings, and role type. All assigned privileges and configurations will be saved.",
        operationId = "createRole"
    )
    @CommonApiResponses.CreateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<?> createRoleFull(
        @Parameter(
            description = "Role creation request with all configuration details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SaveRoleRequest.class),
                examples = @ExampleObject(
                    name = "Create Role Request",
                    value = RoleManagement.CREATE_ROLE_REQUEST
                )
            )
        )
        @Valid @RequestBody SaveRoleRequest req,
        @RequestAttribute(required = true) CurrentUserDto currentUser
    );

    @Operation(
        summary = "Update an existing role",
        description = "Update an existing role's configuration including name, description, privileges, landing page, skin settings, and role type. All privileges will be replaced with the new assignments.",
        operationId = "updateRole"
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<?> updateRoleFull(
        @Parameter(
            description = "Unique identifier of the role to update (must be >= 1)",
            required = true,
            example = "1"
        )
        @PathVariable @Min(1) Long roleId,
        @Parameter(
            description = "Role update request with all configuration details. The ID field will be automatically set to the path parameter.",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SaveRoleRequest.class),
                examples = @ExampleObject(
                    name = "Update Role Request",
                    value = RoleManagement.UPDATE_ROLE_REQUEST
                )
            )
        )
        @Valid @RequestBody SaveRoleRequest req,
        @RequestAttribute(required = true) CurrentUserDto currentUser
    );
}
