package com.itt.service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.itt.service.annotation.ReadOnlyDataSource;
import com.itt.service.dto.CompanyDTO;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.repository.MasterCompanyRepository;
import com.itt.service.repository.MasterConfigRepository;
import com.itt.service.service.MasterDataService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MasterDataServiceImpl implements MasterDataService {

	private final MasterCompanyRepository masterCompanyRepository;
	private final MasterConfigRepository masterConfigRepository;

	@ReadOnlyDataSource("Parent companies for dropdown")
	public List<CompanyDTO> getParentCompanies() {
		return masterCompanyRepository.getParentCompanies().stream()
				.map(company -> new CompanyDTO(company.getId(), company.getCompanyName())).toList();
	}

	@ReadOnlyDataSource("Config lookup by type")
	public List<MasterConfigDTO> getConfigByType(String configType) {
		return masterConfigRepository.findByConfigType(configType).stream()
				.map(config -> new MasterConfigDTO(config.getId(), config.getConfigType(), config.getKeyCode(),
						config.getName(), config.getDescription(), config.getIntValue(), config.getStringValue()))
				.toList();
	}

	@ReadOnlyDataSource("Config lookup by key")
	public List<MasterConfigDTO> getConfigByKey(String keyCode) {
		return masterConfigRepository.findByKeyCode(keyCode).stream()
				.map(config -> new MasterConfigDTO(config.getId(), config.getConfigType(), config.getKeyCode(),
						config.getName(),
						config.getDescription(), config.getIntValue(), config.getStringValue()))
				.toList();
	}

	@Override
	public List<MasterConfigDTO> getConfigByTypeIn(List<String> configTypes) {
		return masterConfigRepository.findByConfigTypes(configTypes).stream()
				.map(config -> new MasterConfigDTO(config.getId(), config.getConfigType(), config.getKeyCode(),
						config.getName(), config.getDescription(), config.getIntValue(), config.getStringValue()))
				.toList();
	}

	@Override
	public MasterConfigDTO getConfigById(Integer id) {
	    var config = masterConfigRepository.findById(id);
	    return config.map(c -> new MasterConfigDTO(c.getId(), c.getConfigType(), c.getKeyCode(), 
	        c.getName(), c.getDescription(), c.getIntValue(), c.getStringValue())).orElse(null);
	}
}
