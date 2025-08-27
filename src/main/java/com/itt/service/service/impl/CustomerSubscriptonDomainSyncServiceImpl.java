package com.itt.service.service.impl;

import org.springframework.stereotype.Service;

import com.itt.service.dto.customer_subscription.SubscriptionBulkUpdateRequest;
import com.itt.service.dto.customer_subscription.SubscriptionCopyRequest;
import com.itt.service.dto.customer_subscription.SubscriptionUpdateRequest;
import com.itt.service.service.CustomerSubscriptonDomainSyncService;

@Service
public class CustomerSubscriptonDomainSyncServiceImpl implements CustomerSubscriptonDomainSyncService {
	@Override
	public Boolean sendCustomerSubscriptionUpdateToDomains(SubscriptionUpdateRequest request) {
		if (!sendUpdateToOceanDomain(request) || !sendUpdateToAirDomain(request)
				|| !sendUpdateToRailRoadDomain(request)) {
			return false;
		}
		return true;
	}

	@Override
	public Boolean sendBulkCustomerSubscriptionUpdateToDomains(SubscriptionBulkUpdateRequest request) {
		if (!sendBulkUpdateToOceanDomain(request) || !sendBulkUpdateToAirDomain(request)
				|| !sendBulkUpdateToRailRoadDomain(request)) {
			return false;
		}
		return true;
	}

	@Override
	public Boolean sendCopyCustomerSubscriptionToDomains(SubscriptionCopyRequest request) {
		if (!sendCopyToOceanDomain(request) || !sendCopyToAirDomain(request) || !sendCopyToRailRoadDomain(request)) {
			return false;
		}
		return true;
	}

	private Boolean sendUpdateToRailRoadDomain(SubscriptionUpdateRequest request) {
		return true;
	}

	private Boolean sendUpdateToAirDomain(SubscriptionUpdateRequest request) {
		return true;
	}

	private Boolean sendUpdateToOceanDomain(SubscriptionUpdateRequest request) {
		return true;
	}

	private boolean sendBulkUpdateToRailRoadDomain(SubscriptionBulkUpdateRequest request) {
		return true;
	}

	private boolean sendBulkUpdateToAirDomain(SubscriptionBulkUpdateRequest request) {
		return true;
	}

	private boolean sendBulkUpdateToOceanDomain(SubscriptionBulkUpdateRequest request) {
		return true;
	}

	private boolean sendCopyToRailRoadDomain(SubscriptionCopyRequest request) {
		return true;
	}

	private boolean sendCopyToAirDomain(SubscriptionCopyRequest request) {
		return true;
	}

	private boolean sendCopyToOceanDomain(SubscriptionCopyRequest request) {
		return true;
	}
}