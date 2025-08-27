package com.itt.service.service;

import com.itt.service.dto.customer_subscription.SubscriptionBulkUpdateRequest;
import com.itt.service.dto.customer_subscription.SubscriptionCopyRequest;
import com.itt.service.dto.customer_subscription.SubscriptionUpdateRequest;

public interface CustomerSubscriptonDomainSyncService {
	Boolean sendCustomerSubscriptionUpdateToDomains(SubscriptionUpdateRequest request);

	Boolean sendBulkCustomerSubscriptionUpdateToDomains(SubscriptionBulkUpdateRequest request);

	Boolean sendCopyCustomerSubscriptionToDomains(SubscriptionCopyRequest request);
}