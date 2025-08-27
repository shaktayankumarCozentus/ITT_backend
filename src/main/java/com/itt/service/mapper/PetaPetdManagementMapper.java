package com.itt.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.itt.service.dto.peta_petd.PetaPetdDTO;
import com.itt.service.entity.MapCompanyPetaPetd;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetaPetdManagementMapper {
	@Mapping(source = "company.companyName", target = "companyName")
	@Mapping(source = "company.id", target = "companyCode")
	@Mapping(source = "petaPetdEnabledFlag", target = "petaCalling")
	@Mapping(source = "oceanFrequency.id", target = "oceanFrequencyTypeId")
	@Mapping(source = "oceanFrequency.keyCode", target = "oceanFrequencyTypeCode")
	@Mapping(source = "oceanFrequency.name", target = "oceanFrequencyTypeName")
	@Mapping(source = "airFrequency.id", target = "airFrequencyTypeId")
	@Mapping(source = "airFrequency.keyCode", target = "airFrequencyTypeCode")
	@Mapping(source = "airFrequency.name", target = "airFrequencyTypeName")
	@Mapping(source = "railRoadFrequency.id", target = "railRoadFrequencyTypeId")
	@Mapping(source = "railRoadFrequency.keyCode", target = "railRoadFrequencyTypeCode")
	@Mapping(source = "railRoadFrequency.name", target = "railRoadFrequencyTypeName")
	@Mapping(source = "updatedBy.email", target = "updatedBy")
	PetaPetdDTO toDto(MapCompanyPetaPetd company);
}
