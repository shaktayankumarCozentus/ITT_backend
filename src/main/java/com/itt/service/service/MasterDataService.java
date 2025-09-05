package com.itt.service.service;

import java.util.List;

import com.itt.service.dto.CompanyDTO;
import com.itt.service.dto.master.MasterConfigDTO;

public interface MasterDataService {
	public List<CompanyDTO> getParentCompanies();

	public List<MasterConfigDTO> getConfigByType(String configType);

	public List<MasterConfigDTO> getConfigByTypeIn(List<String> configTypes);

	public List<MasterConfigDTO> getConfigByKey(String keyCode);

	public MasterConfigDTO getConfigById(Integer id);
}
