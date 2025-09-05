package com.itt.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itt.service.dto.CompanyRequestDTO;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.customer_subscription.CompanyDTO;
import com.itt.service.dto.customer_subscription.CustomerSubscriptionDTO;
import com.itt.service.dto.customer_subscription.SubscriptionBulkUpdateRequest;
import com.itt.service.dto.customer_subscription.SubscriptionCopyRequest;
import com.itt.service.dto.customer_subscription.SubscriptionUpdateRequest;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.entity.MapCompanySubscription;
import com.itt.service.entity.MasterCompany;
import com.itt.service.enums.ErrorCode;
import com.itt.service.exception.CustomException;
import com.itt.service.mapper.CustomerSubscriptionMapper;
import com.itt.service.repository.MapCompanySubscriptionRepository;
import com.itt.service.repository.MasterCompanyRepository;
import com.itt.service.service.CustomerSubscriptonDomainSyncService;
import com.itt.service.service.MasterDataService;
import com.itt.service.service.impl.CustomerSubscriptionServiceImpl;
import com.itt.service.validator.SortFieldValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerSubscriptionServiceImpl - Business Logic Tests")
class CustomerSubscriptionServiceImplTest {

	@Mock
	private MapCompanySubscriptionRepository mapRepo;
	@Mock
	private MasterCompanyRepository companyRepo;
	@Mock
	private SortFieldValidator sortFieldValidator;
	@Mock
	private CustomerSubscriptionMapper mapper;
	@Mock
	private CustomerSubscriptonDomainSyncService domainSyncService;
	@Mock
	private MasterDataService masterDataService;
	@Mock
	private EntityManager entityManager;
	@Mock
	private Query nativeQuery;
	@InjectMocks
	private CustomerSubscriptionServiceImpl service;

	// ==================== Bulk Update Tests ====================
	@Test
	@DisplayName("Should bulk update subscription tier for multiple companies")
	void shouldBulkUpdateSubscriptionTierForMultipleCompanies() {
		SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
		request.setSubscriptionTierType(2);
		request.setCompanyCodes(Arrays.asList(101, 102, 103));

		when(domainSyncService.sendBulkCustomerSubscriptionUpdateToDomains(request)).thenReturn(true);
		when(companyRepo.updateSubscriptionTypeByCompanyCodes(anyInt(), anyList())).thenReturn(3);

		String result = service.bulkUpdateSubscriptionTier(request);

		assertThat(result).contains("3");
		verify(domainSyncService).sendBulkCustomerSubscriptionUpdateToDomains(request);
		verify(companyRepo).updateSubscriptionTypeByCompanyCodes(2, Arrays.asList(101, 102, 103));
	}

	@Test
	@DisplayName("Should bulk update subscription tier for single company")
	void shouldBulkUpdateSubscriptionTierForSingleCompany() {
		SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
		request.setSubscriptionTierType(1);
		request.setCompanyCodes(Collections.singletonList(101));

		when(domainSyncService.sendBulkCustomerSubscriptionUpdateToDomains(request)).thenReturn(true);
		when(companyRepo.updateSubscriptionTypeByCompanyCodes(1, Collections.singletonList(101))).thenReturn(1);

		String result = service.bulkUpdateSubscriptionTier(request);

		assertThat(result).contains("1").doesNotContain("companies"); // Should not pluralize
		verify(domainSyncService).sendBulkCustomerSubscriptionUpdateToDomains(request);
		verify(companyRepo).updateSubscriptionTypeByCompanyCodes(1, Collections.singletonList(101));
	}

	@Test
	@DisplayName("Should handle exception during bulk update")
	void shouldHandleExceptionDuringBulkUpdate() {
		SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
		request.setSubscriptionTierType(2);
		request.setCompanyCodes(Arrays.asList(101, 102));

		when(domainSyncService.sendBulkCustomerSubscriptionUpdateToDomains(request)).thenReturn(false);

		CustomException ex = assertThrows(CustomException.class, () -> service.bulkUpdateSubscriptionTier(request));
		assertEquals(ErrorCode.DATABASE_CONNECTION_ERROR, ex.getErrorCode()); // Service catches all exceptions and wraps them
		verify(domainSyncService).sendBulkCustomerSubscriptionUpdateToDomains(request);
	}

