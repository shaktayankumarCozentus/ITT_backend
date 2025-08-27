package com.itt.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itt.service.dto.CompanyDTO;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.entity.MasterCompany;
import com.itt.service.entity.MasterConfig;
import com.itt.service.repository.MasterCompanyRepository;
import com.itt.service.repository.MasterConfigRepository;
import com.itt.service.service.impl.MasterDataServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("MasterDataService - Business Logic Tests")
class MasterDataServiceImplTest {

	@Mock
	private MasterConfigRepository masterConfigRepo;

	@Mock
	private MasterCompanyRepository masterCompanyRepository;

	@InjectMocks
	private MasterDataServiceImpl masterDataService;

	@Nested
	@DisplayName("Get Configs By Type Tests")
	class GetConfigsByTypeTests {

		@Test
		@DisplayName("Should return configs for valid type")
		void shouldReturnConfigsForValidType() {
			// Given
			List<MasterConfig> configs = Arrays.asList(createMasterConfig(1L, "TYPE_A", "Config A"),
					createMasterConfig(2L, "TYPE_A", "Config B"));
			when(masterConfigRepo.findByConfigType("TYPE_A")).thenReturn(configs);

			// When
			List<MasterConfigDTO> result = masterDataService.getConfigByType("TYPE_A");

			// Then
			assertThat(result).isNotNull();
			assertThat(result).hasSize(2);
			assertThat(result.get(0).getName()).isEqualTo("Config A");
			verify(masterConfigRepo).findByConfigType("TYPE_A");
		}

		@Test
		@DisplayName("Should handle empty configs for unknown type")
		void shouldHandleEmptyConfigsForUnknownType() {
			// Given
			when(masterConfigRepo.findByConfigType("UNKNOWN")).thenReturn(Collections.emptyList());

			// When
			List<MasterConfigDTO> result = masterDataService.getConfigByType("UNKNOWN");

			// Then
			assertThat(result).isNotNull();
			assertThat(result).isEmpty();
			verify(masterConfigRepo).findByConfigType("UNKNOWN");
		}
	}

	@Nested
	@DisplayName("Get Configs By Key Tests")
	class GetConfigsByTKeyTests {

		@Test
		@DisplayName("Should return configs for valid type")
		void shouldReturnConfigsForValidKey() {
			// Given
			List<MasterConfig> configs = Arrays.asList(createMasterConfig(1L, "TYPE_A", "Config A"),
					createMasterConfig(2L, "TYPE_A", "Config B"));
			when(masterConfigRepo.findByKeyCode("TYPE_A")).thenReturn(configs);

			// When
			List<MasterConfigDTO> result = masterDataService.getConfigByKey("TYPE_A");

			// Then
			assertThat(result).isNotNull();
			assertThat(result).hasSize(2);
			assertThat(result.get(0).getName()).isEqualTo("Config A");
			verify(masterConfigRepo).findByKeyCode("TYPE_A");
		}

		@Test
		@DisplayName("Should handle empty configs for unknown type")
		void shouldHandleEmptyConfigsForUnknownKey() {
			// Given
			when(masterConfigRepo.findByKeyCode("UNKNOWN")).thenReturn(Collections.emptyList());

			// When
			List<MasterConfigDTO> result = masterDataService.getConfigByKey("UNKNOWN");

			// Then
			assertThat(result).isNotNull();
			assertThat(result).isEmpty();
			verify(masterConfigRepo).findByKeyCode("UNKNOWN");
		}
	}

	@Test
	@DisplayName("Should return mapped parent companies")
	void shouldReturnMappedParentCompanies() {
		MasterCompany company1 = new MasterCompany();
		company1.setId(101);
		company1.setCompanyName("Company A");

		MasterCompany company2 = new MasterCompany();
		company2.setId(102);
		company2.setCompanyName("Company B");

		when(masterCompanyRepository.getParentCompanies()).thenReturn(Arrays.asList(company1, company2));

		List<CompanyDTO> result = masterDataService.getParentCompanies();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getCompanyCode()).isEqualTo(101);
		assertThat(result.get(0).getCompanyName()).isEqualTo("Company A");
		assertThat(result.get(1).getCompanyCode()).isEqualTo(102);
		assertThat(result.get(1).getCompanyName()).isEqualTo("Company B");

