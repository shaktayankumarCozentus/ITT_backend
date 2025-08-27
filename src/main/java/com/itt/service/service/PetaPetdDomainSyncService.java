package com.itt.service.service;

import com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest;
import com.itt.service.dto.peta_petd.PetaPetdUpdateRequest;

public interface PetaPetdDomainSyncService {
	Boolean sendPetaPetdUpdateToDomains(PetaPetdUpdateRequest request);

	Boolean sendBulkPetaPetdUpdateToDomains(PetaPetdBulkUpdateRequest request);
}