	@Test
	@DisplayName("Should handle null companyCodes in request")
	void shouldHandleNullCompanyCodesInRequest() {
		SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
		request.setSubscriptionTierType(1);
		request.setCompanyCodes(null);

		when(domainSyncService.sendBulkCustomerSubscriptionUpdateToDomains(request)).thenReturn(true);
		when(companyRepo.updateSubscriptionTypeByCompanyCodes(1, null)).thenReturn(0);

		String result = service.bulkUpdateSubscriptionTier(request);

		assertThat(result).contains("0");
		verify(domainSyncService).sendBulkCustomerSubscriptionUpdateToDomains(request);
		verify(companyRepo).updateSubscriptionTypeByCompanyCodes(1, null);
	}

	// ==================== Single Update Tests ====================
	@Test
	@DisplayName("Should update customer subscription and features")
	void shouldUpdateCustomerSubscriptionAndFeatures() {
		SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
		request.setSubscriptionTierType(1);
		request.setCompanyCode(101);
		request.setFeatureIds(Arrays.asList(10, 20));

		MasterCompany mockCompany = new MasterCompany();
		mockCompany.setId(101);
		mockCompany.setCompanyName("Test Company");

		when(domainSyncService.sendCustomerSubscriptionUpdateToDomains(request)).thenReturn(true);
		when(companyRepo.findById(101)).thenReturn(java.util.Optional.of(mockCompany));
		when(mapRepo.saveAll(anyList())).thenReturn(Collections.emptyList());

		String result = service.updateCustomerSubscription(request);

		assertThat(result).contains("successful");
		verify(domainSyncService).sendCustomerSubscriptionUpdateToDomains(request);
		verify(companyRepo).updateSubscriptionTypeByCompanyCode(1, 101);
		verify(mapRepo).deleteByCompanyId(101);
	}

	@Test
	@DisplayName("Should update customer subscription without features")
	void shouldUpdateCustomerSubscriptionWithoutFeatures() {
		SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
		request.setSubscriptionTierType(1);
		request.setCompanyCode(101);
		request.setFeatureIds(Collections.emptyList());

		MasterCompany mockCompany = new MasterCompany();
		mockCompany.setId(101);
		mockCompany.setCompanyName("Test Company");

		when(domainSyncService.sendCustomerSubscriptionUpdateToDomains(request)).thenReturn(true);
		when(companyRepo.findById(101)).thenReturn(java.util.Optional.of(mockCompany));

		String result = service.updateCustomerSubscription(request);

		assertThat(result).contains("successful");
		verify(domainSyncService).sendCustomerSubscriptionUpdateToDomains(request);
		verify(companyRepo).updateSubscriptionTypeByCompanyCode(1, 101);
		verify(mapRepo, never()).deleteByCompanyId(anyInt());
		verify(mapRepo, never()).saveAll(anyList());
	}

	@Test
	@DisplayName("Should handle exception during single update")
	void shouldHandleExceptionDuringSingleUpdate() {
		SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
		request.setSubscriptionTierType(1);
		request.setCompanyCode(101);

		when(domainSyncService.sendCustomerSubscriptionUpdateToDomains(request)).thenReturn(false);

		CustomException ex = assertThrows(CustomException.class, () -> service.updateCustomerSubscription(request));
		assertEquals(ErrorCode.DATABASE_CONNECTION_ERROR, ex.getErrorCode()); // Service catches all exceptions and wraps them
		verify(domainSyncService).sendCustomerSubscriptionUpdateToDomains(request);
	}

	// ==================== Get All Features Tests ====================
	@Test
	@DisplayName("Should return all feature IDs for company")
	void shouldReturnAllFeatureIdsForCompany() {
		List<MapCompanySubscription> features = Arrays.asList(new MapCompanySubscription(101, 10, 1, 1, 1),
				new MapCompanySubscription(101, 20, 1, 1, 1));
		when(mapRepo.findByCompanyId(101)).thenReturn(features);

		List<Integer> result = service.getAllFeaturesByCompanyCode(101);

		assertThat(result).containsExactly(10, 20);
		verify(mapRepo).findByCompanyId(101);
	}