		verify(masterCompanyRepository).getParentCompanies();
	}

	@Test
	@DisplayName("Should return empty list when no parent companies found")
	void shouldReturnEmptyListWhenNoParentCompaniesFound() {
		when(masterCompanyRepository.getParentCompanies()).thenReturn(Collections.emptyList());

		List<CompanyDTO> result = masterDataService.getParentCompanies();

		assertThat(result).isEmpty();
		verify(masterCompanyRepository).getParentCompanies();
	}

	// ==================== Get Configs By Types (Multiple) Tests ====================

	@Test
	@DisplayName("Should return configs for multiple valid types")
	void shouldReturnConfigsForMultipleValidTypes() {
		// Code Fix: Testing getConfigByTypeIn() method that was missing coverage
		List<String> configTypes = Arrays.asList("FEATURE_TOGGLE", "SUBSCRIPTION_TYPE");
		
		MasterConfig feature1 = createMasterConfig(1L, "FEATURE_1", "Feature Toggle 1");
		feature1.setConfigType("FEATURE_TOGGLE");
		
		MasterConfig feature2 = createMasterConfig(2L, "FEATURE_2", "Feature Toggle 2");
		feature2.setConfigType("FEATURE_TOGGLE");
		
		MasterConfig subscription1 = createMasterConfig(3L, "BASIC", "Basic Subscription");
		subscription1.setConfigType("SUBSCRIPTION_TYPE");
		
		List<MasterConfig> mockConfigs = Arrays.asList(feature1, feature2, subscription1);
		
		when(masterConfigRepo.findByConfigTypes(configTypes)).thenReturn(mockConfigs);
		
		List<MasterConfigDTO> result = masterDataService.getConfigByTypeIn(configTypes);
		
		assertThat(result).isNotNull();
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getName()).isEqualTo("Feature Toggle 1");
		assertThat(result.get(0).getConfigType()).isEqualTo("FEATURE_TOGGLE");
		assertThat(result.get(1).getName()).isEqualTo("Feature Toggle 2");
		assertThat(result.get(2).getName()).isEqualTo("Basic Subscription");
		assertThat(result.get(2).getConfigType()).isEqualTo("SUBSCRIPTION_TYPE");
		
		verify(masterConfigRepo).findByConfigTypes(configTypes);
	}
	
	@Test
	@DisplayName("Should return empty list for unknown config types")
	void shouldReturnEmptyListForUnknownConfigTypes() {
		// Code Fix: Testing getConfigByTypeIn() with non-existent config types
		List<String> unknownTypes = Arrays.asList("UNKNOWN_TYPE", "INVALID_TYPE");
		
		when(masterConfigRepo.findByConfigTypes(unknownTypes)).thenReturn(Collections.emptyList());
		
		List<MasterConfigDTO> result = masterDataService.getConfigByTypeIn(unknownTypes);
		
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
		verify(masterConfigRepo).findByConfigTypes(unknownTypes);
	}
	
	@Test
	@DisplayName("Should handle single config type in list")
	void shouldHandleSingleConfigTypeInList() {
		// Code Fix: Testing getConfigByTypeIn() with single type in list
		List<String> singleType = Arrays.asList("SINGLE_TYPE");
		
		MasterConfig config = createMasterConfig(1L, "KEY_1", "Single Config");
		config.setConfigType("SINGLE_TYPE");
		
		when(masterConfigRepo.findByConfigTypes(singleType)).thenReturn(Arrays.asList(config));
		
		List<MasterConfigDTO> result = masterDataService.getConfigByTypeIn(singleType);
		
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Single Config");
		assertThat(result.get(0).getConfigType()).isEqualTo("SINGLE_TYPE");
		verify(masterConfigRepo).findByConfigTypes(singleType);
	}
	
	@Test
	@DisplayName("Should handle empty config types list")
	void shouldHandleEmptyConfigTypesList() {
		// Code Fix: Testing getConfigByTypeIn() with empty list
		List<String> emptyTypes = Collections.emptyList();
		
		when(masterConfigRepo.findByConfigTypes(emptyTypes)).thenReturn(Collections.emptyList());
		
		List<MasterConfigDTO> result = masterDataService.getConfigByTypeIn(emptyTypes);
		
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
		verify(masterConfigRepo).findByConfigTypes(emptyTypes);
	}
	
	// ==================== Additional Edge Cases for Better Coverage ====================
	
	@Test
	@DisplayName("Should handle null values in parent companies mapping")
	void shouldHandleNullValuesInParentCompaniesMapping() {
		// Code Fix: Testing getParentCompanies() with companies having null values
		MasterCompany company1 = new MasterCompany();
		company1.setId(101);
		company1.setCompanyName(null); // Null company name
		
		MasterCompany company2 = new MasterCompany();
		company2.setId(null); // Null ID
		company2.setCompanyName("Valid Name");
		
		when(masterCompanyRepository.getParentCompanies()).thenReturn(Arrays.asList(company1, company2));
		
		List<CompanyDTO> result = masterDataService.getParentCompanies();
		
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getCompanyCode()).isEqualTo(101);
		assertThat(result.get(0).getCompanyName()).isNull();
		assertThat(result.get(1).getCompanyCode()).isNull();
		assertThat(result.get(1).getCompanyName()).isEqualTo("Valid Name");
		verify(masterCompanyRepository).getParentCompanies();
	}
	
	@Test
	@DisplayName("Should handle configs with all field variations")
	void shouldHandleConfigsWithAllFieldVariations() {
		// Code Fix: Testing config mapping with various field combinations
		MasterConfig config1 = new MasterConfig();
		config1.setId(1);
		config1.setConfigType("TYPE_A");
		config1.setKeyCode("KEY_A");
		config1.setName("Config A");
		config1.setDescription("Description A");
		config1.setIntValue(100);
		config1.setStringValue("String A");
		
		MasterConfig config2 = new MasterConfig();
		config2.setId(2);
		config2.setConfigType("TYPE_A");
		config2.setKeyCode(null); // Null key
		config2.setName(null); // Null name
		config2.setDescription(null); // Null description
		config2.setIntValue(null); // Null int value
		config2.setStringValue(null); // Null string value
		
		when(masterConfigRepo.findByConfigType("TYPE_A")).thenReturn(Arrays.asList(config1, config2));
		
		List<MasterConfigDTO> result = masterDataService.getConfigByType("TYPE_A");
		
		assertThat(result).hasSize(2);
		
		// Verify first config with all fields
		MasterConfigDTO dto1 = result.get(0);
		assertThat(dto1.getId()).isEqualTo(1);
		assertThat(dto1.getConfigType()).isEqualTo("TYPE_A");
		assertThat(dto1.getKeyCode()).isEqualTo("KEY_A");
		assertThat(dto1.getName()).isEqualTo("Config A");
		assertThat(dto1.getDescription()).isEqualTo("Description A");
		assertThat(dto1.getIntValue()).isEqualTo(100);
		assertThat(dto1.getStringValue()).isEqualTo("String A");
		
		// Verify second config with null fields
		MasterConfigDTO dto2 = result.get(1);
		assertThat(dto2.getId()).isEqualTo(2);
		assertThat(dto2.getConfigType()).isEqualTo("TYPE_A");
		assertThat(dto2.getKeyCode()).isNull();
		assertThat(dto2.getName()).isNull();
		assertThat(dto2.getDescription()).isNull();
		assertThat(dto2.getIntValue()).isNull();
		assertThat(dto2.getStringValue()).isNull();
		
		verify(masterConfigRepo).findByConfigType("TYPE_A");
	}
	
	@Test
	@DisplayName("Should handle configs by key with mixed data")
	void shouldHandleConfigsByKeyWithMixedData() {
		// Code Fix: Testing getConfigByKey() with realistic data variations
		MasterConfig config1 = createMasterConfig(10L, "COMMON_KEY", "First Config");
		config1.setConfigType("TYPE_X");
		config1.setDescription("First Description");
		config1.setIntValue(42);
		
		MasterConfig config2 = createMasterConfig(20L, "COMMON_KEY", "Second Config");
		config2.setConfigType("TYPE_Y");
		config2.setStringValue("Custom Value");
		
		when(masterConfigRepo.findByKeyCode("COMMON_KEY")).thenReturn(Arrays.asList(config1, config2));
		
		List<MasterConfigDTO> result = masterDataService.getConfigByKey("COMMON_KEY");
		
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getName()).isEqualTo("First Config");
		assertThat(result.get(0).getConfigType()).isEqualTo("TYPE_X");
		assertThat(result.get(0).getIntValue()).isEqualTo(42);
		assertThat(result.get(1).getName()).isEqualTo("Second Config");
		assertThat(result.get(1).getConfigType()).isEqualTo("TYPE_Y");
		assertThat(result.get(1).getStringValue()).isEqualTo("Custom Value");
		
		verify(masterConfigRepo).findByKeyCode("COMMON_KEY");
	}

	// ==================== Helper Methods ====================

	private MasterConfig createMasterConfig(Long id, String keyCode, String name) {
		MasterConfig config = new MasterConfig();
		config.setId(id.intValue());
		config.setKeyCode(keyCode);
		config.setName(name);
		return config;
	}
}
