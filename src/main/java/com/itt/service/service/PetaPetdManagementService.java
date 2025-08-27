package com.itt.service.service;

import java.util.List;

import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest;
import com.itt.service.dto.peta_petd.PetaPetdDTO;
import com.itt.service.dto.peta_petd.PetaPetdUpdateRequest;

public interface PetaPetdManagementService {

	public PaginationResponse<PetaPetdDTO> findAll(DataTableRequest request);

	public List<MasterConfigDTO> getAllPetaPetdConfigs();

	public String bulkUpdatePetaPetd(PetaPetdBulkUpdateRequest request);

	public String updatePetaPetd(PetaPetdUpdateRequest request);
}
