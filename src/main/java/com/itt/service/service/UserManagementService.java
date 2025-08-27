package com.itt.service.service;

import java.util.List;
import java.util.Map;

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

public interface UserManagementService {

	PaginationResponse<SearchUsersResponseDto> getUsers(CurrentUserDto user, SearchUsersRequestDto request);

	UserCountResponseDto getUserCount(CurrentUserDto user, String type);

	List<RoleDto> getActiveRoles(CurrentUserDto user, boolean active);

	List<CompanyTreeNode> searchCompanies(CurrentUserDto user, SearchCompanyRequestDto request);

	List<SearchUsersResponseDto> searchUserForCopy(CurrentUserDto user, SearchForCopyRequestDto request);

	String updateUserAccessAssignments(CurrentUserDto user, String userId, AccessAssignmentRequestDto request);

	String copyUserAccess(CurrentUserDto user, CopyAccessRequestDto request);

	List<CompanyDto> getTopLevelCompanies(CurrentUserDto user, TopLevelCompaniesRequestDto request);

	Map<String, List<CompanyDto>> getChildCompanies(CurrentUserDto user, ChildCompaniesRequestDto request);

	List<AssignedCompanyDto> getAssignedCompanies(CurrentUserDto user, String userId);

	AccessSummaryResponseDto getAccessSummary(CurrentUserDto user, String userId);

	CompanyTreeResponseDto buildCompanyTree(CompanyTreeRequestDto request);

	/**
	 * Get paginated companies for a specific user using Universal Search Framework.
	 * 
	 * @param user Current authenticated user for authorization
	 * @param userId Target user ID to get companies for
	 * @param request Search request with pagination and search criteria
	 * @return Paginated list of user companies with search and sort capabilities
	 */
	PaginationResponse<UserCompanyDto> getUserCompanies(CurrentUserDto user, Integer userId, SearchUsersRequestDto request);
}