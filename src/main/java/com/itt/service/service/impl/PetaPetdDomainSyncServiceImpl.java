package com.itt.service.service.impl;

import org.springframework.stereotype.Service;

import com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest;
import com.itt.service.dto.peta_petd.PetaPetdUpdateRequest;
import com.itt.service.service.PetaPetdDomainSyncService;

@Service
public class PetaPetdDomainSyncServiceImpl implements PetaPetdDomainSyncService {
	public Boolean sendPetaPetdUpdateToDomains(PetaPetdUpdateRequest request) {
		if (!sendUpdateToOceanDomain(request) || !sendUpdateToAirDomain(request)
				|| !sendUpdateToRailRoadDomain(request)) {
			return false;
		}
		return true;
	}

	public Boolean sendBulkPetaPetdUpdateToDomains(PetaPetdBulkUpdateRequest request) {
		if (!sendBulkUpdateToOceanDomain(request) || !sendBulkUpdateToAirDomain(request)
				|| !sendBulkUpdateToRailRoadDomain(request)) {
			return false;
		}
		return true;
	}

	private Boolean sendUpdateToRailRoadDomain(PetaPetdUpdateRequest request) {
		return true;
	}

	private Boolean sendUpdateToAirDomain(PetaPetdUpdateRequest request) {
		return true;
	}

	private Boolean sendUpdateToOceanDomain(PetaPetdUpdateRequest request) {
		return true;
	}

	private boolean sendBulkUpdateToRailRoadDomain(PetaPetdBulkUpdateRequest request) {
		return true;
	}

	private boolean sendBulkUpdateToAirDomain(PetaPetdBulkUpdateRequest request) {
		return true;
	}

	private boolean sendBulkUpdateToOceanDomain(PetaPetdBulkUpdateRequest request) {
		return true;
	}
}
