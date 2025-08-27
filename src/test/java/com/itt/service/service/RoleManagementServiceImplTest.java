package com.itt.service.service;

import com.itt.service.config.search.RoleSearchConfig;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.role.*;
import com.itt.service.dto.role.RoleWithPrivilegesDto.LandingPageDto;
import com.itt.service.dto.role.FeaturePrivilegeDTO;
import com.itt.service.dto.role.SaveRoleRequest;
import com.itt.service.dto.role.RoleDto;
import com.itt.service.dto.role.SkinGroupDto;
import com.itt.service.entity.*;
import com.itt.service.fw.search.DynamicSearchQueryBuilder;
import com.itt.service.fw.search.UniversalSortFieldValidator;
import com.itt.service.repository.*;
import com.itt.service.service.impl.RoleManagementServiceImpl;
import com.itt.service.shared.builders.ServiceTestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleManagementService - Business Logic Tests")
class RoleManagementServiceImplTest {

    @Mock private RoleRepository roleRepo;
    @Mock private MapRoleCategoryFeaturePrivilegeSkinRepository rolePrivSkinRepo;
    @Mock private MapCategoryFeaturePrivilegeRepository mapCatFeatPrivRepo;
    @Mock private MasterConfigRepository masterConfigRepo;
    @Mock private RoleSearchConfig roleSearchConfig;
    @Mock private DynamicSearchQueryBuilder queryBuilder;
    @Mock private UniversalSortFieldValidator universalSortValidator;

    @InjectMocks
    private RoleManagementServiceImpl roleManagementService;

    @Nested
    @DisplayName("Get Privilege Hierarchy Tests")
    class GetPrivilegeHierarchyTests {

        @Test
        @DisplayName("Should return global privilege hierarchy when no role ID provided")
        void shouldReturnGlobalPrivilegeHierarchyWhenNoRoleId() {
            // Given
            List<MapCategoryFeaturePrivilege> mockMappings = createMockPrivilegeMappings();
            when(mapCatFeatPrivRepo.findAllFull()).thenReturn(mockMappings);

            // When
            List<FeaturePrivilegeDTO> result = roleManagementService.getPrivilegeHierarchy(Optional.empty());

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2); // USER_MANAGEMENT and ROLE_MANAGEMENT categories
            
            // Verify USER_MANAGEMENT category
            FeaturePrivilegeDTO userMgmt = findCategoryByName(result, "USER_MANAGEMENT");
            assertThat(userMgmt).isNotNull();
            assertThat(userMgmt.getFeatures()).hasSize(1);
            assertThat(userMgmt.getFeatures().get(0).getPrivileges()).hasSize(1);
            assertThat(userMgmt.getFeatures().get(0).getPrivileges().get(0).getIsSelected()).isFalse();

            verify(mapCatFeatPrivRepo).findAllFull();
            verifyNoInteractions(rolePrivSkinRepo);
        }

        @Test
        @DisplayName("Should return role-specific privilege hierarchy when role ID provided")
        void shouldReturnRoleSpecificPrivilegeHierarchyWhenRoleIdProvided() {
            // Given
            Long roleId = 1L;
            List<MapCategoryFeaturePrivilege> mockMappings = createMockPrivilegeMappings();
            List<MapRoleCategoryFeaturePrivilegeSkin> roleMappings = createMockRoleMappings();
            
            when(mapCatFeatPrivRepo.findAllFull()).thenReturn(mockMappings);
            when(rolePrivSkinRepo.findByRoleIdWithFetch(roleId.intValue())).thenReturn(roleMappings);

            // When
            List<FeaturePrivilegeDTO> result = roleManagementService.getPrivilegeHierarchy(Optional.of(roleId));

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            
            // Verify that selected privileges are marked as selected
            FeaturePrivilegeDTO userMgmt = findCategoryByName(result, "USER_MANAGEMENT");
            assertThat(userMgmt.getFeatures().get(0).getPrivileges().get(0).getIsSelected()).isTrue();

            verify(mapCatFeatPrivRepo).findAllFull();
            verify(rolePrivSkinRepo).findByRoleIdWithFetch(roleId.intValue());
        }

