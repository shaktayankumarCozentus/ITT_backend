package com.itt.service.service.impl;

import com.itt.service.annotation.ReadOnlyDataSource;
import com.itt.service.annotation.WriteDataSource;
import com.itt.service.config.search.RoleSearchConfig;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.role.*;
import com.itt.service.dto.role.RoleWithPrivilegesDto.ConfigDto;
import com.itt.service.dto.role.RoleWithPrivilegesDto.LandingPageDto;
import com.itt.service.entity.*;
import com.itt.service.fw.search.DynamicSearchQueryBuilder;
import com.itt.service.fw.search.SearchableEntity;
import com.itt.service.fw.search.UniversalSortFieldValidator;
import com.itt.service.repository.*;
import com.itt.service.service.RoleManagementService;
import com.itt.service.validator.SortFieldValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleManagementServiceImpl implements RoleManagementService {

	  private final RoleRepository roleRepo;
	    private final MapRoleCategoryFeaturePrivilegeSkinRepository rolePrivSkinRepo;
	    private final MapCategoryFeaturePrivilegeRepository mapCatFeatPrivRepo;
	    private final MasterConfigRepository masterConfigRepo;
	    private final RoleSearchConfig roleSearchConfig;
	    private final DynamicSearchQueryBuilder queryBuilder;
	    private final UniversalSortFieldValidator universalSortValidator;

	    public RoleManagementServiceImpl(
	            RoleRepository roleRepo,
	            MapRoleCategoryFeaturePrivilegeSkinRepository rolePrivSkinRepo,
	            MapCategoryFeaturePrivilegeRepository mapCatFeatPrivRepo,
	            MasterConfigRepository masterConfigRepo,
	            RoleSearchConfig roleSearchConfig,
	            DynamicSearchQueryBuilder queryBuilder,
	            @Qualifier("universalSortFieldValidator") UniversalSortFieldValidator universalSortValidator) {
	        this.roleRepo = roleRepo;
	        this.rolePrivSkinRepo = rolePrivSkinRepo;
	        this.mapCatFeatPrivRepo = mapCatFeatPrivRepo;
	        this.masterConfigRepo = masterConfigRepo;
	        this.roleSearchConfig = roleSearchConfig;
	        this.queryBuilder = queryBuilder;
	        this.universalSortValidator = universalSortValidator;
	    }

        /**
         * Universal search method using the framework
         */
        @ReadOnlyDataSource("Universal search with automatic query generation")
        @Transactional(readOnly = true, timeout = 30)
        public PaginationResponse<Role> search(DataTableRequest request) {
                // Register entity for sort validation
                universalSortValidator.registerEntity(roleSearchConfig);
                
                // Set entity-specific sort validator for security
                request.setSortFieldValidator(new EntitySpecificSortValidator<>(roleSearchConfig, universalSortValidator));
                
                // Execute dynamic search
                Page<Role> page = queryBuilder.findWithDynamicSearch(roleSearchConfig, request);
                
                // Convert to response format
                return new PaginationResponse<>(page);
        }

        /**
         * Entity-specific sort validator that only validates fields for this entity
         */
        private static class EntitySpecificSortValidator<T> implements SortFieldValidator {
                
                private final SearchableEntity<T> entity;
                private final UniversalSortFieldValidator universalValidator;
                
                public EntitySpecificSortValidator(SearchableEntity<T> entity, UniversalSortFieldValidator universalValidator) {
                        this.entity = entity;
                        this.universalValidator = universalValidator;
                }
                
                @Override
                public boolean isValidSortField(String field) {
                        return universalValidator.isValidSortField(field, entity.getEntityClass());
                }
                
                @Override
                public java.util.Set<String> getValidSortFields() {
                        return entity.getSortableFields();
                }
        }

        @Override
        @ReadOnlyDataSource("Get privilege hierarchy for role management")
        @Transactional(readOnly = true)
        @Cacheable(value = "privilegeHierarchy", key = "#roleId.orElse('ALL')")
        public List<FeaturePrivilegeDTO> getPrivilegeHierarchy(Optional<Long> roleId) {
                // PERFORMANCE OPTIMIZATION: Use fetch join for single role privilege loading
                final Set<Long> selectedIds = roleId
                                .map(id -> rolePrivSkinRepo.findByRoleIdWithFetch(id.intValue())
                                                .stream()  // Changed from parallelStream() - safer for Hibernate entities
                                                .filter(Objects::nonNull)
                                                .filter(m -> m.getMapCategoryFeaturePrivilege() != null)
                                                .map(m -> m.getMapCategoryFeaturePrivilege().getId().longValue())
                                                .collect(Collectors.toUnmodifiableSet()))
                                .orElse(Set.of());
                
                // PERFORMANCE OPTIMIZATION: Cache the privilege rows since they don't change often
                final var rows = getCachedPrivilegeRows();
                
                // Optimized: Group by category first to reduce nested iterations - O(n) instead of O(n²)
                final Map<Long, List<MapCategoryFeaturePrivilege>> categoryGroups = rows
                                .parallelStream()
                                .filter(Objects::nonNull)
                                .filter(row -> row.getFeatureCategory() != null)
                                .collect(Collectors.groupingBy(
                                                row -> row.getFeatureCategory().getId().longValue(),
                                                LinkedHashMap::new,
                                                Collectors.toList()));
                
                return categoryGroups.entrySet()
                                .parallelStream()
                                .map(entry -> buildCategoryDto(entry.getKey(), entry.getValue(), selectedIds))
                                .filter(Objects::nonNull)
                                .toList();
        }
        
        /**
         * PERFORMANCE OPTIMIZATION: Cache privilege rows since they're accessed frequently
         * and don't change often during application runtime
         */
        @Cacheable(value = "privilegeRows", key = "'ALL'")
        private List<MapCategoryFeaturePrivilege> getCachedPrivilegeRows() {
                return mapCatFeatPrivRepo.findAllFull();
        }
        
        /**
         * Optimized helper method to build category DTO with improved time complexity
         * Time Complexity: O(n) instead of O(n²) nested loops
         */
        private FeaturePrivilegeDTO buildCategoryDto(Long categoryId, 
                        List<MapCategoryFeaturePrivilege> categoryRows, 
                        Set<Long> selectedIds) {
                
                if (categoryRows.isEmpty()) {
                        return null;
                }
                
                final var firstRow = categoryRows.get(0);
                final var category = firstRow.getFeatureCategory();
                
                // Group by feature ID for O(1) lookup instead of nested iteration
                final Map<Long, List<MapCategoryFeaturePrivilege>> featureGroups = categoryRows
                                .parallelStream()
                                .filter(row -> row.getFeature() != null)
                                .collect(Collectors.groupingBy(
                                                row -> row.getFeature().getId().longValue(),
                                                LinkedHashMap::new,
                                                Collectors.toList()));
                
                final var features = featureGroups.entrySet()
                                .parallelStream()
                                .map(featureEntry -> buildFeatureDto(featureEntry.getKey(), 
                                                featureEntry.getValue(), selectedIds))
                                .filter(Objects::nonNull)
                                .toList();
                
                return new FeaturePrivilegeDTO(
                                categoryId,
                                category.getName(),
                                category.getKeyCode(),
                                features);
        }
        
        /**
         * Optimized helper method to build feature DTO
         * Time Complexity: O(n) linear processing
         */
        private FeaturePrivilegeDTO.FeatureDTO buildFeatureDto(Long featureId,
                        List<MapCategoryFeaturePrivilege> featureRows,
                        Set<Long> selectedIds) {
                
                if (featureRows.isEmpty()) {
                        return null;
                }
                
                final var firstRow = featureRows.get(0);
                final var feature = firstRow.getFeature();
                
                final var privileges = featureRows
                                .parallelStream()
                                .filter(row -> row.getPrivilege() != null)
                                .map(row -> new FeaturePrivilegeDTO.PrivilegeDTO(
                                                row.getPrivilege().getId().longValue(),
                                                row.getPrivilege().getName(),
                                                row.getPrivilege().getKeyCode(),
                                                row.getId().longValue(),
                                                selectedIds.contains(row.getId().longValue())))
                                .toList();
                
                return new FeaturePrivilegeDTO.FeatureDTO(
                                featureId,
                                feature.getName(),
                                feature.getKeyCode(),
                                privileges);
        }

        @Override
        @ReadOnlyDataSource("Get paginated roles with privileges and configurations using Universal Search Framework")
        @Transactional(readOnly = true)
        public PaginationResponse<RoleWithPrivilegesDto> getPaginatedRolesWithFullPrivilegesAndConfigs(
                        DataTableRequest req) {
                
                // Use the Universal Search Framework for search, sort, and pagination
                // The framework automatically includes fetch joins for roleTypeConfig, landingPageConfig, 
                // createdByUser, and updatedByUser as defined in RoleSearchConfig
                final var rolePages = search(req);
                final var roles = rolePages.getContent();
                
                if (roles.isEmpty()) {
                        return new PaginationResponse<>(
                                        rolePages.getPage(), rolePages.getSize(), rolePages.getTotalElements(),
                                        rolePages.getTotalPages(), rolePages.getLast(), List.of());
                }
                
                // PERFORMANCE OPTIMIZATION: Load all privilege hierarchy data ONCE instead of per role
                final var allPrivilegeRows = getCachedPrivilegeRows();
                
                // PERFORMANCE OPTIMIZATION: Batch load role mappings for all roles at once
                final var roleIds = roles.stream()
                                .map(Role::getId)
                                .toList();
                
                // ✅ Now using fetch joins to prevent N+1 queries for all related entities
                final var roleMappings = rolePrivSkinRepo.findByRole_IdInWithFetch(roleIds);
                
                // Group mappings by role ID for efficient lookup
                final Map<Integer, List<MapRoleCategoryFeaturePrivilegeSkin>> mappingsByRole = roleMappings
                                .stream()
                                .collect(Collectors.groupingBy(
                                                m -> m.getRole().getId(),
                                                LinkedHashMap::new,
                                                Collectors.toList()));
                
                // Convert Role entities to RoleWithPrivilegesDto with optimized data access
                // User details are already loaded via fetch joins from the search framework
                final var content = roles.stream()
                                .map(role -> convertToRoleWithPrivilegesDtoOptimized(role, 
                                                mappingsByRole.getOrDefault(role.getId(), List.of()),
                                                allPrivilegeRows))
                                .filter(Objects::nonNull)
                                .toList();

                return new PaginationResponse<>(
                                rolePages.getPage(),
                                rolePages.getSize(),
                                rolePages.getTotalElements(),
                                rolePages.getTotalPages(),
                                rolePages.getLast(),
                                content);
        }

        /**
         * OPTIMIZED: Convert Role entity to RoleWithPrivilegesDto using pre-loaded data
         * Eliminates N+1 queries by using fetch joins from Universal Search Framework
         */
        private RoleWithPrivilegesDto convertToRoleWithPrivilegesDtoOptimized(
                        Role role,
                        List<MapRoleCategoryFeaturePrivilegeSkin> roleMappings,
                        List<MapCategoryFeaturePrivilege> allPrivilegeRows) {
                
                // Build privilege hierarchy for this specific role using pre-loaded data
                final Set<Long> selectedPrivilegeIds = roleMappings.stream()
                                .filter(m -> m.getMapCategoryFeaturePrivilege() != null)
                                .map(m -> m.getMapCategoryFeaturePrivilege().getId().longValue())
                                .collect(Collectors.toSet());
                
                // Use pre-loaded data instead of querying again
                final var privilegeHierarchy = buildOptimizedTree(allPrivilegeRows, selectedPrivilegeIds);
                
                // Build skin configurations - skin mappings have NON-NULL skinConfig and NULL mapCategoryFeaturePrivilege
                final var skinConfigs = roleMappings.stream()
                                .filter(m -> m.getSkinConfig() != null && m.getMapCategoryFeaturePrivilege() == null)
                                .map(m -> new ConfigDto(
                                                m.getSkinConfig().getId().longValue(),
                                                m.getSkinConfig().getKeyCode(),
                                                m.getSkinConfig().getName()))
                                .distinct() // Remove duplicates if any
                                .toList();
                
                // Build role type and landing page DTOs
                final var roleType = Optional.ofNullable(role.getRoleTypeConfig())
                                .map(rt -> new RoleWithPrivilegesDto.ConfigDto(
                                                rt.getId().longValue(),
                                                rt.getKeyCode(),
                                                rt.getName()))
                                .orElse(null);

                final var landingPage = Optional.ofNullable(role.getLandingPageConfig())
                                .map(lp -> new RoleWithPrivilegesDto.LandingPageDto(
                                                lp.getId().longValue(),
                                                lp.getKeyCode(),
                                                lp.getName()))
                                .orElse(null);

                // Get user emails from the fetch-joined user entities (already loaded by framework)
                final String createdByEmail = Optional.ofNullable(role.getCreatedByUser())
                                .map(MasterUser::getEmail)
                                .orElse(null);
                final String updatedByEmail = Optional.ofNullable(role.getUpdatedByUser())
                                .map(MasterUser::getEmail)
                                .orElse(null);

                return RoleWithPrivilegesDto.builder()
                                .id(role.getId().longValue())
                                .name(role.getName())
                                .description(role.getDescription())
                                .isActive(role.getIsActive() == 1)
                                .roleType(roleType)
                                .landingPage(landingPage)
                                .createdOn(role.getCreatedOn())
                                .createdBy(createdByEmail) // Use email from fetch-joined entity
                                .updatedOn(role.getUpdatedOn())
                                .updatedBy(updatedByEmail) // Use email from fetch-joined entity
                                .privilegeHierarchy(privilegeHierarchy)
                                .skinConfigs(skinConfigs)
                                .build();
        }

        /**
         * Optimized tree builder with improved time complexity
         * Time Complexity: O(n) instead of O(n²) - uses groupBy operations instead of nested loops
         */
        private List<FeaturePrivilegeDTO> buildOptimizedTree(
                        List<MapCategoryFeaturePrivilege> rows,
                        Set<Long> selectedIds) {
                
                if (rows.isEmpty()) {
                        return List.of();
                }
                
                // Optimized: Group by category first to avoid nested searching - O(n) vs O(n²)
                return rows.stream()
                                .filter(Objects::nonNull)
                                .filter(row -> row.getFeatureCategory() != null)
                                .collect(Collectors.groupingBy(
                                                row -> row.getFeatureCategory().getId().longValue(),
                                                LinkedHashMap::new,
                                                Collectors.toList()))
                                .entrySet()
                                .stream()
                                .map(entry -> buildCategoryDto(entry.getKey(), entry.getValue(), selectedIds))
                                .filter(Objects::nonNull)
                                .toList();
        }

        @Override
        @ReadOnlyDataSource("Get available landing pages for role configuration")
        @Transactional(readOnly = true)
        public List<LandingPageDto> getLandingPages() {
                // Optimized: Use sequential processing for safer Hibernate entity handling
                return masterConfigRepo.findByConfigType("LANDING_PAGE")
                                .stream()  // Changed from parallelStream() - safer for Hibernate entities
                                .filter(Objects::nonNull)
                                .map(cfg -> new LandingPageDto(
                                                cfg.getId().longValue(),
                                                cfg.getKeyCode(),
                                                cfg.getName()))
                               .toList();
        }

        @Override
        @ReadOnlyDataSource("Get available skin groups for role configuration")
        @Transactional(readOnly = true)
        @Cacheable(value = "skinGroups", key = "'all'")
        public List<SkinGroupDto> getSkinGroups() {
                // PERFORMANCE OPTIMIZATION: Batch fetch both config types in single query
                final var configs = masterConfigRepo.findByConfigTypes(List.of("SKIN", "ROLE_TYPE"));
                
                // Partition configurations by type for efficient processing
                final Map<String, List<MasterConfig>> configsByType = configs
                                .stream()  // Changed from parallelStream() - safer for Hibernate entities
                                .filter(Objects::nonNull)
                                .collect(Collectors.groupingBy(MasterConfig::getConfigType));
                
                final var skins = configsByType.getOrDefault("SKIN", List.of());
                final var roleTypes = configsByType.getOrDefault("ROLE_TYPE", List.of());
                
                // Create a map of role type keys to full role type DTOs
                final Map<String, SkinGroupDto.RoleTypeDto> roleTypeMap = roleTypes
                                .stream()  // Changed from parallelStream() - safer for Hibernate entities
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(
                                                MasterConfig::getKeyCode,
                                                cfg -> new SkinGroupDto.RoleTypeDto(
                                                                cfg.getId().longValue(),
                                                                cfg.getKeyCode(),
                                                                cfg.getName()),
                                                (existing, replacement) -> existing,
                                                LinkedHashMap::new));
                
                // Optimized: Sequential processing with null safety and modern collectors  
                final Map<String, List<SkinDto>> grouped = skins
                                .stream()  // Changed from parallelStream() - safer for Hibernate entities
                                .filter(Objects::nonNull)
                                .filter(cfg -> cfg.getStringValue() != null)
                                .collect(Collectors.groupingBy(
                                                MasterConfig::getStringValue,
                                                LinkedHashMap::new,
                                                Collectors.mapping(cfg -> new SkinDto(
                                                                cfg.getId().longValue(),
                                                                cfg.getKeyCode(),
                                                                cfg.getName()),
                                                                Collectors.toUnmodifiableList())));
                                                                
                return grouped.entrySet()
                                .stream()  // Changed from parallelStream() - safer for Hibernate entities
                                .map(entry -> {
                                        final var roleTypeDto = roleTypeMap.get(entry.getKey());
                                        return new SkinGroupDto(roleTypeDto, entry.getValue());
                                })
                                .filter(skinGroup -> skinGroup.getRoleType() != null) // Only include groups with valid role types
                               .toList();
        }
        
        @Override
        @WriteDataSource("Save or update role with full privilege and configuration data")
        @Transactional(rollbackFor = Exception.class)
        public RoleDto saveOrUpdateFull(SaveRoleRequest req) {
                // Optimized: Use switch expressions (Java 17+) and modern validation
                final var role = switch (Optional.ofNullable(req.getId()).isPresent() ? "UPDATE" : "CREATE") {
                        case "UPDATE" -> roleRepo.findById(req.getId().intValue())
                                        .orElseThrow(() -> new NoSuchElementException(
                                                        "Role not found with ID: " + req.getId()));
                        case "CREATE" -> Role.builder()
                                        .createdOn(LocalDateTime.now())
                                        .createdById(req.getCreatedById()) // Use actual user ID from request
                                        .build();
                        default -> throw new IllegalStateException("Unexpected operation type");
                };

                // Optimized: Method chaining with null-safe operations
                updateRoleFields(role, req);

                // Save role
                final var savedRole = roleRepo.save(role);

                // Optimized: Batch operations for better performance
                updateRolePrivilegesAndSkins(savedRole, req);

                return buildRoleDto(savedRole);
        }
        
        /**
         * Optimized helper method to update role fields
         */
        private void updateRoleFields(Role role, SaveRoleRequest req) {
                role.setName(req.getName());
                role.setDescription(req.getDescription());
                role.setIsActive(Boolean.TRUE.equals(req.getIsActive()) ? 1 : 0);
                role.setRoleTypeConfig(Optional.ofNullable(req.getRoleTypeConfigId())
                                .map(id -> MasterConfig.builder().id(id.intValue()).build())
                                .orElse(null));
                role.setLandingPageConfig(Boolean.TRUE.equals(req.getCustomLanding()) 
                                && req.getLandingPageConfigId() != null
                                ? MasterConfig.builder().id(req.getLandingPageConfigId().intValue()).build()
                                : null);
                role.setUpdatedOn(LocalDateTime.now());
                role.setUpdatedById(req.getUpdatedById()); // Use actual user ID from request
        }
        
        /**
         * Optimized batch update for role privileges and skins
         * Time Complexity: O(n) with batch operations instead of individual saves
         */
        private void updateRolePrivilegesAndSkins(Role savedRole, SaveRoleRequest req) {
                // Clear existing mappings
                rolePrivSkinRepo.deleteByRole_Id(savedRole.getId());
                
                final var now = LocalDateTime.now();
                final var roleRef = Role.builder().id(savedRole.getId()).build();
                final var mappingsToSave = new ArrayList<MapRoleCategoryFeaturePrivilegeSkin>();
                
                // Optimized: Batch collect all mappings then save in one operation
                Optional.ofNullable(req.getPrivilegeIds())
                                .filter(ids -> !ids.isEmpty())
                                .ifPresent(privilegeIds -> {
                                    var privilegeMappings = privilegeIds.stream()
                                                    .filter(Objects::nonNull)
                                                    .map(privilegeId -> MapRoleCategoryFeaturePrivilegeSkin.builder()
                                                                    .role(roleRef)
                                                                    .mapCategoryFeaturePrivilege(MapCategoryFeaturePrivilege.builder()
                                                                                    .id(privilegeId.intValue()).build())
                                                                    .skinConfig(null)
                                                                    .createdOn(now)
                                                                    .createdById(req.getCreatedById()) // Use actual user ID from request
                                                                    .build())
                                                    .toList();
                                    mappingsToSave.addAll(privilegeMappings);
                                });
                
                Optional.ofNullable(req.getSkinConfigIds())
                                .filter(ids -> !ids.isEmpty())
                                .ifPresent(skinIds -> {
                                    var skinMappings = skinIds.stream()
                                                    .filter(Objects::nonNull)
                                                    .map(skinId -> MapRoleCategoryFeaturePrivilegeSkin.builder()
                                                                    .role(roleRef)
                                                                    .mapCategoryFeaturePrivilege(null)
                                                                    .skinConfig(MasterConfig.builder()
                                                                                    .id(skinId.intValue()).build())
                                                                    .createdOn(now)
                                                                    .createdById(req.getCreatedById()) // Use actual user ID from request
                                                                    .build())
                                                    .toList();
                                    mappingsToSave.addAll(skinMappings);
                                });
                
                // OPTIMIZED BATCH SAVE - Uses enhanced MySQL batch configuration
                if (!mappingsToSave.isEmpty()) {
                        rolePrivSkinRepo.saveAll(mappingsToSave);
                }
        }
        
        /**
         * Helper method to build RoleDto from Role entity with fetch-joined user details
         */
        private RoleDto buildRoleDto(Role role) {
                // Get user emails from the fetch-joined user entities
                String createdByEmail = Optional.ofNullable(role.getCreatedByUser())
                                .map(MasterUser::getEmail)
                                .orElse(null);
                String updatedByEmail = Optional.ofNullable(role.getUpdatedByUser())
                                .map(MasterUser::getEmail)
                                .orElse(null);
                
                return new RoleDto(
                                role.getId().longValue(),
                                role.getName(),
                                role.getDescription(),
                                role.getIsActive() == 1,
                                Optional.ofNullable(role.getRoleTypeConfig())
                                                .map(config -> config.getId().longValue())
                                                .orElse(null),
                                Optional.ofNullable(role.getLandingPageConfig())
                                                .map(config -> config.getId().longValue())
                                                .orElse(null),
                                role.getCreatedOn(),
                                createdByEmail, // Use email from fetch-joined entity
                                role.getUpdatedOn(),
                                updatedByEmail); // Use email from fetch-joined entity
        }
        
}
