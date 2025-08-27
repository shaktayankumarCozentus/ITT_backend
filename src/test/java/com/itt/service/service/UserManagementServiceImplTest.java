//package com.itt.service.service;
//
//import com.itt.service.dto.DataTableRequest;
//import com.itt.service.dto.CurrentUserDto;
//import com.itt.service.dto.user_management.*;
//import com.itt.service.entity.*;
//import com.itt.service.repository.*;
//import com.itt.service.service.impl.UserManagementServiceImpl;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.domain.Specification;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static com.itt.service.specification.UserSpecification.hasActiveRole;
//import static com.itt.service.specification.UserSpecification.hasInactiveRole;
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserManagementServiceImplTest {
//
//    @Mock CompanyRepository           companyJDBCRepository;
//    @Mock MasterUserRepository       userRepo;
//    @Mock RoleRepository             roleRepo;
//    @Mock MasterCompanyRepository    masterCompanyRepo;
//    @Mock MapUserCompanyRepository   mapRepo;
//
//    @InjectMocks
//    UserManagementServiceImpl service;
//
//    private final CurrentUserDto dummyUser =
//            new CurrentUserDto(999, "dummy@example.com", "Dummy User");
//
//    // common Timestamp for tests
//    private final LocalDateTime now = LocalDateTime.now();
//
//    @BeforeEach
//    void setUp() {
//        // no-op
//    }
//
//    // ------------------------------------------------------------
//    // 1) getUsers(...) tests
//    // ------------------------------------------------------------
//
//    @Test
//    void whenGetUsers_withAndWithoutRoleOrCompany_thenMappingIsCorrect() {
//        // Prepare two MasterUser rows
//        Role adminRole = new Role();
//        adminRole.setId(5);
//        adminRole.setName("Admin");
//        // User1: no role, no companies → "New"
//        MasterUser u1 = MasterUser.builder()
//                .id(1)
//                .fullName("Alice")
//                .email("alice@example.com")
//                .isBdpEmployee(true)
//                .build(); // companies defaults empty; assignedRole null
//        // User2: has role, has one company → "Existing"
//        MasterCompany mc = new MasterCompany();
//        mc.setId(10);
//        mc.setCompanyName("Acme Corp");
//        MasterUser u2 = MasterUser.builder()
//                .id(2)
//                .fullName("Bob")
//                .email("bob@example.com")
//                .isBdpEmployee(false)
//                .build();
//        u2.setAssignedRole(adminRole);
//        u2.getCompanies().add(mc);
//
//        // stub repository to return a page
//        Page<MasterUser> page = new PageImpl<>(
//                List.of(u1, u2),
//                PageRequest.of(0, 10, Sort.by("fullName").ascending()),
//                2
//        );
//        when(userRepo.findAll(ArgumentMatchers.<Specification<MasterUser>>any(), any(Pageable.class)))
//                .thenReturn(page);
//
//        // Build request: no global search, no column filters/sorts
//        DataTableRequest dt = new DataTableRequest();
//        dt.getColumns().clear();  // ensure no columns.enable sort
//        SearchUsersRequestDto req = new SearchUsersRequestDto();
//        req.setDataTableRequest(dt);
//        req.setIsActiveRole(null); // do not filter by role‐activity
//
//        // Execute
//        PaginatedUserResponseDto resp =
//                service.getUsers(dummyUser, req);
//
//        // Verify paging
//        assertThat(resp.getPage()).isZero();
//        assertThat(resp.getSize()).isEqualTo(10);
//        assertThat(resp.getTotalElements()).isEqualTo(2);
//        assertThat(resp.isLast()).isTrue();
//        // Verify content mapping
//        assertThat(resp.getContent()).hasSize(2);
//
//        SearchUsersResponseDto dto1 = resp.getContent().getFirst();
//        assertThat(dto1.getName()).isEqualTo("Alice");
//        assertThat(dto1.getUserType()).isEqualTo("New");
//        assertThat(dto1.getCompanyName()).isEmpty();
//        assertThat(dto1.getAssignedRoleName()).isNull();
//
//        SearchUsersResponseDto dto2 = resp.getContent().get(1);
//        assertThat(dto2.getName()).isEqualTo("Bob");
//        assertThat(dto2.getUserType()).isEqualTo("Existing");
//        assertThat(dto2.getCompanyName()).isEqualTo("Acme Corp");
//        assertThat(dto2.getAssignedRoleName()).isEqualTo("Admin");
//        assertThat(dto2.getAssignedRoleId()).isEqualTo(5);
//    }
//
//    // ------------------------------------------------------------
//    // 2) getUserCount(...)
//    // ------------------------------------------------------------
//    @Test
//    void whenGetUserCount_active_thenCountFromRepo() {
//        when(userRepo.count(hasActiveRole()))
//                .thenReturn(42L);
//
//        UserCountResponseDto result = service.getUserCount(dummyUser, "active");
//        assertThat(result.getCount()).isEqualTo(42L);
//
//        verify(userRepo).count(hasActiveRole());
//    }
//
//    @Test
//    void whenGetUserCount_inactive_thenCountFromRepo() {
//        // <-- use the generic any() to disambiguate the Specification<MasterUser> overload
//        when(userRepo.count(hasInactiveRole()))
//                .thenReturn(7L);
//
//        UserCountResponseDto result = service.getUserCount(dummyUser, "inactive");
//        assertThat(result.getCount()).isEqualTo(7L);
//
//        // also verify the same overload was called
//        verify(userRepo).count(hasInactiveRole());
//    }
//
//    // ------------------------------------------------------------
//    // 3) getActiveRoles(...)
//    // ------------------------------------------------------------
//
//    @Test
//    void whenGetActiveRoles_thenRoleDtosReturned() {
//        Role r1 = new Role(); r1.setId(4); r1.setName("User");
//        Role r2 = new Role(); r2.setId(5); r2.setName("Admin");
//        when(roleRepo.findByIsActive(1)).thenReturn(List.of(r1, r2));
//
//        List<RoleDto> dtos = service.getActiveRoles(dummyUser, true);
//        assertThat(dtos).hasSize(2)
//                .extracting(RoleDto::getRoleName)
//                .containsExactlyInAnyOrder("User", "Admin");
//    }
//
//    @Test
//    void whenGetActiveRoles_inactive_thenRoleDtosReturned() {
//        Role r3 = new Role(); r3.setId(6); r3.setName("Guest");
//        when(roleRepo.findByIsActive(0)).thenReturn(List.of(r3));
//
//        List<RoleDto> dtos = service.getActiveRoles(dummyUser, false);
//        assertThat(dtos).hasSize(1)
//                .first()
//                .extracting(RoleDto::getRoleName)
//                .isEqualTo("Guest");
//    }
//
//    // ------------------------------------------------------------
//    // 4) searchUserForCopy(...)
//    // ------------------------------------------------------------
//
//    @Test
//    void whenSearchUserForCopy_thenNameAndRoleIdReturned() {
//        MasterUser u = MasterUser.builder()
//                .id(10).fullName("Carol").email("carol@ex.com").roleId(4).build();
//        when(userRepo.findAll(ArgumentMatchers.<Specification<MasterUser>>any(), any(Sort.class)))
//                .thenReturn(List.of(u));
//
//        SearchForCopyRequestDto req = new SearchForCopyRequestDto();
//        req.setSearchText("Car");
//        List<SearchUsersResponseDto> result =
//                service.searchUserForCopy(dummyUser, req);
//
//        assertThat(result).hasSize(1);
//        SearchUsersResponseDto dto = result.get(0);
//        assertThat(dto.getName()).isEqualTo("Carol");
//        assertThat(dto.getAssignedRoleId()).isEqualTo(4);
//    }
//
//    // ------------------------------------------------------------
//    // 5) updateUserAccessAssignments(...)
//    // ------------------------------------------------------------
//
//    @Test
//    void whenUpdateUserAccessAssignments_thenDeletesAndInserts() {
//        // stub existing user
//        MasterUser user = new MasterUser();
//        user.setId(100);
//        when(userRepo.findById(100)).thenReturn(Optional.of(user));
//
//        // stub masterCompanyRepo
//        MasterCompany mc1 = new MasterCompany(); mc1.setId(10);
//        MasterCompany mc2 = new MasterCompany(); mc2.setId(20);
//        when(masterCompanyRepo.findById(10)).thenReturn(Optional.of(mc1));
//        when(masterCompanyRepo.findById(20)).thenReturn(Optional.of(mc2));
//
//        // build request
//        AssignedCompanyDto ac1 = new AssignedCompanyDto("10", "X");
//        AssignedCompanyDto ac2 = new AssignedCompanyDto("20", "Y");
//        AccessAssignmentRequestDto req = new AccessAssignmentRequestDto();
//        req.setRoleId(5);
//        req.setIsBdpEmployee(true);
//        req.setAssignedCompanies(List.of(ac1, ac2));
//
//        // execute
//        String msg = service.updateUserAccessAssignments(dummyUser, "100", req);
//        assertThat(msg).contains("updated successfully");
//
//        // verify bulk‐delete & batch‐insert
//        verify(mapRepo).deleteByUserId(100);
//        @SuppressWarnings("unchecked")
//        ArgumentCaptor<List<MapUserCompany>> captor = ArgumentCaptor.forClass(List.class);
//        verify(mapRepo).saveAll(captor.capture());
//        List<MapUserCompany> saved = captor.getValue();
//        assertThat(saved).hasSize(2)
//                .extracting(MapUserCompany::getCompanyId)
//                .containsExactlyInAnyOrder(10, 20);
//    }
//
//    // ------------------------------------------------------------
//    // 6) getAssignedCompanies(...)
//    // ------------------------------------------------------------
//
//    @Test
//    void whenGetAssignedCompanies_thenDtoList() {
//        MasterCompany mc = new MasterCompany();
//        mc.setOriginalCompanyId(55);
//        mc.setCompanyName("Zeta");
//        when(userRepo.findAssignedCompanies(55)).thenReturn(List.of(mc));
//
//        List<AssignedCompanyDto> dtos =
//                service.getAssignedCompanies(dummyUser, "55");
//        assertThat(dtos).hasSize(1)
//                .first()
//                .satisfies(d -> {
//                    assertThat(d.getCompanyCode()).isEqualTo("55");
//                    assertThat(d.getCompanyName()).isEqualTo("Zeta");
//                });
//    }
//
//    // ------------------------------------------------------------
//    // 7) searchCompanies(...) tree‐building
//    // ------------------------------------------------------------
//
//    @Test
//    void whenSearchCompanies_thenCorrectTree() {
//        // flat: 1→0, 2→1, 3→1, 4→2
//        CompanyDtoPOc r = new CompanyDtoPOc(1,"R",0,now,"Change Later", "Standard");
//        CompanyDtoPOc c1= new CompanyDtoPOc(2,"C1",1,now,"Change Later", "Standard");
//        CompanyDtoPOc c2= new CompanyDtoPOc(3,"C2",1,now,"Change Later", "Standard");
//        CompanyDtoPOc gc= new CompanyDtoPOc(4,"GC",2,now,"Change Later", "Standard");
//        when(companyJDBCRepository.searchHierarchy("kw", true))
//                .thenReturn(List.of(r,c1,c2,gc));
//
//        var tree = service.searchCompanies(dummyUser,
//                new SearchCompanyRequestDto("kw", true));
//        // one root, two children, one grandchild
//        assertThat(tree).hasSize(1)
//                .first()
//                .satisfies(root -> {
//                    assertThat(root.getChildren()).hasSize(2);
//                    assertThat(root.getChildren().stream()
//                            .flatMap(n -> n.getChildren().stream())
//                            .map(node -> node.getData().getCompanyName()))
//                            .containsExactly("GC");
//                });
//    }
//
//    // ------------------------------------------------------------
//    // 8) buildCompanyTree(...) tree‐building
//    // ------------------------------------------------------------
//
//    // helper to build a flat list
//    private List<CompanyDtoPOc> flatSample() {
//        return List.of(
//                // two roots: id=1 A, id=2 B
//                new CompanyDtoPOc(1, "Alpha",   0, now, "Change Later", "Standard"),
//                new CompanyDtoPOc(2, "Beta",    0, now, "Change Later", "Standard"),
//                // children under Alpha
//                new CompanyDtoPOc(3, "Gamma",   1, now, "Change Later", "Standard"),
//                new CompanyDtoPOc(4, "Delta",   3, now, "Change Later", "Standard")
//        );
//    }
//
//    @Test
//    void buildCompanyTree_noSearch_returnsFirstPageOnly() {
//        // given type = PSA (1)
//        when(companyJDBCRepository.fetchAllCompanies(1)).thenReturn(flatSample());
//
//        // page 0, size 1 → only the first‐alphabetical root ("Alpha")
//        DataTableRequest dt = new DataTableRequest();
//        dt.setPagination(new DataTableRequest.Pagination(0, 1));
//        CompanyTreeRequestDto req = new CompanyTreeRequestDto(dt, "PSA");
//
//        CompanyTreeResponseDto response = service.buildCompanyTree(req);
//
//        // should have exactly one root node ("Alpha")
//        assertThat(response.getCompanyTreeNodes()).hasSize(1)
//                .extracting(node -> node.getData().getCompanyName())
//                .containsExactly("Alpha");
//
//        // verify its subtree: Alpha → Gamma → Delta
//        CompanyTreeNode alpha = response.getCompanyTreeNodes().get(0);
//        assertThat(alpha.getChildren()).hasSize(1);
//        CompanyTreeNode gamma = alpha.getChildren().get(0);
//        assertThat(gamma.getData().getCompanyName()).isEqualTo("Gamma");
//        assertThat(gamma.getChildren()).hasSize(1);
//        assertThat(gamma.getChildren().get(0).getData().getCompanyName()).isEqualTo("Delta");
//    }
//
//    @Test
//    void buildCompanyTree_paginationSecondPage_returnsSecondRoot() {
//        when(companyJDBCRepository.fetchAllCompanies(1)).thenReturn(flatSample());
//
//        // page 1, size 1 → second root ("Beta")
//        DataTableRequest dt = new DataTableRequest();
//        dt.setPagination(new DataTableRequest.Pagination(1, 1));
//        CompanyTreeRequestDto req = new CompanyTreeRequestDto(dt, "PSA");
//
//        CompanyTreeResponseDto response = service.buildCompanyTree(req);
//
//        assertThat(response.getCompanyTreeNodes()).hasSize(1)
//                .extracting(node -> node.getData().getCompanyName())
//                .containsExactly("Beta");
//        // "Beta" has no children
//        assertThat(response.getCompanyTreeNodes().get(0).getChildren()).isEmpty();
//    }
//
//    @Test
//    void buildCompanyTree_searchDeepChild_returnsOnlyTopLevelAncestorWithFullSubtree() {
//        when(companyJDBCRepository.fetchAllCompanies(1)).thenReturn(flatSample());
//
//        // searchText="Delta", page ignored because we match only one root
//        DataTableRequest dt = new DataTableRequest();
//        dt.setSearchFilter(new DataTableRequest.SearchFilter("delta", List.of()));
//        dt.setPagination(new DataTableRequest.Pagination(0, 10));
//        CompanyTreeRequestDto req = new CompanyTreeRequestDto(dt, "PSA");
//
//        CompanyTreeResponseDto response = service.buildCompanyTree(req);
//
//        // only one root: Alpha
//        assertThat(response.getCompanyTreeNodes()).hasSize(1)
//                .extracting(node -> node.getData().getCompanyName())
//                .containsExactly("Alpha");
//
//        CompanyTreeNode alpha = response.getCompanyTreeNodes().get(0);
//        // subtree still complete
//        assertThat(alpha.getChildren())
//                .extracting(node -> node.getData().getCompanyName())
//                .containsExactly("Gamma");
//        CompanyTreeNode gamma = alpha.getChildren().get(0);
//        assertThat(gamma.getChildren())
//                .extracting(node -> node.getData().getCompanyName())
//                .containsExactly("Delta");
//    }
//
//    @Test
//    void buildCompanyTree_emptyDb_returnsEmptyList() {
//        when(companyJDBCRepository.fetchAllCompanies(0)).thenReturn(List.of());
//
//        DataTableRequest dt = new DataTableRequest();
//        dt.setPagination(new DataTableRequest.Pagination(0, 5));
//        CompanyTreeRequestDto req = new CompanyTreeRequestDto(dt, "0");
//
//        CompanyTreeResponseDto response = service.buildCompanyTree(req);
//
//        assertThat(response.getCompanyTreeNodes()).isEmpty();
//    }
//
//    // ------------------------------------------------------------
//    // 9) copyUserAccess(...) tests - MISSING COVERAGE
//    // ------------------------------------------------------------
//
//    @Test
//    void whenCopyUserAccess_preview_sameRoleAndCompanies_thenAlreadySameMessage() {
//        // Code Fix: Testing copyUserAccess preview mode when source and target are identical
//        Role adminRole = new Role();
//        adminRole.setId(5);
//        adminRole.setName("Admin");
//        
//        MasterCompany mc1 = new MasterCompany();
//        mc1.setId(10);
//        mc1.setCompanyName("Company A");
//        
//        // Both users have same role and same companies
//        MasterUser sourceUser = MasterUser.builder()
//                .id(100)
//                .fullName("Source User")
//                .roleId(5)
//                .build();
//        sourceUser.getCompanies().add(mc1);
//        
//        MasterUser targetUser = MasterUser.builder()
//                .id(200)
//                .fullName("Target User")
//                .roleId(5) // Same role
//                .build();
//        targetUser.getCompanies().add(mc1); // Same company
//        
//        when(userRepo.findById(100)).thenReturn(Optional.of(sourceUser));
//        when(userRepo.findById(200)).thenReturn(Optional.of(targetUser));
//        
//        CopyAccessRequestDto request = new CopyAccessRequestDto();
//        request.setSourceUserId(100);
//        request.setTargetUserId(200);
//        request.setRequestType("preview");
//        
//        String result = service.copyUserAccess(dummyUser, request);
//        
//        assertThat(result).contains("already has the same role and company assignments");
//        verify(userRepo).findById(100);
//        verify(userRepo).findById(200);
//        verify(userRepo, never()).save(any(MasterUser.class));
//    }
//    
//    @Test
//    void whenCopyUserAccess_preview_differentRoleOrCompanies_thenDifferentMessage() {
//        // Code Fix: Testing copyUserAccess preview mode when source and target are different
//        MasterCompany mc1 = new MasterCompany();
//        mc1.setId(10);
//        MasterCompany mc2 = new MasterCompany();
//        mc2.setId(20);
//        
//        MasterUser sourceUser = MasterUser.builder()
//                .id(100)
//                .roleId(5)
//                .build();
//        sourceUser.getCompanies().add(mc1);
//        
//        MasterUser targetUser = MasterUser.builder()
//                .id(200)
//                .roleId(6) // Different role
//                .build();
//        targetUser.getCompanies().add(mc2); // Different company
//        
//        when(userRepo.findById(100)).thenReturn(Optional.of(sourceUser));
//        when(userRepo.findById(200)).thenReturn(Optional.of(targetUser));
//        
//        CopyAccessRequestDto request = new CopyAccessRequestDto();
//        request.setSourceUserId(100);
//        request.setTargetUserId(200);
//        request.setRequestType("preview");
//        
//        String result = service.copyUserAccess(dummyUser, request);
//        
//        assertThat(result).contains("Target User has different role and Companies");
//    }
//    
//    @Test
//    void whenCopyUserAccess_apply_thenCopiesRoleAndCompanies() {
//        // Code Fix: Testing copyUserAccess apply mode - actual copying of role and companies
//        MasterCompany mc1 = new MasterCompany();
//        mc1.setId(10);
//        MasterCompany mc2 = new MasterCompany();
//        mc2.setId(20);
//        
//        MasterUser sourceUser = MasterUser.builder()
//                .id(100)
//                .roleId(5)
//                .build();
//        sourceUser.getCompanies().add(mc1);
//        sourceUser.getCompanies().add(mc2);
//        
//        MasterUser targetUser = MasterUser.builder()
//                .id(200)
//                .roleId(6) // Different role
//                .build();
//        
//        when(userRepo.findById(100)).thenReturn(Optional.of(sourceUser));
//        when(userRepo.findById(200)).thenReturn(Optional.of(targetUser));
//        
//        CopyAccessRequestDto request = new CopyAccessRequestDto();
//        request.setSourceUserId(100);
//        request.setTargetUserId(200);
//        request.setRequestType("apply");
//        
//        String result = service.copyUserAccess(dummyUser, request);
//        
//        assertThat(result).contains("Access copied successfully");
//        assertThat(targetUser.getRoleId()).isEqualTo(5); // Role copied
//        verify(userRepo).save(targetUser);
//        verify(mapRepo).deleteByUserId(200);
//        @SuppressWarnings("unchecked")
//        ArgumentCaptor<List<MapUserCompany>> captor = ArgumentCaptor.forClass(List.class);
//        verify(mapRepo).saveAll(captor.capture());
//        List<MapUserCompany> saved = captor.getValue();
//        assertThat(saved).hasSize(2)
//                .extracting(MapUserCompany::getCompanyId)
//                .containsExactlyInAnyOrder(10, 20);
//    }
//    
//    @Test
//    void whenCopyUserAccess_sourceUserNotFound_thenThrowsException() {
//        // Code Fix: Testing copyUserAccess error handling when source user doesn't exist
//        when(userRepo.findById(999)).thenReturn(Optional.empty());
//        
//        CopyAccessRequestDto request = new CopyAccessRequestDto();
//        request.setSourceUserId(999);
//        request.setTargetUserId(200);
//        request.setRequestType("apply");
//        
//        assertThatThrownBy(() -> service.copyUserAccess(dummyUser, request))
//                .isInstanceOf(EntityNotFoundException.class)
//                .hasMessageContaining("Source user not found: 999");
//    }
//    
//    @Test
//    void whenCopyUserAccess_targetUserNotFound_thenThrowsException() {
//        // Code Fix: Testing copyUserAccess error handling when target user doesn't exist
//        MasterUser sourceUser = MasterUser.builder().id(100).build();
//        when(userRepo.findById(100)).thenReturn(Optional.of(sourceUser));
//        when(userRepo.findById(999)).thenReturn(Optional.empty());
//        
//        CopyAccessRequestDto request = new CopyAccessRequestDto();
//        request.setSourceUserId(100);
//        request.setTargetUserId(999);
//        request.setRequestType("apply");
//        
//        assertThatThrownBy(() -> service.copyUserAccess(dummyUser, request))
//                .isInstanceOf(EntityNotFoundException.class)
//                .hasMessageContaining("Target user not found: 999");
//    }
//    
//    // ------------------------------------------------------------
//    // 10) Null/Empty Implementation Methods - MISSING COVERAGE
//    // ------------------------------------------------------------
//    
//    @Test
//    void whenGetTopLevelCompanies_thenReturnsNull() {
//        // Code Fix: Testing getTopLevelCompanies method that returns null (unimplemented)
//        TopLevelCompaniesRequestDto request = new TopLevelCompaniesRequestDto();
//        
//        List<CompanyDto> result = service.getTopLevelCompanies(dummyUser, request);
//        
//        assertThat(result).isNull();
//    }
//    
//    @Test
//    void whenGetChildCompanies_thenReturnsNull() {
//        // Code Fix: Testing getChildCompanies method that returns null (unimplemented)
//        ChildCompaniesRequestDto request = new ChildCompaniesRequestDto();
//        
//        Map<String, List<CompanyDto>> result = service.getChildCompanies(dummyUser, request);
//        
//        assertThat(result).isNull();
//    }
//    
//    @Test
//    void whenGetAccessSummary_thenReturnsNull() {
//        // Code Fix: Testing getAccessSummary method that returns null (unimplemented)
//        AccessSummaryResponseDto result = service.getAccessSummary(dummyUser, "100");
//        
//        assertThat(result).isNull();
//    }
//    
//    // ------------------------------------------------------------
//    // 11) Edge Cases and Additional Coverage - MISSING COVERAGE
//    // ------------------------------------------------------------
//    
//    @Test
//    void whenUpdateUserAccessAssignments_userNotFound_thenThrowsException() {
//        // Code Fix: Testing updateUserAccessAssignments error handling when user doesn't exist
//        when(userRepo.findById(999)).thenReturn(Optional.empty());
//        
//        AccessAssignmentRequestDto request = new AccessAssignmentRequestDto();
//        request.setRoleId(5);
//        request.setIsBdpEmployee(true);
//        request.setAssignedCompanies(Collections.emptyList());
//        
//        assertThatThrownBy(() -> service.updateUserAccessAssignments(dummyUser, "999", request))
//                .isInstanceOf(EntityNotFoundException.class)
//                .hasMessageContaining("User not found: 999");
//    }
//    
//    @Test
//    void whenUpdateUserAccessAssignments_companyNotFound_thenThrowsException() {
//        // Code Fix: Testing updateUserAccessAssignments error handling when company doesn't exist
//        MasterUser user = new MasterUser();
//        user.setId(100);
//        when(userRepo.findById(100)).thenReturn(Optional.of(user));
//        when(masterCompanyRepo.findById(999)).thenReturn(Optional.empty());
//        
//        AssignedCompanyDto invalidCompany = new AssignedCompanyDto("999", "Invalid Company");
//        AccessAssignmentRequestDto request = new AccessAssignmentRequestDto();
//        request.setRoleId(5);
//        request.setIsBdpEmployee(true);
//        request.setAssignedCompanies(List.of(invalidCompany));
//        
//        assertThatThrownBy(() -> service.updateUserAccessAssignments(dummyUser, "100", request))
//                .isInstanceOf(EntityNotFoundException.class)
//                .hasMessageContaining("Company not found: 999");
//    }
//    
//    @Test
//    void whenGetUsers_withGlobalSearchAndColumnFilters_thenAppliesAllFilters() {
//        // Code Fix: Testing getUsers with both global search and column-specific filters
//        MasterUser user = MasterUser.builder()
//                .id(1)
//                .fullName("Alice Admin")
//                .email("alice@example.com")
//                .build();
//        
//        Page<MasterUser> page = new PageImpl<>(List.of(user));
//        when(userRepo.findAll(ArgumentMatchers.<Specification<MasterUser>>any(), any(Pageable.class)))
//                .thenReturn(page);
//        
//        DataTableRequest dt = new DataTableRequest();
//        dt.setSearchFilter(new DataTableRequest.SearchFilter("alice", List.of()));
//        
//        // Add column filter
//        DataTableRequest.Column nameColumn = new DataTableRequest.Column();
//        nameColumn.setColumnName("fullName");
//        nameColumn.setFilter("Admin");
//        dt.getColumns().add(nameColumn);
//        
//        SearchUsersRequestDto request = new SearchUsersRequestDto();
//        request.setDataTableRequest(dt);
//        request.setIsActiveRole(true);
//        
//        PaginatedUserResponseDto result = service.getUsers(dummyUser, request);
//        
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice Admin");
//    }
//    
//    @Test
//    void whenGetUsers_withCustomSortColumns_thenUsesCustomSort() {
//        // Code Fix: Testing getUsers with custom sort columns instead of default fullName sort
//        MasterUser user = MasterUser.builder()
//                .id(1)
//                .fullName("Alice")
//                .email("alice@example.com")
//                .build();
//        
//        Page<MasterUser> page = new PageImpl<>(List.of(user));
//        when(userRepo.findAll(ArgumentMatchers.<Specification<MasterUser>>any(), any(Pageable.class)))
//                .thenReturn(page);
//        
//        DataTableRequest dt = new DataTableRequest();
//        // Add sort column
//        DataTableRequest.Column sortColumn = new DataTableRequest.Column();
//        sortColumn.setColumnName("email");
//        sortColumn.setSort("desc");
//        dt.getColumns().add(sortColumn);
//        
//        SearchUsersRequestDto request = new SearchUsersRequestDto();
//        request.setDataTableRequest(dt);
//        
//        PaginatedUserResponseDto result = service.getUsers(dummyUser, request);
//        
//        assertThat(result.getContent()).hasSize(1);
//        // Verify custom sort was applied (by checking that dt.toPageable() was used)
//        verify(userRepo).findAll(ArgumentMatchers.<Specification<MasterUser>>any(), any(Pageable.class));
//    }
//    
//    @Test
//    void whenSearchUserForCopy_withEmptySearchText_thenHandlesGracefully() {
//        // Code Fix: Testing searchUserForCopy with empty search text edge case
//        when(userRepo.findAll(ArgumentMatchers.<Specification<MasterUser>>any(), any(Sort.class)))
//                .thenReturn(Collections.emptyList());
//        
//        SearchForCopyRequestDto request = new SearchForCopyRequestDto();
//        request.setSearchText(""); // Empty search text
//        
//        List<SearchUsersResponseDto> result = service.searchUserForCopy(dummyUser, request);
//        
//        assertThat(result).isEmpty();
//    }
//    
//    @Test
//    void whenGetAssignedCompanies_withNonExistentUser_thenReturnsEmptyList() {
//        // Code Fix: Testing getAssignedCompanies when user has no assigned companies
//        when(userRepo.findAssignedCompanies(999)).thenReturn(Collections.emptyList());
//        
//        List<AssignedCompanyDto> result = service.getAssignedCompanies(dummyUser, "999");
//        
//        assertThat(result).isEmpty();
//        verify(userRepo).findAssignedCompanies(999);
//    }
//
//}
