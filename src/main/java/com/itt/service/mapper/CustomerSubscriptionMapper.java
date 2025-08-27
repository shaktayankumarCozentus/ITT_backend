package com.itt.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.itt.service.dto.customer_subscription.CustomerSubscriptionDTO;
import com.itt.service.entity.MasterCompany;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerSubscriptionMapper {

	@Mapping(source = "createdOn", target = "onboardedOn")
	@Mapping(source = "id", target = "customerCode")
	@Mapping(source = "id", target = "customerId")
	@Mapping(source = "companyName", target = "customerName")
	@Mapping(source = "subscriptionTypeConfig.id", target = "subscriptionTypeId")
	@Mapping(source = "subscriptionTypeConfig.keyCode", target = "subscriptionTypeKeyCode")
	@Mapping(source = "subscriptionTypeConfig.name", target = "subscriptionTypeName")
	@Mapping(source = "onboardedBySource.id", target = "onboardedSourceId")
	@Mapping(source = "onboardedBySource.keyCode", target = "onboardedSourceKeyCode")
	@Mapping(source = "onboardedBySource.name", target = "onboardedSourceName")
	@Mapping(source = "updatedBy.email", target = "updatedBy")
	@Mapping(source = "createdBy.email", target = "createdBy")
	CustomerSubscriptionDTO toDto(MasterCompany company);
}