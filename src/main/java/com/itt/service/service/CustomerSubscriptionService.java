package com.itt.service.service;

import java.util.List;

import com.itt.service.dto.CompanyRequestDTO;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.customer_subscription.CompanyDTO;
import com.itt.service.dto.customer_subscription.CustomerSubscriptionDTO;
import com.itt.service.dto.customer_subscription.SubscriptionBulkUpdateRequest;
import com.itt.service.dto.customer_subscription.SubscriptionCopyRequest;
import com.itt.service.dto.customer_subscription.SubscriptionUpdateRequest;
import com.itt.service.dto.master.MasterConfigDTO;

public interface CustomerSubscriptionService {
	public PaginationResponse<CustomerSubscriptionDTO> findAll(DataTableRequest request);

	public PaginationResponse<CustomerSubscriptionDTO> findAllWithFeatures(DataTableRequest request);

	public List<MasterConfigDTO> getAllFeaturesSubscriptionTypeFromMasterConfig();

	public List<Integer> getAllFeaturesByCompanyCode(Integer companyCode);

	public String bulkUpdateSubscriptionTier(SubscriptionBulkUpdateRequest request);

	public String updateCustomerSubscription(SubscriptionUpdateRequest request);

	public String copyCustomerSubscription(SubscriptionCopyRequest request);
	
	public List<CompanyDTO> getCompanyList(CompanyRequestDTO request);
}
