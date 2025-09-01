package com.itt.service.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.itt.service.annotation.ReadOnlyDataSource;
import com.itt.service.config.search.UserCompanySearchConfig;
import com.itt.service.config.search.UserSearchConfig;
import com.itt.service.dto.CurrentUserDto;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.user_management.AccessAssignmentRequestDto;
import com.itt.service.dto.user_management.AssignedCompaniesResponseDto;
import com.itt.service.dto.user_management.AssignedCompanyDto;
import com.itt.service.dto.user_management.CompanyDtoPOc;
import com.itt.service.dto.user_management.CompanyTreeNode;
import com.itt.service.dto.user_management.CompanyTreeRequestDto;
import com.itt.service.dto.user_management.CompanyTreeResponseDto;
import com.itt.service.dto.user_management.CopyAccessRequestDto;
import com.itt.service.dto.user_management.RoleDto;
import com.itt.service.dto.user_management.SearchCompanyRequestDto;
import com.itt.service.dto.user_management.SearchForCopyRequestDto;
import com.itt.service.dto.user_management.SearchUsersRequestDto;
import com.itt.service.dto.user_management.SearchUsersRequestDto.UserRoleFilter;
import com.itt.service.dto.user_management.SearchUsersResponseDto;
import com.itt.service.dto.user_management.UserCompanyDto;
import com.itt.service.dto.user_management.UserCountResponseDto;
import com.itt.service.entity.MapUserCompany;
import com.itt.service.entity.MasterCompany;
import com.itt.service.entity.MasterUser;
import com.itt.service.entity.Role;
import com.itt.service.enums.ErrorCode;
import com.itt.service.exception.CustomException;
import com.itt.service.fw.search.DynamicSearchQueryBuilder;
import com.itt.service.fw.search.SearchableEntity;
import com.itt.service.fw.search.UniversalSortFieldValidator;
import com.itt.service.repository.CompanyRepository;
import com.itt.service.repository.MapUserCompanyRepository;
import com.itt.service.repository.MasterCompanyRepository;
import com.itt.service.repository.MasterUserRepository;
import com.itt.service.repository.RoleRepository;
import com.itt.service.service.UserManagementService;
import com.itt.service.specification.UserSpecification;
import com.itt.service.validator.SortFieldValidator;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

	private final CompanyRepository companyJDBCRepository;
	private final MasterUserRepository userRepo;
	private final RoleRepository roleRepo;
	private final MasterCompanyRepository masterCompanyRepo;
	private final MapUserCompanyRepository mapRepo;

	// Universal Search Framework components
	private final UserSearchConfig userSearchConfig;
	private final DynamicSearchQueryBuilder queryBuilder;
	private final UniversalSortFieldValidator universalSortValidator;
	private final UserCompanySearchConfig userCompanySearchConfig;

	/**
	 * Enhanced user search using Universal Search Framework.
	 * 
	 * <h3>Performance Optimizations:</h3>
	 * <ul>
	 * <li>Fetch joins for assignedRole and companies to prevent N+1 queries</li>
	 * <li>Entity-specific sort field validation for security</li>
	 * <li>Automatic query generation with advanced search operators</li>
	 * </ul>
	 * 
	 * <h3>Searchable Fields:</h3>
	 * <ul>
	 * <li>name/fullName - User's full name</li>
	 * <li>email - User's email address</li>
	 * <li>role/roleName/assignedRole - Assigned role name</li>
	 * <li>company/companyName/companies - Company names</li>
	 * </ul>
	 */
	@Override
	@ReadOnlyDataSource("Enhanced user search with Universal Search Framework")
	@Transactional(readOnly = true, timeout = 30)
	public PaginationResponse<SearchUsersResponseDto> getUsers(CurrentUserDto user, SearchUsersRequestDto request) {

		// 1. Register entity for sort validation
		universalSortValidator.registerEntity(userSearchConfig);

		// 2. Get DataTableRequest from the wrapper
		DataTableRequest dt = request.getDataTableRequest();

		// 3. Set entity-specific sort validator for security
		dt.setSortFieldValidator(new EntitySpecificSortValidator(userSearchConfig, universalSortValidator));

		// 4. Apply role filter if specified - handle new business logic
		if (request.getUserRoleFilter() != null) {
			if (request.getUserRoleFilter() == UserRoleFilter.ACTIVE_OR_NO_ROLE) {
				// Use special filter that will be handled by UserSearchConfig
				DataTableRequest.Column roleFilter = new DataTableRequest.Column();
				roleFilter.setColumnName("assignedRole.isActive");
				roleFilter.setFilter("activeOrNoRole:1");
				dt.getColumns().add(roleFilter);
			} else if (request.getUserRoleFilter() == UserRoleFilter.INACTIVE_ROLE) {
				// Users with inactive role only (uses INNER JOIN)
				DataTableRequest.Column roleFilter = new DataTableRequest.Column();
				roleFilter.setColumnName("assignedRole.isActive");
				roleFilter.setFilter("eq:0");
				dt.getColumns().add(roleFilter);
			}
		}

		// 5. Execute dynamic search using Universal Search Framework
		Page<MasterUser> page = queryBuilder.findWithDynamicSearch(userSearchConfig, dt);

		// 6. PERFORMANCE OPTIMIZATION: Batch fetch company counts for all users in
		// current page
		List<Integer> userIds = page.getContent().stream().map(MasterUser::getId).toList();

		Map<Integer, Long> companyCounts = getCompanyCountsForUsers(userIds);

		// 7. Convert to response DTOs with optimized mapping
		List<SearchUsersResponseDto> content = page.getContent().stream()
				.map(u -> convertToSearchUsersResponseDto(u, companyCounts.getOrDefault(u.getId(), 0L))).toList();

		// 8. Build paginated response using PaginationResponse (like role controller)
		PaginationResponse<SearchUsersResponseDto> response = new PaginationResponse<>();
		response.setPage(page.getNumber());
		response.setSize(page.getSize());
		response.setTotalElements(page.getTotalElements());
		response.setTotalPages(page.getTotalPages());
		response.setLast(page.isLast());
		response.setContent(content);

		return response;
	}

	/**
	 * PERFORMANCE OPTIMIZATION: Batch fetch company counts for multiple users.
	 * Instead of N queries (one per user), this executes 1 GROUP BY query for all
	 * users.
	 * 
	 * Performance Impact: - Before: 50 separate "SELECT COUNT(*) FROM
	 * MapUserCompany WHERE userId = ?" queries - After: 1 single "SELECT userId,
	 * COUNT(*) FROM MapUserCompany WHERE userId IN (?, ?, ...) GROUP BY userId"
	 * query
	 * 
	 * @param userIds List of user IDs to get company counts for
	 * @return Map of userId -> companyCount
	 */
	private Map<Integer, Long> getCompanyCountsForUsers(List<Integer> userIds) {
		if (userIds.isEmpty()) {
			return Map.of();
		}

		List<Object[]> results = mapRepo.countCompaniesByUserIds(userIds);

		return results.stream().collect(Collectors.toMap(row -> (Integer) row[0], // userId
				row -> (Long) row[1] // company count
		));
	}

	/**
	 * Entity-specific sort validator that only validates fields for MasterUser
	 * entity. Provides security by preventing access to unauthorized fields.
	 */
	private static class EntitySpecificSortValidator implements SortFieldValidator {

		private final SearchableEntity<MasterUser> entity;
		private final UniversalSortFieldValidator universalValidator;

		public EntitySpecificSortValidator(SearchableEntity<MasterUser> entity,
				UniversalSortFieldValidator universalValidator) {
			this.entity = entity;
			this.universalValidator = universalValidator;
		}

		@Override
		public boolean isValidSortField(String field) {
			return universalValidator.isValidSortField(field, entity.getEntityClass());
		}

		@Override
		public Set<String> getValidSortFields() {
			return entity.getSortableFields();
		}
	}

	/**
	 * OPTIMIZED: Convert MasterUser entity to SearchUsersResponseDto using
	 * pre-fetched company count. Eliminates N+1 queries by using batch company
	 * count lookup.
	 * 
	 * @param user         The user entity (with fetch-joined role and audit users)
	 * @param companyCount Pre-fetched company count for this user
	 * @return Optimized DTO with company count information
	 */
	private SearchUsersResponseDto convertToSearchUsersResponseDto(MasterUser user, Long companyCount) {
		SearchUsersResponseDto dto = new SearchUsersResponseDto();

		// Basic user information - use Integer userId (primary key)
		dto.setUserId(user.getId());
		dto.setName(user.getFullName());
		dto.setEmail(user.getEmail());
		dto.setIsBdpEmployee(user.getIsBdpEmployee());
		dto.setUpdatedOn(user.getUpdatedOn());

		// Role information (optimized with fetch join)
		if (user.getAssignedRole() != null) {
			dto.setAssignedRoleName(user.getAssignedRole().getName());
			dto.setAssignedRoleId(user.getAssignedRole().getId());
		}

		// Company count (only field needed for company information)
		dto.setCompanyCount(companyCount);

		// FIXED: User type logic - "New" if companyCount = 0 AND assignedRoleId = null
		boolean hasRole = user.getAssignedRole() != null;
		boolean hasCompany = companyCount > 0;
		dto.setUserType((!hasRole && !hasCompany) ? "New" : "Existing");

		// Set updatedBy using the fetch-joined entity
		final String updatedByEmail = Optional.ofNullable(user.getUpdatedByUser()).map(MasterUser::getEmail)
				.orElse(null);
		dto.setUpdatedBy(updatedByEmail);

		return dto;
	}

	@Override
	public UserCountResponseDto getUserCount(CurrentUserDto user, String type) {

		long count;
		// Handle both old and new type values for backward compatibility
		if ("active".equalsIgnoreCase(type) || "activeOrNoRole".equalsIgnoreCase(type)) {
			// New logic: Count users with active role OR no role
			count = userRepo.count(UserSpecification.hasActiveRoleOrNoRole());
		} else if ("inactive".equalsIgnoreCase(type) || "inactiveRole".equalsIgnoreCase(type)) {
			// Count users with inactive role only
			count = userRepo.count(UserSpecification.hasInactiveRole());
		} else {
			// Fallback for old "active" vs others
			count = userRepo.count(UserSpecification.hasActiveRole());
		}

		return new UserCountResponseDto(count);
	}

	@Override
	public List<RoleDto> getActiveRoles(CurrentUserDto userEmailDto, boolean active) {
		// convert boolean → 1 or 0
		Integer flag = active ? 1 : 0;
		List<Role> roles = roleRepo.findByIsActive(flag);

		// map entity → DTO
		return roles.stream().map(this::toDto).toList();
	}

	private RoleDto toDto(Role role) {
		return RoleDto.builder().roleId(role.getId()).roleName(role.getName()).build();
	}

	@Override
	public List<CompanyTreeNode> searchCompanies(CurrentUserDto user, SearchCompanyRequestDto request) {
		// 1) Fetch flat hierarchy rows
		List<CompanyDtoPOc> flat = companyJDBCRepository.searchHierarchy(request.getSearchText(),
				request.getIsPsaBdp());

		// 2) Map id→node for easy lookup
		Map<Integer, CompanyTreeNode> nodeMap = new HashMap<>();
		for (CompanyDtoPOc dto : flat) {
			CompanyTreeNode node = new CompanyTreeNode();
			node.setChecked(false);
			node.setExpanded(false);
			node.setKey(String.valueOf(dto.getId()));
			node.setData(dto);
			node.setParentId(dto.getParentId());
			nodeMap.put(dto.getId(), node);
		}

		// 3) Build parent→children links
		List<CompanyTreeNode> roots = new ArrayList<>();
		for (CompanyTreeNode node : nodeMap.values()) {
			Integer parentId = node.getParentId();
			if (parentId != null && parentId != 0 && nodeMap.containsKey(parentId)) {
				nodeMap.get(parentId).getChildren().add(node);
			} else {
				roots.add(node);
			}
		}

		return roots;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String updateUserAccessAssignments(CurrentUserDto userEmailDto, String userId,
			AccessAssignmentRequestDto request) {

		Integer uid = Integer.valueOf(userId);
		MasterUser user = userRepo.findById(uid)
				.orElseThrow(() -> new EntityNotFoundException("User not found: " + uid));

		// 1) Update role & flag
		user.setRoleId(request.getRoleId());
		user.setIsBdpEmployee(request.getIsBdpEmployee());
		userRepo.save(user);

		// 2) Bulk‐delete existing assignments
		mapRepo.deleteByUserId(uid);

		// 3) Batch‐create new assignments
		List<MapUserCompany> assigns = request.getAssignedCompanies().stream().map(AssignedCompanyDto::getCompanyCode)
				.map(Integer::valueOf).map(companyId -> {
					MasterCompany mc = masterCompanyRepo.findById(companyId)
							.orElseThrow(() -> new EntityNotFoundException("Company not found: " + companyId));
					return MapUserCompany.builder().userId(uid).companyId(mc.getId()).createdOn(LocalDateTime.now())
							.createdById(userEmailDto.getUserId()) // use real creator
							.build();
				}).toList();

		mapRepo.saveAll(assigns);

		return "User access assignments updated successfully";
	}

	@Override
	public List<SearchUsersResponseDto> searchUserForCopy(CurrentUserDto user, SearchForCopyRequestDto request) {
		String searchText = request.getSearchText();
		if (searchText == null || searchText.isBlank()) {
			return Collections.emptyList();
		}
		String text = searchText.toLowerCase();
		String like = "%" + text + "%";

		// 1) Build spec: fullName LIKE ? OR email LIKE ?
		Specification<MasterUser> spec = (root, query, cb) -> cb.or(cb.like(cb.lower(root.get("fullName")), like),
				cb.like(cb.lower(root.get("email")), like));

		// 2) Sort alphabetically by fullName
		Sort sort = Sort.by("fullName").ascending();

		// 3) Fetch matching users
		List<MasterUser> users = userRepo.findAll(spec, sort);

		// 4) Map to minimal DTO (name + roleConfigId)
		return users.stream().map(u -> {
			SearchUsersResponseDto dto = new SearchUsersResponseDto();
			dto.setName(u.getFullName());
			dto.setAssignedRoleId(u.getRoleId() != null ? u.getRoleId() : null);
			return dto;
		}).toList();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String copyUserAccess(CurrentUserDto CurrentUserDto, CopyAccessRequestDto request) {

		// 1) parse out the numeric IDs
		Integer srcId = request.getSourceUserId();
		Integer targetId = request.getTargetUserId();

		// 2) load both users
		MasterUser src = userRepo.findById(srcId)
				.orElseThrow(() -> new EntityNotFoundException("Source user not found: " + srcId));
		MasterUser target = userRepo.findById(targetId)
				.orElseThrow(() -> new EntityNotFoundException("Target user not found: " + targetId));

		// 3) compare roles
		Integer srcRoleId = src.getRoleId();
		Integer targetRoleId = target.getRoleId();

		// 4) compare company sets
		Set<Integer> srcComps = src.getCompanies().stream().map(MasterCompany::getId).collect(Collectors.toSet());
		Set<Integer> tgtComps = target.getCompanies().stream().map(MasterCompany::getId).collect(Collectors.toSet());

		if ("preview".equalsIgnoreCase(request.getRequestType())) {
			// 5) if nothing would change, short‐circuit
			if (Objects.equals(srcRoleId, targetRoleId) && srcComps.equals(tgtComps)) {
				return "Target user already has the same role and company assignments as source user.";
			} else {
				return "Target User has different role and Companies";
			}
		} else {

			// 7) APPLY: copy role
			target.setRoleId(srcRoleId);
			userRepo.save(target);

			// 8) bulk‐delete old assignments
			mapRepo.deleteByUserId(targetId);

			// 9) batch‐insert new assignments
			List<MapUserCompany> toSave = srcComps.stream().map(compId -> MapUserCompany.builder().userId(targetId)
					.companyId(compId).createdOn(LocalDateTime.now()).createdById(CurrentUserDto.getUserId()).build())
					.toList();
			mapRepo.saveAll(toSave);
			return "Access copied successfully.";
		}
	}

	@Override
	public AssignedCompaniesResponseDto getAssignedCompanies(CurrentUserDto user, String userId) {
	    Integer uid = Integer.valueOf(userId);
	    List<MasterCompany> companies = userRepo.findAssignedCompanies(uid);

	    // Partition: PSA (true) vs Non-PSA (false)
	    Map<Boolean, List<MasterCompany>> partitions = companies.stream()
	        .collect(Collectors.partitioningBy(this::isPsa));

	    List<AssignedCompanyDto> psa = partitions.get(true).stream()
	            .map(this::toDto)
	            .toList();

	    List<AssignedCompanyDto> nonPsa = partitions.get(false).stream()
	            .map(this::toDto)
	            .toList();

	    return AssignedCompaniesResponseDto.builder()
	            .psaCompanies(psa)
	            .nonPsaCompanies(nonPsa)
	            .build();
	}

	/** PSA if psa_flag == 1; treat null/others as non-PSA */
	private boolean isPsa(MasterCompany mc) {
	    Integer flag = mc.getPsaFlag();
	    return flag != null && flag == 1;
	}

	/** Map entity → leaf DTO */
	private AssignedCompanyDto toDto(MasterCompany mc) {
	    return AssignedCompanyDto.builder()
	            .companyCode(mc.getId())
	            .companyName(mc.getCompanyName())
	            .build();
	}



	/**
	 * Two separate caches keyed by "PSA" / "NON-PSA", each holds the full list of
	 * CompanyDtoPOc for that category, refreshed every 30 minutes.
	 */
	private final LoadingCache<String, List<CompanyDtoPOc>> companyCache = Caffeine.newBuilder()
			.refreshAfterWrite(Duration.ofMinutes(30)).build(this::loadCompaniesByType);

	/**
	 * Loads & filters companies by PSA flag on first cache miss or refresh. Assumes
	 * CompanyDtoPOc has a getPsaFlag() → 1 for PSA, 0 for NON-PSA.
	 */
	private List<CompanyDtoPOc> loadCompaniesByType(String type) {
		Integer companyType = "PSA".equalsIgnoreCase(type) ? 1 : 0;
		// fetch all and then filter in‐memory
		return new ArrayList<>(companyJDBCRepository.fetchAllCompanies(companyType));
	}

	@Override
	public CompanyTreeResponseDto buildCompanyTree(CompanyTreeRequestDto request) {
		long t0 = System.currentTimeMillis();

		// 1) Load from cache by type
		String type = request.getType().toUpperCase(Locale.ROOT);
		List<CompanyDtoPOc> all = companyCache.get(type);
		long t1 = System.currentTimeMillis();
		log.info("Loaded {} companies for '{}' in {} ms", all.size(), type, t1 - t0);
		CompanyTreeResponseDto response = new CompanyTreeResponseDto();
		response.setTotalSize(all.size());

		// 2) Prepare DataTableRequest
		DataTableRequest dt = request.getDataTableRequest();
		String searchText = dt.getSearchFilter() != null ? dt.getSearchFilter().getSearchText() : null;

		// 3) Build an id→DTO map for traversal
		Map<Integer, CompanyDtoPOc> dtoMap = all.stream()
				.collect(Collectors.toMap(CompanyDtoPOc::getId, Function.identity()));

		// 4) Determine the candidate roots
		List<CompanyDtoPOc> candidateRoots;
		if (StringUtils.hasText(searchText)) {
			String lower = Optional.ofNullable(searchText).orElse("").toLowerCase(Locale.ROOT);
			Set<Integer> rootIds = new HashSet<>();

			// for each matching node, walk up to its top‐level ancestor
			for (CompanyDtoPOc dto : all) {
				if (String.valueOf(dto.getId()).contains(lower)
						|| dto.getCompanyName().toLowerCase(Locale.ROOT).contains(lower)) {

					CompanyDtoPOc cur = dto;
					while (cur.getParentId() != null && cur.getParentId() != 0) {
						cur = dtoMap.get(cur.getParentId());
						if (cur == null)
							break;
					}
					if (cur != null) {
						rootIds.add(cur.getId());
					}
				}
			}
			candidateRoots = rootIds.stream().map(dtoMap::get).filter(Objects::nonNull).toList();
		} else {
			// “true” roots have parentId==null or 0
			candidateRoots = all.stream().filter(d -> d.getParentId() == null || d.getParentId() == 0).toList();
		}
		long t2 = System.currentTimeMillis();
		log.info("Identified {} candidate roots in {} ms", candidateRoots.size(), t2 - t1);

		// 5) Sort candidateRoots by specified columns or default to name asc
		List<DataTableRequest.Column> cols = dt.getColumns();
		Comparator<CompanyDtoPOc> cmp = Comparator.comparing(CompanyDtoPOc::getCompanyName,
				String.CASE_INSENSITIVE_ORDER);

		// if client provided any sort directives, build a combined comparator
		List<Comparator<CompanyDtoPOc>> directives = cols.stream().filter(c -> StringUtils.hasText(c.getSort()))
				.map(c -> {
					Comparator<CompanyDtoPOc> c0;
					switch (c.getColumnName()) {
					case "companyName":
						c0 = Comparator.comparing(CompanyDtoPOc::getCompanyName, String.CASE_INSENSITIVE_ORDER);
						break;
					case "companyCode":
						c0 = Comparator.comparing(CompanyDtoPOc::getId);
						break;
					default:
						return null;
					}
					return "desc".equalsIgnoreCase(c.getSort()) ? c0.reversed() : c0;
				}).filter(Objects::nonNull).toList();

		if (!directives.isEmpty()) {
			// chain them in order
			cmp = directives.stream().reduce(Comparator::thenComparing).get();
		}
		candidateRoots = new ArrayList<>(candidateRoots);
		candidateRoots.sort(cmp);
		long t3 = System.currentTimeMillis();
		log.info("Sorted {} roots in {} ms", candidateRoots.size(), t3 - t2);

		// 6) Paginate
		int page = dt.getPagination().getPage();
		int size = dt.getPagination().getSize();
		int from = page * size;
		int to = Math.min(from + size, candidateRoots.size());

		List<CompanyDtoPOc> pageRoots = (from >= candidateRoots.size()) ? Collections.emptyList()
				: candidateRoots.subList(from, to);

		long t4 = System.currentTimeMillis();
		log.info("Paginated roots [{}..{}) in {} ms", from, to, t4 - t3);

		// 7) Build full tree for **all** nodes once
		Map<Integer, CompanyTreeNode> nodeMap = new HashMap<>();
		for (CompanyDtoPOc dto : all) {
			CompanyTreeNode node = new CompanyTreeNode();
			node.setKey(String.valueOf(dto.getId()));
			node.setData(dto);
			node.setParentId(dto.getParentId());
			nodeMap.put(dto.getId(), node);
		}
		// link children
		for (CompanyTreeNode node : nodeMap.values()) {
			Integer pid = node.getParentId();
			if (pid != null && pid != 0 && nodeMap.containsKey(pid)) {
				nodeMap.get(pid).getChildren().add(node);
			}
		}

		// 8) Extract only the paged‐roots (with their full subtrees)
		List<CompanyTreeNode> result = pageRoots.stream().map(dto -> nodeMap.get(dto.getId())).filter(Objects::nonNull)
				.toList();

		long t5 = System.currentTimeMillis();
		log.info("Built {} subtrees in {} ms", result.size(), t5 - t4);
		response.setCompanyTreeNodes(result);
		return response;
	}

	/**
	 * Get paginated companies for a specific user using Universal Search Framework.
	 * Supports searching by company ID and name with page size of 500.
	 * 
	 * @param user Current authenticated user for authorization checks
	 * @param userId Target user ID to get companies for
	 * @param request Search request with pagination and search criteria
	 * @return Paginated list of user companies sorted by company name
	 */
	@Override
	@ReadOnlyDataSource("Get user companies with Universal Search Framework")
	@Transactional(readOnly = true, timeout = 30)
	public PaginationResponse<UserCompanyDto> getUserCompanies(CurrentUserDto user, Integer userId, SearchUsersRequestDto request) {
		
		// 1. Authorization check - ensure user can access this data
		// You can add specific authorization logic here based on your requirements
		if (user == null) {
			throw new CustomException(ErrorCode.UNAUTHORIZED, "User authentication required");
		}
		
		// 2. Register entity for sort validation
		universalSortValidator.registerEntity(userCompanySearchConfig);
		
		// 3. Get DataTableRequest and set page size to 500
		DataTableRequest dt = request.getDataTableRequest();
		dt.getPagination().setSize(500); // Fixed page size as requested
		
		// 4. Add userId filter to search only this user's companies
		DataTableRequest.Column userIdFilter = new DataTableRequest.Column();
		userIdFilter.setColumnName("userId");
		userIdFilter.setFilter("eq:" + userId.toString());
		dt.getColumns().add(userIdFilter);
		
		// 5. Set entity-specific sort validator for security
		dt.setSortFieldValidator(new UserCompanyEntitySpecificSortValidator(userCompanySearchConfig, universalSortValidator));
		
		// 6. Execute dynamic search using Universal Search Framework
		Page<MapUserCompany> page = queryBuilder.findWithDynamicSearch(
				(SearchableEntity<MapUserCompany>) userCompanySearchConfig, dt);
		
		// 7. Convert to response DTOs
		List<UserCompanyDto> content = page.getContent().stream()
				.map(this::convertToUserCompanyDto)
				.toList();
		
		// 8. Build paginated response
		PaginationResponse<UserCompanyDto> response = new PaginationResponse<>();
		response.setPage(page.getNumber());
		response.setSize(page.getSize());
		response.setTotalElements(page.getTotalElements());
		response.setTotalPages(page.getTotalPages());
		response.setLast(page.isLast());
		response.setContent(content);
		
		return response;
	}
	
	/**
	 * Convert MapUserCompany entity to UserCompanyDto with enhanced field mapping.
	 * 
	 * @param mapUserCompany The entity with fetched company information
	 * @return UserCompanyDto with company ID, name, and code
	 */
	private UserCompanyDto convertToUserCompanyDto(MapUserCompany mapUserCompany) {
		if (mapUserCompany == null) {
			return null;
		}
		
		var company = mapUserCompany.getCompany();
		return UserCompanyDto.builder()
				.companyId(mapUserCompany.getCompanyId())
				.companyName(company != null ? company.getCompanyName() : "Unknown")
				.build();
	}

	/**
	 * Entity-specific sort validator for MapUserCompany that only validates fields for this entity
	 */
	private static class UserCompanyEntitySpecificSortValidator implements SortFieldValidator {
		
		private final SearchableEntity<MapUserCompany> entity;
		private final UniversalSortFieldValidator universalValidator;

		public UserCompanyEntitySpecificSortValidator(SearchableEntity<MapUserCompany> entity,
				UniversalSortFieldValidator universalValidator) {
			this.entity = entity;
			this.universalValidator = universalValidator;
		}

		@Override
		public boolean isValidSortField(String field) {
			return universalValidator.isValidSortField(field, entity.getEntityClass());
		}

		@Override
		public Set<String> getValidSortFields() {
			return entity.getSortableFields();
		}
	}
}
