package com.itt.service.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.itt.service.config.search.PetaPetdSearchConfig;
import com.itt.service.constants.ErrorMessages;
import com.itt.service.constants.SuccessMessages;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest;
import com.itt.service.dto.peta_petd.PetaPetdDTO;
import com.itt.service.dto.peta_petd.PetaPetdUpdateRequest;
import com.itt.service.entity.MapCompanyPetaPetd;
import com.itt.service.entity.MasterCompany;
import com.itt.service.enums.ErrorCode;
import com.itt.service.exception.CustomException;
import com.itt.service.fw.search.SearchableEntity;
import com.itt.service.mapper.PetaPetdManagementMapper;
import com.itt.service.repository.MapCompanyPetaPetdRepository;
import com.itt.service.repository.MasterCompanyRepository;
import com.itt.service.service.BaseService;
import com.itt.service.service.MasterDataService;
import com.itt.service.service.PetaPetdDomainSyncService;
import com.itt.service.service.PetaPetdManagementService;
import com.itt.service.validator.SortFieldValidator;

@Service
public class PetaPetdManagementServiceImpl extends BaseService<MapCompanyPetaPetd, Integer, PetaPetdDTO>
		implements PetaPetdManagementService {

	private final MasterDataService masterDataService;
	private final PetaPetdSearchConfig petaPetdSearchConfig;
	private final MapCompanyPetaPetdRepository repository;
	private final MasterCompanyRepository companyRepository;
	private final PetaPetdDomainSyncService domainSyncService;

	public PetaPetdManagementServiceImpl(MapCompanyPetaPetdRepository repository,
			MasterCompanyRepository companyRepository,
			@Qualifier("mapCompanyPetaPetdValidator") SortFieldValidator sortFieldValidator,
			MasterDataService masterDataService, PetaPetdManagementMapper mapper,
			PetaPetdSearchConfig petaPetdSearchConfig, PetaPetdDomainSyncService domainSyncService) {
		super(repository, sortFieldValidator, mapper::toDto);
		this.repository = repository;
		this.companyRepository = companyRepository;
		this.masterDataService = masterDataService;
		this.petaPetdSearchConfig = petaPetdSearchConfig;
		this.domainSyncService = domainSyncService;
	}

	@Override
	protected SearchableEntity<MapCompanyPetaPetd> getSearchableEntity() {
		return petaPetdSearchConfig;
	}

	@Override
	public PaginationResponse<PetaPetdDTO> findAll(DataTableRequest request) {
		return super.search(request);
	}

	@Override
	public List<MasterConfigDTO> getAllPetaPetdConfigs() {
		return masterDataService.getConfigByType("PETA_PETD_FREQUENCY");
	}

	@Override
	public String bulkUpdatePetaPetd(PetaPetdBulkUpdateRequest request) {
		try {
			Boolean isSent = domainSyncService.sendBulkPetaPetdUpdateToDomains(request);
			if (isSent == null || !isSent) {
				throw new CustomException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
						ErrorMessages.BULK_PETA_PETD_UPDATE_FAILURE);
			}
			Integer noOfRowsAffected;
			if (Boolean.TRUE.equals(request.getIsAllSelected())) {
				noOfRowsAffected = repository.updatePetaPetdCallingForAllCompanies(request.getPetaCalling());
			} else {
				noOfRowsAffected = repository.updatePetaPetdCallingByCompanyCodes(request.getPetaCalling(),
						request.getCompanyCodes());
			}
			String plural = noOfRowsAffected == 1 ? "" : "s";
			return String.format(SuccessMessages.BULK_PETA_PETD_UPDATE_SUCCESSFUL, noOfRowsAffected, plural);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.DATABASE_CONNECTION_ERROR, ErrorMessages.BULK_PETA_PETD_UPDATE_FAILURE,
					e);
		}
	}

	@Override
	public String updatePetaPetd(PetaPetdUpdateRequest request) {
		try {
			Boolean isSent = domainSyncService.sendPetaPetdUpdateToDomains(request);
			if (isSent == null || !isSent) {
				throw new CustomException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
						ErrorMessages.PETA_PETD_UPDATE_FAILURE);
			}
			MasterCompany company = companyRepository.findById(request.getCompanyCode()).orElseThrow(
					() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND));
			Integer noOfRowsAffected = repository.updatePetaPetdFrequencyByCompanyCodes(request.getPetaCalling(),
					request.getOceanFrequencyType(), request.getAirFrequencyType(), request.getRailRoadFrequencyType(),
					request.getCompanyCode());
			if (noOfRowsAffected == 0) {
				throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, ErrorMessages.ENTITY_NOT_FOUND);
			}
			return String.format(SuccessMessages.PETA_PETD_UPDATE_SUCCESSFUL, company.getCompanyName());
		} catch (Exception e) {
			throw new CustomException(ErrorCode.DATABASE_CONNECTION_ERROR, ErrorMessages.PETA_PETD_UPDATE_FAILURE, e);
		}
	}

}