	@Test
	@DisplayName("Should return empty list when no features found")
	void shouldReturnEmptyListWhenNoFeaturesFound() {
		when(mapRepo.findByCompanyId(101)).thenReturn(Collections.emptyList());

		List<Integer> result = service.getAllFeaturesByCompanyCode(101);

		assertThat(result).isEmpty();
		verify(mapRepo).findByCompanyId(101);
	}

	// ==================== Copy Subscription Tests ====================

	@Test
	@DisplayName("Should copy subscription features between companies successfully")
	void shouldCopySubscriptionFeaturesBetweenCompaniesSuccessfully() {
		SubscriptionCopyRequest request = new SubscriptionCopyRequest();
		request.setSourceCompanyId(101);
		request.setTargetCompanyIds(Arrays.asList(201, 202));

		when(companyRepo.copyCustomerSubscription(101, Arrays.asList(201, 202), 0)).thenReturn(2);

		// Spy the service to verify bulkUpsertFeatures is called
		CustomerSubscriptionServiceImpl spyService = org.mockito.Mockito.spy(service);

		// Use doNothing for the private helper via reflection (or make it
		// package-private for easier testing)
		org.mockito.Mockito.doNothing().when(spyService).bulkUpsertFeatures(101, Arrays.asList(201, 202), 0);

		String result = spyService.copyCustomerSubscription(request);

		assertThat(result).contains("2");
		verify(companyRepo).copyCustomerSubscription(101, Arrays.asList(201, 202), 0);
		verify(spyService).bulkUpsertFeatures(101, Arrays.asList(201, 202), 0);
	}

	@Test
	@DisplayName("Should handle exception during copy subscription")
	void shouldHandleExceptionDuringCopySubscription() {
		SubscriptionCopyRequest request = new SubscriptionCopyRequest();
		request.setSourceCompanyId(101);
		request.setTargetCompanyIds(Arrays.asList(201, 202));

		when(companyRepo.copyCustomerSubscription(101, Arrays.asList(201, 202), 0))
				.thenThrow(new RuntimeException("DB error"));

		CustomException ex = assertThrows(CustomException.class, () -> service.copyCustomerSubscription(request));
		assertEquals(ErrorCode.DATABASE_CONNECTION_ERROR, ex.getErrorCode());
	}

	@Test
	@DisplayName("Should not call bulkUpsertFeatures when targetCompanyIds is empty")
	void shouldNotCallBulkUpsertFeaturesWhenTargetCompanyIdsIsEmpty() {
		SubscriptionCopyRequest request = new SubscriptionCopyRequest();
		request.setSourceCompanyId(101);
		request.setTargetCompanyIds(Collections.emptyList());

		when(companyRepo.copyCustomerSubscription(101, Collections.emptyList(), 0)).thenReturn(0);

		CustomerSubscriptionServiceImpl spyService = org.mockito.Mockito.spy(service);
		org.mockito.Mockito.doNothing().when(spyService).bulkUpsertFeatures(101, Collections.emptyList(), 0);

		String result = spyService.copyCustomerSubscription(request);

		assertThat(result).contains("0");
		verify(companyRepo).copyCustomerSubscription(101, Collections.emptyList(), 0);
		verify(spyService).bulkUpsertFeatures(101, Collections.emptyList(), 0);
	}

	// ==================== Search and Pagination Tests ====================
	
