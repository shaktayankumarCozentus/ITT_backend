package com.itt.service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.CurrentUserDto;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.user_management.AccessAssignmentRequestDto;
import com.itt.service.dto.user_management.AssignedCompaniesResponseDto;
import com.itt.service.dto.user_management.CompanyTreeRequestDto;
import com.itt.service.dto.user_management.CompanyTreeResponseDto;
import com.itt.service.dto.user_management.CopyAccessRequestDto;
import com.itt.service.dto.user_management.RoleDto;
import com.itt.service.dto.user_management.SearchForCopyRequestDto;
import com.itt.service.dto.user_management.SearchUsersRequestDto;
import com.itt.service.dto.user_management.SearchUsersResponseDto;
import com.itt.service.dto.user_management.UserCompanyDto;
import com.itt.service.dto.user_management.UserCountResponseDto;
import com.itt.service.service.UserManagementService;
import com.itt.service.util.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "User Management", description = "User, role, and company management APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-management")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserManagementController {

	private final UserManagementService userManagementService;

	@Operation(summary = "List and search users", description = "Returns paginated list of users. Supports filters, sorting, and new role-based tabs: ACTIVE_OR_NO_ROLE (users with active role or no role) and INACTIVE_ROLE (users with inactive role only). If no search criteria provided, returns all users.")
	@PostMapping("/users")
	public ResponseEntity<ApiResponse<PaginationResponse<SearchUsersResponseDto>>> getUsers(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
			@RequestBody(description = "Search user request", required = true) @org.springframework.web.bind.annotation.RequestBody SearchUsersRequestDto request) {
		PaginationResponse<SearchUsersResponseDto> result = userManagementService.getUsers(currentUser, request);
		return ResponseBuilder.dynamicResponse(result);
	}

	@Operation(summary = "Get user count by role activity")
	@GetMapping("/user-count")
	public ResponseEntity<ApiResponse<UserCountResponseDto>> getUserCount(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
			@RequestParam String type) {
		UserCountResponseDto count = userManagementService.getUserCount(currentUser, type);
		return ResponseBuilder.success(count);
	}

	@Operation(summary = "Get all active or inactive roles")
	@GetMapping("/roles")
	public ResponseEntity<ApiResponse<List<RoleDto>>> getActiveRoles(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
			@RequestParam boolean active) {
		List<RoleDto> roles = userManagementService.getActiveRoles(currentUser, active);
		return ResponseBuilder.success(roles);
	}

	@Operation(summary = "Update user role and company assignments")
	@PutMapping("/user-assignments")
	public ResponseEntity<ApiResponse<Void>> updateUserAssignments(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
			@Parameter(description = "User ID") @RequestParam String userId,
			@RequestBody(description = "Access assignment payload", required = true) @org.springframework.web.bind.annotation.RequestBody AccessAssignmentRequestDto request) {
		String message = userManagementService.updateUserAccessAssignments(currentUser, userId, request);
		return ResponseBuilder.success(message);
	}

	@Operation(summary = "Search user for copy access")
	@PostMapping("/search-for-copy")
	public ResponseEntity<ApiResponse<List<SearchUsersResponseDto>>> searchUserForCopy(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
			@RequestBody(description = "Search for copy target", required = true) @org.springframework.web.bind.annotation.RequestBody SearchForCopyRequestDto request) {
		List<SearchUsersResponseDto> users = userManagementService.searchUserForCopy(currentUser, request);
		return ResponseBuilder.success(users);
	}

	@Operation(summary = "Copy access from one user to another")
	@PostMapping("/copy-access")
	public ResponseEntity<ApiResponse<Void>> copyUserAccess(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
			@RequestBody(description = "Copy access request", required = true) @org.springframework.web.bind.annotation.RequestBody CopyAccessRequestDto request) {
		String message = userManagementService.copyUserAccess(currentUser, request);
		return ResponseBuilder.success(message);
	}

	@Operation(summary = "Get companies assigned to a user")
	@GetMapping("/user-assigned-companies")
	public ResponseEntity<ApiResponse<AssignedCompaniesResponseDto>> getUserAssignedCompanies(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
			@Parameter(description = "User ID") @RequestParam String userId) {
		AssignedCompaniesResponseDto assigned = userManagementService.getAssignedCompanies(currentUser, userId);
		return ResponseBuilder.success(assigned);
	}

	@Operation(summary = "Get company tree structure", description = "Returns the full company hierarchy, paginated.  "
			+ "Pass `type=PSA`, `NON-PSA`.")
	@PostMapping("/company-tree")
	public ResponseEntity<ApiResponse<CompanyTreeResponseDto>> getCompanyTree(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,

			@RequestBody(description = "Pagination + type filter", required = true) @org.springframework.web.bind.annotation.RequestBody CompanyTreeRequestDto request) {
		CompanyTreeResponseDto result = userManagementService.buildCompanyTree(request);
		return ResponseBuilder.success(result);
	}

	@Operation(summary = "List and search user companies", description = "Returns paginated list of companies assigned to a user. Supports search functionality. If no search criteria provided, returns all user companies. Fixed page size of 500.")
	@PostMapping("/user-companies")
	public ResponseEntity<ApiResponse<PaginationResponse<UserCompanyDto>>> searchUserCompanies(
			@Parameter(hidden = true) @RequestAttribute(value = "currentUser", required = false) CurrentUserDto currentUser,
			@Parameter(description = "User ID") @RequestParam Integer userId,
			@RequestBody(description = "Search request with pagination", required = true) @org.springframework.web.bind.annotation.RequestBody SearchUsersRequestDto request) {
		PaginationResponse<UserCompanyDto> result = userManagementService.getUserCompanies(currentUser, userId,
				request);
		return ResponseBuilder.dynamicResponse(result);
	}
}