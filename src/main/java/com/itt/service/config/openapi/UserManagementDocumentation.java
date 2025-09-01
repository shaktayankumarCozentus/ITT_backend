package com.itt.service.config.openapi;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.CurrentUserDto;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.user_management.AccessAssignmentRequestDto;
import com.itt.service.dto.user_management.AccessSummaryResponseDto;
import com.itt.service.dto.user_management.AssignedCompanyDto;
import com.itt.service.dto.user_management.ChildCompaniesRequestDto;
import com.itt.service.dto.user_management.CompanyDto;
import com.itt.service.dto.user_management.CompanyTreeNode;
import com.itt.service.dto.user_management.CompanyTreeRequestDto;
import com.itt.service.dto.user_management.CompanyTreeResponseDto;
import com.itt.service.dto.user_management.CopyAccessRequestDto;
import com.itt.service.dto.user_management.RoleDto;
import com.itt.service.dto.user_management.SearchCompanyRequestDto;
import com.itt.service.dto.user_management.SearchForCopyRequestDto;
import com.itt.service.dto.user_management.SearchUsersRequestDto;
import com.itt.service.dto.user_management.SearchUsersResponseDto;
import com.itt.service.dto.user_management.TopLevelCompaniesRequestDto;
import com.itt.service.dto.user_management.UserCompanyDto;
import com.itt.service.dto.user_management.UserCountResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface UserManagementDocumentation {

    @Operation(
        summary = "List and search users",
        description = "Returns paginated list of users. Supports filters, sorting, and active role switch. If no search criteria provided, returns all users.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Search user request",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Search Users Request Example",
                    value = com.itt.service.config.openapi.ApiExamples.UserManagement.SEARCH_USERS_REQUEST
                )
            )
        )
    )
    ResponseEntity<ApiResponse<PaginationResponse<SearchUsersResponseDto>>> getUsers(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestBody(description = "Search user request", required = true) SearchUsersRequestDto request
    );

    @Operation(summary = "Get user count by role activity")
    ResponseEntity<ApiResponse<UserCountResponseDto>> getUserCount(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestParam String type
    );

    @Operation(summary = "Get all active or inactive roles")
    ResponseEntity<ApiResponse<List<RoleDto>>> getActiveRoles(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestParam boolean active
    );

    @Operation(
        summary = "List and search companies",
        description = "Returns companies with PSA/BDP toggle. If no search criteria provided, returns all companies.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Company search filter",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Company Search Request Example",
                    value = "{\"searchText\":\"Acme\"}"
                )
            )
        )
    )
    ResponseEntity<ApiResponse<List<CompanyTreeNode>>> searchCompanies(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestBody(description = "Company search filter", required = true) SearchCompanyRequestDto request
    );

    @Operation(
        summary = "Update user role and company assignments",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Access assignment payload",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Update User Assignments Request Example",
                    value = com.itt.service.config.openapi.ApiExamples.UserManagement.UPDATE_USER_ASSIGNMENTS_REQUEST
                )
            )
        )
    )
    ResponseEntity<ApiResponse<Void>> updateUserAssignments(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @Parameter(description = "User ID") @RequestParam String userId,
        @RequestBody(description = "Access assignment payload", required = true) AccessAssignmentRequestDto request
    );

    @Operation(
        summary = "Search user for copy access",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Search for copy target",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Search User For Copy Request Example",
                    value = "{\"searchText\":\"copy\"}"
                )
            )
        )
    )
    ResponseEntity<ApiResponse<List<SearchUsersResponseDto>>> searchUserForCopy(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestBody(description = "Search for copy target", required = true) SearchForCopyRequestDto request
    );

    @Operation(
        summary = "Copy access from one user to another",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Copy access request",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Copy Access Request Example",
                    value = "{\"fromUserId\":\"101\",\"toUserId\":\"102\"}"
                )
            )
        )
    )
    ResponseEntity<ApiResponse<Void>> copyUserAccess(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestBody(description = "Copy access request", required = true) CopyAccessRequestDto request
    );

    @Operation(
        summary = "Load top-level companies",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Top level companies request",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Top Level Companies Request Example",
                    value = "{\"type\":\"PARENT\"}"
                )
            )
        )
    )
    ResponseEntity<ApiResponse<List<CompanyDto>>> getTopLevelCompanies(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestBody(description = "Top level companies request", required = true) TopLevelCompaniesRequestDto request
    );

    @Operation(
        summary = "Load child companies by parent IDs",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Parent companies payload",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Child Companies Request Example",
                    value = "{\"parentIds\":[1,2]}"
                )
            )
        )
    )
    ResponseEntity<ApiResponse<Map<String, List<CompanyDto>>>> getChildCompanies(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestBody(description = "Parent companies payload", required = true) ChildCompaniesRequestDto request
    );

    @Operation(summary = "Get companies assigned to a user")
    ResponseEntity<ApiResponse<List<AssignedCompanyDto>>> getUserAssignedCompanies(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @Parameter(description = "User ID") @RequestParam String userId
    );

    @Operation(summary = "Get user access summary for copy preview")
    ResponseEntity<ApiResponse<AccessSummaryResponseDto>> getUserAccessSummary(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @Parameter(description = "User ID") @RequestParam String userId
    );

    @Operation(
        summary = "Get company tree structure",
        description = "Returns the full company hierarchy, paginated. Pass `type=PSA`, `NON-PSA`.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Pagination + type filter",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Company Tree Request Example",
                    value = "{\"type\":\"PSA\",\"page\":0,\"size\":10}"
                )
            )
        )
    )
    ResponseEntity<ApiResponse<CompanyTreeResponseDto>> getCompanyTree(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @RequestBody(description = "Pagination + type filter", required = true) CompanyTreeRequestDto request
    );

    @Operation(
        summary = "List and search user companies",
        description = "Returns paginated list of companies assigned to a user. Supports search functionality. If no search criteria provided, returns all user companies. Fixed page size of 500.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Search request with pagination",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "User Companies Search Request Example",
                    value = com.itt.service.config.openapi.ApiExamples.UserManagement.SEARCH_USERS_REQUEST
                )
            )
        )
    )
    ResponseEntity<ApiResponse<PaginationResponse<UserCompanyDto>>> searchUserCompanies(
        @Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
        @Parameter(description = "User ID") @RequestParam Integer userId,
        @RequestBody(description = "Search request with pagination", required = true) SearchUsersRequestDto request
    );
}