	@Test
	@DisplayName("Should find all customer subscriptions using search framework")
	void shouldFindAllCustomerSubscriptionsUsingSearchFramework() {
		// Code Fix: Testing findAll() method that delegates to BaseService.search()
		DataTableRequest request = new DataTableRequest();
		request.getPagination().setPage(0);
		request.getPagination().setSize(10);
		
		CustomerSubscriptionDTO mockDto = new CustomerSubscriptionDTO();
		mockDto.setCustomerId(101);
		mockDto.setCustomerName("Test Company");
		
		PaginationResponse<CustomerSubscriptionDTO> mockResponse = new PaginationResponse<>();
		mockResponse.setContent(List.of(mockDto));
		mockResponse.setTotalElements(1L);
		
		// Spy the service to mock the BaseService.search() call
		CustomerSubscriptionServiceImpl spyService = spy(service);
		org.mockito.Mockito.doReturn(mockResponse).when(spyService).search(request);
		
		PaginationResponse<CustomerSubscriptionDTO> result = spyService.findAll(request);
		
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getCustomerId()).isEqualTo(101);
		verify(spyService).search(request);
	}
	
	@Test
	@DisplayName("Should find all customer subscriptions with features populated")
	void shouldFindAllCustomerSubscriptionsWithFeaturesPopulated() {
		// Code Fix: Testing findAllWithFeatures() that enriches results with feature data
		DataTableRequest request = new DataTableRequest();
		request.getPagination().setPage(0);
		request.getPagination().setSize(10);
		
		CustomerSubscriptionDTO mockDto1 = new CustomerSubscriptionDTO();
		mockDto1.setCustomerId(101);
		mockDto1.setCustomerName("Company 1");
		
		CustomerSubscriptionDTO mockDto2 = new CustomerSubscriptionDTO();
		mockDto2.setCustomerId(102);
		mockDto2.setCustomerName("Company 2");
		
		PaginationResponse<CustomerSubscriptionDTO> mockResponse = new PaginationResponse<>();
		mockResponse.setContent(Arrays.asList(mockDto1, mockDto2));
		mockResponse.setTotalElements(2L);
		
		// Mock feature mappings
		Map<Integer, List<Integer>> featureMap = new HashMap<>();
		featureMap.put(101, Arrays.asList(10, 20));
		featureMap.put(102, Arrays.asList(30));
		
		CustomerSubscriptionServiceImpl spyService = spy(service);
		org.mockito.Mockito.doReturn(mockResponse).when(spyService).search(request);
		org.mockito.Mockito.doReturn(featureMap).when(spyService).getAllFeaturesByCompanyCodes(Arrays.asList(101, 102));
		
		PaginationResponse<CustomerSubscriptionDTO> result = spyService.findAllWithFeatures(request);
		
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().get(0).getFeatureIds()).containsExactly(10, 20);
		assertThat(result.getContent().get(1).getFeatureIds()).containsExactly(30);
		verify(spyService).search(request);
		verify(spyService).getAllFeaturesByCompanyCodes(Arrays.asList(101, 102));
	}
	
	@Test
	@DisplayName("Should handle empty features in findAllWithFeatures")
	void shouldHandleEmptyFeaturesInFindAllWithFeatures() {
		// Code Fix: Testing edge case where companies have no features
		DataTableRequest request = new DataTableRequest();
		
		CustomerSubscriptionDTO mockDto = new CustomerSubscriptionDTO();
		mockDto.setCustomerId(101);
		mockDto.setCustomerName("Company Without Features");
		
		PaginationResponse<CustomerSubscriptionDTO> mockResponse = new PaginationResponse<>();
		mockResponse.setContent(List.of(mockDto));
		
		Map<Integer, List<Integer>> emptyFeatureMap = new HashMap<>();
		
		CustomerSubscriptionServiceImpl spyService = spy(service);
		org.mockito.Mockito.doReturn(mockResponse).when(spyService).search(request);
		org.mockito.Mockito.doReturn(emptyFeatureMap).when(spyService).getAllFeaturesByCompanyCodes(List.of(101));
		
		PaginationResponse<CustomerSubscriptionDTO> result = spyService.findAllWithFeatures(request);
		
		assertThat(result.getContent().get(0).getFeatureIds()).isEmpty();
	}
	
	// ==================== Master Config and Features Tests ====================
	
	@Test
	@DisplayName("Should get all features and subscription types from master config")
	void shouldGetAllFeaturesAndSubscriptionTypesFromMasterConfig() {
		// Code Fix: Testing getAllFeaturesSubscriptionTypeFromMasterConfig() method
		MasterConfigDTO feature1 = new MasterConfigDTO();
		feature1.setId(1);
		feature1.setKeyCode("FEATURE_1");
		feature1.setConfigType("FEATURE_TOGGLE");
		
		MasterConfigDTO subscription1 = new MasterConfigDTO();
		subscription1.setId(2);
		subscription1.setKeyCode("BASIC");
		subscription1.setConfigType("SUBSCRIPTION_TYPE");
		
		List<MasterConfigDTO> mockConfigs = Arrays.asList(feature1, subscription1);
		
		when(masterDataService.getConfigByTypeIn(List.of("FEATURE_TOGGLE", "SUBSCRIPTION_TYPE"))).thenReturn(mockConfigs);
		
		List<MasterConfigDTO> result = service.getAllFeaturesSubscriptionTypeFromMasterConfig();
		
		assertThat(result).hasSize(2);
		assertThat(result).containsExactly(feature1, subscription1);
		verify(masterDataService).getConfigByTypeIn(List.of("FEATURE_TOGGLE", "SUBSCRIPTION_TYPE"));
	}
	
	@Test
	@DisplayName("Should get features by company codes and group by company ID")
	void shouldGetFeaturesByCompanyCodesAndGroupByCompanyId() {
		// Code Fix: Testing getAllFeaturesByCompanyCodes() method that groups features by company
		List<Integer> companyCodes = Arrays.asList(101, 102);
		
		MapCompanySubscription feature1 = new MapCompanySubscription(101, 10, 1, 1, 1);
		MapCompanySubscription feature2 = new MapCompanySubscription(101, 20, 1, 1, 1);
		MapCompanySubscription feature3 = new MapCompanySubscription(102, 30, 1, 1, 1);
		
		List<MapCompanySubscription> mockFeatures = Arrays.asList(feature1, feature2, feature3);
		
		when(mapRepo.findByCompanyIdIn(companyCodes)).thenReturn(mockFeatures);
		
		Map<Integer, List<Integer>> result = service.getAllFeaturesByCompanyCodes(companyCodes);
		
		assertThat(result).hasSize(2);
		assertThat(result.get(101)).containsExactly(10, 20);
		assertThat(result.get(102)).containsExactly(30);
		verify(mapRepo).findByCompanyIdIn(companyCodes);
	}
	
	@Test
	@DisplayName("Should handle empty features when getting by company codes")
	void shouldHandleEmptyFeaturesWhenGettingByCompanyCodes() {
		// Code Fix: Testing edge case with no features found
		List<Integer> companyCodes = Arrays.asList(101, 102);
		
		when(mapRepo.findByCompanyIdIn(companyCodes)).thenReturn(Collections.emptyList());
		
		Map<Integer, List<Integer>> result = service.getAllFeaturesByCompanyCodes(companyCodes);
		
		assertThat(result).isEmpty();
		verify(mapRepo).findByCompanyIdIn(companyCodes);
	}
	
	// ==================== Company List Tests ====================
	
	@Test
	@DisplayName("Should get company list with default pagination")
	void shouldGetCompanyListWithDefaultPagination() {
		// Code Fix: Testing getCompanyList() with basic pagination functionality
		CompanyRequestDTO request = new CompanyRequestDTO();
		request.setCount(0);
		
		MasterCompany company1 = new MasterCompany();
		company1.setId(101);
		company1.setCompanyName("Test Company 1");
		company1.setParentId(null);
		
		MasterCompany company2 = new MasterCompany();
		company2.setId(102);
		company2.setCompanyName("Test Company 2");
		company2.setParentId(0);
		
		Page<MasterCompany> mockPage = new PageImpl<>(Arrays.asList(company1, company2));
		
		when(companyRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
		
		List<CompanyDTO> result = service.getCompanyList(request);
		
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getCompanyCode()).isEqualTo(101);
		assertThat(result.get(0).getCompanyName()).isEqualTo("Test Company 1");
		assertThat(result.get(1).getCompanyCode()).isEqualTo(102);
		assertThat(result.get(1).getCompanyName()).isEqualTo("Test Company 2");
		verify(companyRepo).findAll(any(Specification.class), any(Pageable.class));
	}
	
	@Test
	@DisplayName("Should get company list with search text filtering")
	void shouldGetCompanyListWithSearchTextFiltering() {
		// Code Fix: Testing getCompanyList() with search functionality
		CompanyRequestDTO request = new CompanyRequestDTO();
		request.setCount(1);
		request.setSearchText("Test");
		
		MasterCompany company = new MasterCompany();
		company.setId(101);
		company.setCompanyName("Test Company");
		company.setParentId(null);
		
		Page<MasterCompany> mockPage = new PageImpl<>(List.of(company));
		
		when(companyRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
		
		List<CompanyDTO> result = service.getCompanyList(request);
		
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getCompanyName()).contains("Test");
		verify(companyRepo).findAll(any(Specification.class), any(Pageable.class));
	}
	
	@Test
	@DisplayName("Should handle null count in company list request")
	void shouldHandleNullCountInCompanyListRequest() {
		// Code Fix: Testing getCompanyList() with null count defaulting to 0
		CompanyRequestDTO request = new CompanyRequestDTO();
		request.setCount(null);
		
		Page<MasterCompany> mockPage = new PageImpl<>(Collections.emptyList());
		
		when(companyRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
		
		List<CompanyDTO> result = service.getCompanyList(request);
		
		assertThat(result).isEmpty();
		verify(companyRepo).findAll(any(Specification.class), any(Pageable.class));
	}
	
	@Test
	@DisplayName("Should handle empty search text in company list")
	void shouldHandleEmptySearchTextInCompanyList() {
		// Code Fix: Testing getCompanyList() with empty search text
		CompanyRequestDTO request = new CompanyRequestDTO();
		request.setSearchText("");
		
		MasterCompany company = new MasterCompany();
		company.setId(101);
		company.setCompanyName("Any Company");
		
		Page<MasterCompany> mockPage = new PageImpl<>(List.of(company));
		
		when(companyRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
		
		List<CompanyDTO> result = service.getCompanyList(request);
		
		assertThat(result).hasSize(1);
		verify(companyRepo).findAll(any(Specification.class), any(Pageable.class));
	}
	
	// ==================== Edge Cases and Additional Scenarios ====================
	
	@Test
	@DisplayName("Should bulk update all companies when isAllSelected is true")
	void shouldBulkUpdateAllCompaniesWhenIsAllSelectedIsTrue() {
		// Code Fix: Testing bulkUpdateSubscriptionTier() with isAllSelected=true functionality
		SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
		request.setSubscriptionTierType(2);
		request.setIsAllSelected(true);
		
		when(domainSyncService.sendBulkCustomerSubscriptionUpdateToDomains(request)).thenReturn(true);
		when(companyRepo.updateSubscriptionTypeForAllCompanies(2)).thenReturn(50);
		
		String result = service.bulkUpdateSubscriptionTier(request);
		
		assertThat(result).contains("50").contains("customers"); // Should pluralize with "customers"
		verify(domainSyncService).sendBulkCustomerSubscriptionUpdateToDomains(request);
		verify(companyRepo).updateSubscriptionTypeForAllCompanies(2);
		verify(companyRepo, never()).updateSubscriptionTypeByCompanyCodes(anyInt(), anyList());
	}
	
	@Test
	@DisplayName("Should handle company not found during single update")
	void shouldHandleCompanyNotFoundDuringSingleUpdate() {
		// Code Fix: Testing updateCustomerSubscription() when company doesn't exist
		SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
		request.setSubscriptionTierType(1);
		request.setCompanyCode(999); // Non-existent company
		
		when(domainSyncService.sendCustomerSubscriptionUpdateToDomains(request)).thenReturn(true);
		when(companyRepo.findById(999)).thenReturn(Optional.empty());
		
		CustomException ex = assertThrows(CustomException.class, () -> service.updateCustomerSubscription(request));
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DATABASE_CONNECTION_ERROR); // Wrapped by catch-all
		verify(domainSyncService).sendCustomerSubscriptionUpdateToDomains(request);
		verify(companyRepo).findById(999);
	}
	
	@Test
	@DisplayName("Should handle null domain sync response")
	void shouldHandleNullDomainSyncResponse() {
		// Code Fix: Testing bulkUpdateSubscriptionTier() when domain sync returns null
		SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
		request.setSubscriptionTierType(1);
		request.setCompanyCodes(List.of(101));
		
		when(domainSyncService.sendBulkCustomerSubscriptionUpdateToDomains(request)).thenReturn(null);
		
		CustomException ex = assertThrows(CustomException.class, () -> service.bulkUpdateSubscriptionTier(request));
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DATABASE_CONNECTION_ERROR); // Wrapped by catch-all
		verify(domainSyncService).sendBulkCustomerSubscriptionUpdateToDomains(request);
	}
	
	// ==================== BulkUpsertFeatures Helper Method Tests ====================
	
	@Test
	@DisplayName("Should execute bulk upsert features with native query")
	void shouldExecuteBulkUpsertFeaturesWithNativeQuery() {
		// Code Fix: Testing bulkUpsertFeatures() helper method with EntityManager native query
		Integer sourceCompanyId = 101;
		List<Integer> targetCompanyIds = Arrays.asList(201, 202);
		Integer createdBy = 1;
		
		// Inject EntityManager using reflection since it's @PersistenceContext
		ReflectionTestUtils.setField(service, "entityManager", entityManager);
		
		when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
		when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
		when(nativeQuery.executeUpdate()).thenReturn(2);
		
		service.bulkUpsertFeatures(sourceCompanyId, targetCompanyIds, createdBy);
		
		verify(entityManager).createNativeQuery(anyString());
		verify(nativeQuery).setParameter("sourceCompanyId", sourceCompanyId);
		verify(nativeQuery).setParameter("createdBy", createdBy);
		verify(nativeQuery).executeUpdate();
	}
	
	@Test
	@DisplayName("Should skip bulk upsert when target company IDs is null")
	void shouldSkipBulkUpsertWhenTargetCompanyIdsIsNull() {
		// Code Fix: Testing bulkUpsertFeatures() early return when targetCompanyIds is null
		Integer sourceCompanyId = 101;
		List<Integer> targetCompanyIds = null;
		Integer createdBy = 1;
		
		service.bulkUpsertFeatures(sourceCompanyId, targetCompanyIds, createdBy);
		
		verify(entityManager, never()).createNativeQuery(anyString());
	}
	
	@Test
	@DisplayName("Should skip bulk upsert when target company IDs is empty")
	void shouldSkipBulkUpsertWhenTargetCompanyIdsIsEmpty() {
		// Code Fix: Testing bulkUpsertFeatures() early return when targetCompanyIds is empty
		Integer sourceCompanyId = 101;
		List<Integer> targetCompanyIds = Collections.emptyList();
		Integer createdBy = 1;
		
		service.bulkUpsertFeatures(sourceCompanyId, targetCompanyIds, createdBy);
		
		verify(entityManager, never()).createNativeQuery(anyString());
	}
	
	@Test
	@DisplayName("Should handle single target company in bulk upsert")
	void shouldHandleSingleTargetCompanyInBulkUpsert() {
		// Code Fix: Testing bulkUpsertFeatures() with single target company
		Integer sourceCompanyId = 101;
		List<Integer> targetCompanyIds = List.of(201);
		Integer createdBy = 1;
		
		// Inject EntityManager using reflection since it's @PersistenceContext
		ReflectionTestUtils.setField(service, "entityManager", entityManager);
		
		when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
		when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
		when(nativeQuery.executeUpdate()).thenReturn(1);
		
		service.bulkUpsertFeatures(sourceCompanyId, targetCompanyIds, createdBy);
		
		verify(entityManager).createNativeQuery(anyString());
		verify(nativeQuery).setParameter("sourceCompanyId", sourceCompanyId);
		verify(nativeQuery).setParameter("createdBy", createdBy);
		verify(nativeQuery).executeUpdate();
	}

}