        @Test
        @DisplayName("Should handle empty privilege mappings")
        void shouldHandleEmptyPrivilegeMappings() {
            // Given
            when(mapCatFeatPrivRepo.findAllFull()).thenReturn(Collections.emptyList());

            // When
            List<FeaturePrivilegeDTO> result = roleManagementService.getPrivilegeHierarchy(Optional.empty());

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(mapCatFeatPrivRepo).findAllFull();
        }
    }

    @Nested
    @DisplayName("Get Paginated Roles Tests")
    class GetPaginatedRolesTests {

        @Test
        @DisplayName("Should return paginated roles with privileges and configurations")
        void shouldReturnPaginatedRolesWithPrivilegesAndConfigurations() {
            // Given
            DataTableRequest request = new DataTableRequest();
            // Set pagination using nested pagination object
            DataTableRequest.Pagination pagination = new DataTableRequest.Pagination();
            pagination.setPage(0);
            pagination.setSize(10);
            request.setPagination(pagination);
            
            List<Role> mockRoles = createMockRoles();
            Page<Role> mockPage = new PageImpl<>(mockRoles);
            List<MapRoleCategoryFeaturePrivilegeSkin> mockMappings = createMockRoleMappings();
            List<MapCategoryFeaturePrivilege> mockPrivileges = createMockPrivilegeMappings();

            // Mock the dynamic search query builder
            when(queryBuilder.findWithDynamicSearch(eq(roleSearchConfig), eq(request))).thenReturn(mockPage);
            when(rolePrivSkinRepo.findByRole_IdInWithFetch(anyList())).thenReturn(mockMappings);
            when(mapCatFeatPrivRepo.findAllFull()).thenReturn(mockPrivileges);

            // When
            PaginationResponse<RoleWithPrivilegesDto> result = 
                roleManagementService.getPaginatedRolesWithFullPrivilegesAndConfigs(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2); // Mock page size from mocked Page

            // Verify role data mapping
            RoleWithPrivilegesDto firstRole = result.getContent().get(0);
            assertThat(firstRole.getName()).isEqualTo("ADMIN");
            assertThat(firstRole.getIsActive()).isTrue();
            assertThat(firstRole.getPrivilegeHierarchy()).isNotNull();

            verify(queryBuilder).findWithDynamicSearch(eq(roleSearchConfig), eq(request));
            verify(rolePrivSkinRepo).findByRole_IdInWithFetch(anyList());
            verify(mapCatFeatPrivRepo).findAllFull();
        }

        @Test
        @DisplayName("Should handle empty role page")
        void shouldHandleEmptyRolePage() {
            // Given
            DataTableRequest request = new DataTableRequest();
            Page<Role> emptyPage = new PageImpl<>(Collections.emptyList());

            // Mock the dynamic search query builder
            when(queryBuilder.findWithDynamicSearch(eq(roleSearchConfig), eq(request))).thenReturn(emptyPage);
            // Note: When roles list is empty, the service returns early without calling other repos

            // When
            PaginationResponse<RoleWithPrivilegesDto> result = 
                roleManagementService.getPaginatedRolesWithFullPrivilegesAndConfigs(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(queryBuilder).findWithDynamicSearch(eq(roleSearchConfig), eq(request));
            // Other repos are not called when roles list is empty
        }
    }

    @Nested
    @DisplayName("Get Landing Pages Tests")
    class GetLandingPagesTests {

        @Test
        @DisplayName("Should return available landing pages")
        void shouldReturnAvailableLandingPages() {
            // Given
            List<MasterConfig> landingPageConfigs = Arrays.asList(
                createMasterConfig(1L, "DASHBOARD", "Dashboard"),
                createMasterConfig(2L, "REPORTS", "Reports"),
                createMasterConfig(3L, "SETTINGS", "Settings")
            );
            when(masterConfigRepo.findByConfigType("LANDING_PAGE")).thenReturn(landingPageConfigs);

            // When
            List<LandingPageDto> result = roleManagementService.getLandingPages();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            
            LandingPageDto dashboard = result.get(0);
            assertThat(dashboard.getId()).isEqualTo(1L);
            assertThat(dashboard.getKey()).isEqualTo("DASHBOARD");
            assertThat(dashboard.getName()).isEqualTo("Dashboard");

            verify(masterConfigRepo).findByConfigType("LANDING_PAGE");
        }

        @Test
        @DisplayName("Should handle empty landing pages")
        void shouldHandleEmptyLandingPages() {
            // Given
            when(masterConfigRepo.findByConfigType("LANDING_PAGE")).thenReturn(Collections.emptyList());

            // When
            List<LandingPageDto> result = roleManagementService.getLandingPages();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(masterConfigRepo).findByConfigType("LANDING_PAGE");
        }
    }

    @Nested
    @DisplayName("Get Skin Groups Tests")
    class GetSkinGroupsTests {

        @Test
        @DisplayName("Should return skin groups grouped by role type")
        void shouldReturnSkinGroupsGroupedByRoleType() {
            // Given - Create combined list for the findByConfigTypes call
            List<MasterConfig> allConfigs = new ArrayList<>();
            // Skin configs
            allConfigs.addAll(Arrays.asList(
                createSkinConfig(1L, "ADMIN_THEME", "Admin Theme", "ADMIN"),
                createSkinConfig(2L, "USER_THEME", "User Theme", "USER"),
                createSkinConfig(3L, "ADMIN_DARK", "Admin Dark", "ADMIN")
            ));
            // Role type configs
            allConfigs.addAll(Arrays.asList(
                createMasterConfig(10L, "ADMIN", "Administrator"),
                createMasterConfig(11L, "USER", "Standard User")
            ));
            
            when(masterConfigRepo.findByConfigTypes(List.of("SKIN", "ROLE_TYPE"))).thenReturn(allConfigs);

            // When
            List<SkinGroupDto> result = roleManagementService.getSkinGroups();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2); // ADMIN and USER groups
            
            // Verify ADMIN group has 2 skins and correct role type details
            SkinGroupDto adminGroup = result.stream()
                .filter(g -> g.getRoleType() != null && "ADMIN".equals(g.getRoleType().getKey()))
                .findFirst()
                .orElseThrow();
            assertThat(adminGroup.getSkins()).hasSize(2);
            assertThat(adminGroup.getRoleType().getId()).isEqualTo(10L);
            assertThat(adminGroup.getRoleType().getKey()).isEqualTo("ADMIN");
            assertThat(adminGroup.getRoleType().getName()).isEqualTo("Administrator");

            // Verify USER group has 1 skin and correct role type details
            SkinGroupDto userGroup = result.stream()
                .filter(g -> g.getRoleType() != null && "USER".equals(g.getRoleType().getKey()))
                .findFirst()
                .orElseThrow();
            assertThat(userGroup.getSkins()).hasSize(1);
            assertThat(userGroup.getRoleType().getId()).isEqualTo(11L);
            assertThat(userGroup.getRoleType().getKey()).isEqualTo("USER");
            assertThat(userGroup.getRoleType().getName()).isEqualTo("Standard User");

            verify(masterConfigRepo).findByConfigTypes(List.of("SKIN", "ROLE_TYPE"));
        }

        @Test
        @DisplayName("Should handle empty skin configurations")
        void shouldHandleEmptySkinConfigurations() {
            // Given
            when(masterConfigRepo.findByConfigTypes(List.of("SKIN", "ROLE_TYPE"))).thenReturn(Collections.emptyList());

            // When
            List<SkinGroupDto> result = roleManagementService.getSkinGroups();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(masterConfigRepo).findByConfigTypes(List.of("SKIN", "ROLE_TYPE"));
        }
    }

    @Nested
    @DisplayName("Save Or Update Role Tests")
    class SaveOrUpdateRoleTests {

        @Test
        @DisplayName("Should create new role when ID is not provided")
        void shouldCreateNewRoleWhenIdNotProvided() {
            // Given
            SaveRoleRequest request = ServiceTestDataBuilder.validRoleRequest("NEW_ROLE");
            Role savedRole = ServiceTestDataBuilder.activeRole("NEW_ROLE");
            savedRole.setId(1);

            when(roleRepo.save(any(Role.class))).thenReturn(savedRole);
            doNothing().when(rolePrivSkinRepo).deleteByRole_Id(anyInt());
            when(rolePrivSkinRepo.saveAll(anyList())).thenReturn(Collections.emptyList());

            // When
            RoleDto result = roleManagementService.saveOrUpdateFull(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("NEW_ROLE");
            assertThat(result.isActive()).isTrue();
            assertThat(result.getId()).isEqualTo(1L);

            // Verify role creation process
            ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepo).save(roleCaptor.capture());
            Role capturedRole = roleCaptor.getValue();
            assertThat(capturedRole.getName()).isEqualTo("NEW_ROLE");
            assertThat(capturedRole.getCreatedById()).isEqualTo(1); // System user ID
            assertThat(capturedRole.getCreatedOn()).isNotNull();

            verify(rolePrivSkinRepo).deleteByRole_Id(1);
        }

        @Test
        @DisplayName("Should update existing role when ID is provided")
        void shouldUpdateExistingRoleWhenIdProvided() {
            // Given
            SaveRoleRequest request = ServiceTestDataBuilder.validRoleRequest("UPDATED_ROLE");
            request.setId(1L);
            
            Role existingRole = ServiceTestDataBuilder.activeRole("OLD_ROLE");
            existingRole.setId(1);
            existingRole.setCreatedOn(LocalDateTime.now().minusDays(30));
            existingRole.setCreatedById(999); // Original user ID

            Role updatedRole = ServiceTestDataBuilder.activeRole("UPDATED_ROLE");
            updatedRole.setId(1);
            updatedRole.setCreatedOn(existingRole.getCreatedOn());
            updatedRole.setCreatedById(existingRole.getCreatedById());

            when(roleRepo.findById(1)).thenReturn(Optional.of(existingRole));
            when(roleRepo.save(any(Role.class))).thenReturn(updatedRole);
            doNothing().when(rolePrivSkinRepo).deleteByRole_Id(anyInt());

            // When
            RoleDto result = roleManagementService.saveOrUpdateFull(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("UPDATED_ROLE");
            assertThat(result.getId()).isEqualTo(1L);

            verify(roleRepo).findById(1);
            verify(roleRepo).save(any(Role.class));
            verify(rolePrivSkinRepo).deleteByRole_Id(1);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent role")
        void shouldThrowExceptionWhenUpdatingNonExistentRole() {
            // Given
            SaveRoleRequest request = ServiceTestDataBuilder.validRoleRequest("NON_EXISTENT");
            request.setId(999L);
            
            when(roleRepo.findById(999)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NoSuchElementException.class, 
                () -> roleManagementService.saveOrUpdateFull(request));

            verify(roleRepo).findById(999);
            verify(roleRepo, never()).save(any());
            verify(rolePrivSkinRepo, never()).deleteByRole_Id(anyInt());
        }

        @Test
        @DisplayName("Should save role with privileges and skin configurations")
        void shouldSaveRoleWithPrivilegesAndSkinConfigurations() {
            // Given
            SaveRoleRequest request = ServiceTestDataBuilder.validRoleRequest("ADMIN");
            request.setPrivilegeIds(Arrays.asList(1L, 2L));
            request.setSkinConfigIds(Arrays.asList(1L, 2L));
            
            Role savedRole = ServiceTestDataBuilder.activeRole("ADMIN");
            savedRole.setId(1);

            when(roleRepo.save(any(Role.class))).thenReturn(savedRole);
            doNothing().when(rolePrivSkinRepo).deleteByRole_Id(anyInt());
            when(rolePrivSkinRepo.saveAll(anyList())).thenReturn(Collections.emptyList());

            // When
            RoleDto result = roleManagementService.saveOrUpdateFull(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("ADMIN");

            // Verify privilege and skin mappings were saved with saveAll (2 privileges + 2 skins = 4 total)
            verify(rolePrivSkinRepo).saveAll(argThat(mappings -> {
                if (mappings == null) return false;
                var list = new java.util.ArrayList<MapRoleCategoryFeaturePrivilegeSkin>();
                mappings.forEach(list::add);
                return list.size() == 4;
            }));
            verify(rolePrivSkinRepo).deleteByRole_Id(1);
        }

        @Test
        @DisplayName("Should handle role with custom landing page configuration")
        void shouldHandleRoleWithCustomLandingPageConfiguration() {
            // Given
            SaveRoleRequest request = ServiceTestDataBuilder.validRoleRequest("CUSTOM_ROLE");
            request.setCustomLanding(true);
            request.setLandingPageConfigId(5L);
            
            Role savedRole = ServiceTestDataBuilder.activeRole("CUSTOM_ROLE");
            savedRole.setId(1);

            when(roleRepo.save(any(Role.class))).thenReturn(savedRole);
            doNothing().when(rolePrivSkinRepo).deleteByRole_Id(anyInt());

            // When
            RoleDto result = roleManagementService.saveOrUpdateFull(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("CUSTOM_ROLE");

            // Verify landing page configuration was set
            ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepo).save(roleCaptor.capture());
            Role capturedRole = roleCaptor.getValue();
            assertThat(capturedRole.getLandingPageConfig()).isNotNull();
            assertThat(capturedRole.getLandingPageConfig().getId()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle role without custom landing page")
        void shouldHandleRoleWithoutCustomLandingPage() {
            // Given
            SaveRoleRequest request = ServiceTestDataBuilder.validRoleRequest("STANDARD_ROLE");
            request.setCustomLanding(false);
            request.setLandingPageConfigId(null);
            
            Role savedRole = ServiceTestDataBuilder.activeRole("STANDARD_ROLE");
            savedRole.setId(1);

            when(roleRepo.save(any(Role.class))).thenReturn(savedRole);
            doNothing().when(rolePrivSkinRepo).deleteByRole_Id(anyInt());

            // When
            RoleDto result = roleManagementService.saveOrUpdateFull(request);

            // Then
            assertThat(result).isNotNull();

            // Verify landing page configuration was not set
            ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepo).save(roleCaptor.capture());
            Role capturedRole = roleCaptor.getValue();
            assertThat(capturedRole.getLandingPageConfig()).isNull();
        }
    }

    // ==================== Helper Methods ====================

    private List<MapCategoryFeaturePrivilege> createMockPrivilegeMappings() {
        MapCategoryFeaturePrivilege mapping1 = new MapCategoryFeaturePrivilege();
        mapping1.setId(1);
        
        // Create mock MasterConfig entities instead of separate entities
        MasterConfig category1 = new MasterConfig();
        category1.setId(1);
        category1.setName("USER_MANAGEMENT");
        category1.setKeyCode("USER_MGMT");
        
        MasterConfig feature1 = new MasterConfig();
        feature1.setId(1);
        feature1.setName("User Operations");
        feature1.setKeyCode("USER_OPS");
        
        MasterConfig privilege1 = new MasterConfig();
        privilege1.setId(1);
        privilege1.setName("Create User");
        privilege1.setKeyCode("CREATE_USER");
        
        mapping1.setFeatureCategory(category1);
        mapping1.setFeature(feature1);
        mapping1.setPrivilege(privilege1);

        MapCategoryFeaturePrivilege mapping2 = new MapCategoryFeaturePrivilege();
        mapping2.setId(2);
        
        MasterConfig category2 = new MasterConfig();
        category2.setId(2);
        category2.setName("ROLE_MANAGEMENT");
        category2.setKeyCode("ROLE_MGMT");
        
        MasterConfig feature2 = new MasterConfig();
        feature2.setId(2);
        feature2.setName("Role Operations");
        feature2.setKeyCode("ROLE_OPS");
        
        MasterConfig privilege2 = new MasterConfig();
        privilege2.setId(2);
        privilege2.setName("Create Role");
        privilege2.setKeyCode("CREATE_ROLE");
        
        mapping2.setFeatureCategory(category2);
        mapping2.setFeature(feature2);
        mapping2.setPrivilege(privilege2);

        return Arrays.asList(mapping1, mapping2);
    }

    private List<MapRoleCategoryFeaturePrivilegeSkin> createMockRoleMappings() {
        MapRoleCategoryFeaturePrivilegeSkin mapping = new MapRoleCategoryFeaturePrivilegeSkin();
        
        // Create mock role
        Role mockRole = new Role();
        mockRole.setId(1);
        mapping.setRole(mockRole);
        
        MapCategoryFeaturePrivilege privilege = new MapCategoryFeaturePrivilege();
        privilege.setId(1);
        mapping.setMapCategoryFeaturePrivilege(privilege);
        
        return Arrays.asList(mapping);
    }

    private List<Role> createMockRoles() {
        Role role1 = ServiceTestDataBuilder.activeRole("ADMIN");
        role1.setId(1);
        role1.setCreatedOn(LocalDateTime.now());
        role1.setCreatedById(1); // System user ID
        role1.setUpdatedOn(LocalDateTime.now());
        role1.setUpdatedById(1); // System user ID

        Role role2 = ServiceTestDataBuilder.activeRole("USER");
        role2.setId(2);
        role2.setCreatedOn(LocalDateTime.now());
        role2.setCreatedById(1); // System user ID
        role2.setUpdatedOn(LocalDateTime.now());
        role2.setUpdatedById(1); // System user ID

        return Arrays.asList(role1, role2);
    }

    private MasterConfig createMasterConfig(Long id, String keyCode, String name) {
        MasterConfig config = new MasterConfig();
        config.setId(id.intValue());
        config.setKeyCode(keyCode);
        config.setName(name);
        config.setConfigType("ROLE_TYPE"); // Set the config type for role type configs
        return config;
    }

    private MasterConfig createSkinConfig(Long id, String keyCode, String name, String stringValue) {
        MasterConfig config = createMasterConfig(id, keyCode, name);
        config.setStringValue(stringValue);
        config.setConfigType("SKIN"); // Override config type for skin configs
        return config;
    }

    private FeaturePrivilegeDTO findCategoryByName(List<FeaturePrivilegeDTO> categories, String name) {
        return categories.stream()
            .filter(c -> name.equals(c.getCategoryName()))
            .findFirst()
            .orElse(null);
    }

    @Nested
    @DisplayName("EntitySpecificSortValidator Tests")
    class EntitySpecificSortValidatorTests {

        @Test
        @DisplayName("Should validate sort field using universal validator")
        void shouldValidateSortFieldUsingUniversalValidator() {
            // Code Fix: Testing EntitySpecificSortValidator.isValidSortField() delegation to universalValidator
            // Arrange
            when(roleSearchConfig.getEntityClass()).thenReturn(Role.class);
            when(universalSortValidator.isValidSortField("roleName", Role.class)).thenReturn(true);
            when(universalSortValidator.isValidSortField("invalidField", Role.class)).thenReturn(false);
            
            // Create EntitySpecificSortValidator instance via reflection
            Object validator = createEntitySpecificSortValidator(roleSearchConfig, universalSortValidator);
            
            // Act & Assert
            assertTrue(invokeIsValidSortField(validator, "roleName"));
            assertFalse(invokeIsValidSortField(validator, "invalidField"));
            
            // Verify the universal validator was called correctly
            verify(universalSortValidator).isValidSortField("roleName", Role.class);
            verify(universalSortValidator).isValidSortField("invalidField", Role.class);
        }

        @Test
        @DisplayName("Should return valid sort fields from entity")
        void shouldReturnValidSortFieldsFromEntity() {
            // Code Fix: Testing EntitySpecificSortValidator.getValidSortFields() delegation to entity
            // Arrange
            Set<String> expectedFields = Set.of("roleName", "roleType", "createdOn", "updatedOn");
            when(roleSearchConfig.getSortableFields()).thenReturn(expectedFields);
            
            // Create EntitySpecificSortValidator instance via reflection
            Object validator = createEntitySpecificSortValidator(roleSearchConfig, universalSortValidator);
            
            // Act
            Set<String> actualFields = invokeGetValidSortFields(validator);
            
            // Assert
            assertThat(actualFields).isEqualTo(expectedFields);
            verify(roleSearchConfig).getSortableFields();
        }

        @Test
        @DisplayName("Should handle empty sort fields set")
        void shouldHandleEmptySortFieldsSet() {
            // Code Fix: Testing EntitySpecificSortValidator.getValidSortFields() with empty set
            // Arrange
            Set<String> emptyFields = Collections.emptySet();
            when(roleSearchConfig.getSortableFields()).thenReturn(emptyFields);
            
            // Create EntitySpecificSortValidator instance via reflection
            Object validator = createEntitySpecificSortValidator(roleSearchConfig, universalSortValidator);
            
            // Act
            Set<String> actualFields = invokeGetValidSortFields(validator);
            
            // Assert
            assertThat(actualFields).isEmpty();
            verify(roleSearchConfig).getSortableFields();
        }

        @Test
        @DisplayName("Should handle null field validation")
        void shouldHandleNullFieldValidation() {
            // Code Fix: Testing EntitySpecificSortValidator.isValidSortField() with null field
            // Arrange
            when(roleSearchConfig.getEntityClass()).thenReturn(Role.class);
            when(universalSortValidator.isValidSortField(null, Role.class)).thenReturn(false);
            
            // Create EntitySpecificSortValidator instance via reflection
            Object validator = createEntitySpecificSortValidator(roleSearchConfig, universalSortValidator);
            
            // Act & Assert
            assertFalse(invokeIsValidSortField(validator, null));
            verify(universalSortValidator).isValidSortField(null, Role.class);
        }

        @Test
        @DisplayName("Should handle different entity types")
        void shouldHandleDifferentEntityTypes() {
            // Code Fix: Testing EntitySpecificSortValidator with role entity class validation
            // Arrange
            when(roleSearchConfig.getEntityClass()).thenReturn(Role.class);
            when(universalSortValidator.isValidSortField("roleType", Role.class)).thenReturn(true);
            when(universalSortValidator.isValidSortField("createdOn", Role.class)).thenReturn(true);
            
            // Create EntitySpecificSortValidator instance via reflection
            Object validator = createEntitySpecificSortValidator(roleSearchConfig, universalSortValidator);
            
            // Act & Assert
            assertTrue(invokeIsValidSortField(validator, "roleType"));
            assertTrue(invokeIsValidSortField(validator, "createdOn"));
            verify(universalSortValidator).isValidSortField("roleType", Role.class);
            verify(universalSortValidator).isValidSortField("createdOn", Role.class);
        }

        // Helper methods to access private inner class via reflection
        private Object createEntitySpecificSortValidator(Object entity, Object universalValidator) {
            try {
                // Get the inner class
                Class<?>[] innerClasses = RoleManagementServiceImpl.class.getDeclaredClasses();
                Class<?> validatorClass = null;
                for (Class<?> innerClass : innerClasses) {
                    if (innerClass.getSimpleName().equals("EntitySpecificSortValidator")) {
                        validatorClass = innerClass;
                        break;
                    }
                }
                
                if (validatorClass == null) {
                    throw new RuntimeException("EntitySpecificSortValidator inner class not found");
                }
                
                // Import the SearchableEntity class
                Class<?> searchableEntityClass = com.itt.service.fw.search.SearchableEntity.class;
                Class<?> universalValidatorClass = com.itt.service.fw.search.UniversalSortFieldValidator.class;
                
                // Create instance using constructor with correct parameter types
                var constructor = validatorClass.getDeclaredConstructor(searchableEntityClass, universalValidatorClass);
                constructor.setAccessible(true);
                return constructor.newInstance(entity, universalValidator);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create EntitySpecificSortValidator", e);
            }
        }

        private boolean invokeIsValidSortField(Object validator, String field) {
            try {
                var method = validator.getClass().getMethod("isValidSortField", String.class);
                method.setAccessible(true);
                return (Boolean) method.invoke(validator, field);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke isValidSortField", e);
            }
        }

        @SuppressWarnings("unchecked")
        private Set<String> invokeGetValidSortFields(Object validator) {
            try {
                var method = validator.getClass().getMethod("getValidSortFields");
                method.setAccessible(true);
                return (Set<String>) method.invoke(validator);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke getValidSortFields", e);
            }
        }
    }
}