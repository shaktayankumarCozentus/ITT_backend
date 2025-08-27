package com.itt.service.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.itt.service.annotation.ReadOnlyDataSource;
import com.itt.service.annotation.WriteDataSource;
import com.itt.service.config.search.CustomerSubscriptionSearchConfig;
import com.itt.service.constants.ErrorMessages;
import com.itt.service.constants.SuccessMessages;
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
import com.itt.service.fw.search.SearchableEntity;
import com.itt.service.mapper.CustomerSubscriptionMapper;
import com.itt.service.repository.MapCompanySubscriptionRepository;
import com.itt.service.repository.MasterCompanyRepository;
import com.itt.service.service.BaseService;
import com.itt.service.service.CustomerSubscriptionService;
import com.itt.service.service.CustomerSubscriptonDomainSyncService;
import com.itt.service.service.MasterDataService;
import com.itt.service.validator.SortFieldValidator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class CustomerSubscriptionServiceImpl extends BaseService<MasterCompany, Integer, CustomerSubscriptionDTO>
		implements CustomerSubscriptionService {
	private final MasterCompanyRepository masterRepository;
	private final MapCompanySubscriptionRepository mapRepository;
	private final MasterDataService masterDataService;
	private final CustomerSubscriptionSearchConfig customerSubscriptionSearchConfig;
	private final CustomerSubscriptonDomainSyncService domainSyncService;

	@PersistenceContext
	private EntityManager entityManager;

	public CustomerSubscriptionServiceImpl(MasterCompanyRepository masterRepository,
			@Qualifier("masterCompanyValidator") SortFieldValidator sortFieldValidator,
			MapCompanySubscriptionRepository mapRepository, MasterDataService masterDataService,
			CustomerSubscriptionMapper mapper, CustomerSubscriptionSearchConfig customerSubscriptionSearchConfig,
			CustomerSubscriptonDomainSyncService domainSyncService) {
		super(masterRepository, sortFieldValidator, mapper::toDto);
		this.masterRepository = masterRepository;
		this.mapRepository = mapRepository;
		this.masterDataService = masterDataService;
		this.customerSubscriptionSearchConfig = customerSubscriptionSearchConfig;
		this.domainSyncService = domainSyncService;
	}

	// ========================================
	// � UNIVERSAL SEARCH FRAMEWORK INTEGRATION
	// ========================================

	@Override
	protected SearchableEntity<MasterCompany> getSearchableEntity() {
		return customerSubscriptionSearchConfig;
	}

	@Override
	public PaginationResponse<CustomerSubscriptionDTO> findAll(DataTableRequest request) {
		// Use Universal Search Framework via BaseService.search() method
		return super.search(request);
	}

	// ========================================
	// 📖 READ OPERATIONS - BaseService provides search with pagination
	// ========================================

	@Override
	public PaginationResponse<CustomerSubscriptionDTO> findAllWithFeatures(DataTableRequest request) {
		// Fetch all companies using Universal Search Framework
		PaginationResponse<CustomerSubscriptionDTO> response = super.search(request);
		// Fetch features for each company
		List<CustomerSubscriptionDTO> companies = response.getContent();
		List<Integer> companyIds = companies.stream().map(CustomerSubscriptionDTO::getCustomerId)
				.collect(Collectors.toList());
		Map<Integer, List<Integer>> companyFeaturesMap = getAllFeaturesByCompanyCodes(companyIds);
		for (CustomerSubscriptionDTO company : companies) {
			company.setFeatureIds(companyFeaturesMap.getOrDefault(company.getCustomerId(), List.of()));
		}
		return response;
	}

	@ReadOnlyDataSource("Get all features and Subscription Types from Master Config")
	@Transactional(readOnly = true)
	public List<MasterConfigDTO> getAllFeaturesSubscriptionTypeFromMasterConfig() {
		return masterDataService.getConfigByTypeIn(List.of("FEATURE_TOGGLE", "SUBSCRIPTION_TYPE"));
	}

	public Map<Integer, List<Integer>> getAllFeaturesByCompanyCodes(List<Integer> companyCodes) {
		return mapRepository.findByCompanyIdIn(companyCodes).stream()
				.collect(Collectors.groupingBy(MapCompanySubscription::getCompanyId,
						Collectors.mapping(MapCompanySubscription::getFeatureToggleId, Collectors.toList())));
	}

	@ReadOnlyDataSource("Get all features by company code")
	@Transactional(readOnly = true)
	public List<Integer> getAllFeaturesByCompanyCode(Integer companyCode) {
		return mapRepository.findByCompanyId(companyCode).stream().map(MapCompanySubscription::getFeatureToggleId)
				.collect(Collectors.toList());
	}

	@WriteDataSource("Bulk subscription tier update")
	@Transactional(rollbackFor = Exception.class)
	public String bulkUpdateSubscriptionTier(SubscriptionBulkUpdateRequest request) {
		try {
			Boolean isSent = domainSyncService.sendBulkCustomerSubscriptionUpdateToDomains(request);
			if (isSent == null || !isSent) {
				throw new CustomException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
						ErrorMessages.BULK_SUBSCRIPTION_UPDATE_FAILURE);
			}
			Integer noOfRowsAffected;
			if (Boolean.TRUE.equals(request.getIsAllSelected())) {
				noOfRowsAffected = masterRepository.updateSubscriptionTypeForAllCompanies(request.getSubscriptionTierType());
			} else {
				noOfRowsAffected = masterRepository.updateSubscriptionTypeByCompanyCodes(
						request.getSubscriptionTierType(), request.getCompanyCodes());
			}
			String plural = noOfRowsAffected == 1 ? "" : "s";
			return String.format(SuccessMessages.BULK_SUBSCRIPTION_UPDATE_SUCCESSFUL, noOfRowsAffected, plural);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.DATABASE_CONNECTION_ERROR,
					ErrorMessages.BULK_SUBSCRIPTION_UPDATE_FAILURE, e);
		}
	}

	@WriteDataSource("Single subscription tier update")
	@Transactional(rollbackFor = Exception.class)
	public String updateCustomerSubscription(SubscriptionUpdateRequest request) {
		try {
			Boolean isSent = domainSyncService.sendCustomerSubscriptionUpdateToDomains(request);
			if (isSent == null || !isSent) {
				throw new CustomException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
						ErrorMessages.SUBSCRIPTION_UPDATE_FAILURE);
			}
			MasterCompany company = masterRepository.findById(request.getCompanyCode()).orElseThrow(
					() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND));
			masterRepository.updateSubscriptionTypeByCompanyCode(request.getSubscriptionTierType(),
					request.getCompanyCode());
			if (!CollectionUtils.isEmpty(request.getFeatureIds())) {
				mapRepository.deleteByCompanyId(request.getCompanyCode());
				List<MapCompanySubscription> features = request.getFeatureIds().stream()
						.map(featureId -> new MapCompanySubscription(request.getCompanyCode(), featureId, 1, 1, 1))
						.collect(Collectors.toList());
				mapRepository.saveAll(features);
			}
			return String.format(SuccessMessages.SUBSCRIPTION_UPDATE_SUCCESSFUL, company.getCompanyName());
		} catch (Exception e) {
			throw new CustomException(ErrorCode.DATABASE_CONNECTION_ERROR, ErrorMessages.SUBSCRIPTION_UPDATE_FAILURE,
					e);
		}
	}

	@WriteDataSource("Copy subscription features between companies")
	@Transactional(rollbackFor = Exception.class)
	public String copyCustomerSubscription(SubscriptionCopyRequest request) {
		try {
			Integer subscriptionType = masterRepository.getSubscriptionTypeByCompanyCode(request.getSourceCompanyId());
			Integer noOfRowsAffected = masterRepository.copyCustomerSubscription(request.getSourceCompanyId(),
					request.getTargetCompanyIds(), subscriptionType);
			String plural = noOfRowsAffected == 1 ? "" : "s";
			bulkUpsertFeatures(request.getSourceCompanyId(), request.getTargetCompanyIds(), 0);

			return String.format(SuccessMessages.SUBSCRIPTION_FEATURES_COPY_SUCCESSFUL, noOfRowsAffected, plural);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.DATABASE_CONNECTION_ERROR,
					ErrorMessages.SUBSCRIPTION_FEATURES_COPY_FAILURE, e);
		}
	}

	/**
	 * Helper method to bulk upsert features for multiple companies. This method
	 * inherits the datasource context from the calling method.
	 * 
	 * @param sourceCompanyId  The source company to copy features from
	 * @param targetCompanyIds List of target companies to copy features to
	 * @param createdBy        User who initiated this operation
	 */
	public void bulkUpsertFeatures(Integer sourceCompanyId, List<Integer> targetCompanyIds, Integer createdBy) {
		if (targetCompanyIds == null || targetCompanyIds.isEmpty()) {
			return;
		}

		String unionTargets = targetCompanyIds.stream().map(id -> "SELECT " + id + " AS target_company_id")
				.collect(Collectors.joining(" UNION ALL "));

		String sql = """
				    INSERT INTO map_company_subscription (
				        company_id, feature_toggle_id, is_active, created_on, created_by_id
				    )
				    SELECT
				        t.target_company_id,
				        s.feature_toggle_id,
				        s.is_active,
				        NOW(),
				        :createdBy
				    FROM map_company_subscription s
				    JOIN (
				        %s
				    ) t ON 1=1
				    WHERE s.company_id = :sourceCompanyId
				    ON DUPLICATE KEY UPDATE
				        is_active = VALUES(is_active),
				        updated_on = NOW(),
				        updated_by_id = :createdBy;
				""".formatted(unionTargets);

		entityManager.createNativeQuery(sql).setParameter("sourceCompanyId", sourceCompanyId)
				.setParameter("createdBy", createdBy).executeUpdate();
	}

	@Override
	public List<CompanyDTO> getCompanyList(CompanyRequestDTO request) {
		Integer page = request.getCount() != null ? request.getCount() : 0;
		Integer size = 10;
		Pageable pageable = Pageable.ofSize(size).withPage(page);

		Specification<MasterCompany> filterSpec = (root, query, cb) -> cb.or(cb.isNull(root.get("parentId")),
				cb.equal(root.get("parentId"), 0));

		if (request.getSearchText() != null && !request.getSearchText().isEmpty()) {
			String searchText = request.getSearchText();
			String likeText = "%" + searchText.toLowerCase() + "%";
			Specification<MasterCompany> searchSpec = (root, query, cb) -> cb.or(
					cb.like(cb.lower(cb.concat(root.get("id").as(String.class), cb.literal(""))), likeText),
					cb.like(cb.lower(root.get("companyName")), likeText));
			filterSpec = filterSpec.and(searchSpec);
		}

		List<MasterCompany> companies = masterRepository.findAll(filterSpec, pageable).getContent();
		return companies.stream().map(company -> new CompanyDTO(company.getId(), company.getCompanyName()))
				.collect(Collectors.toList());
	}
}