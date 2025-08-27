package com.itt.service.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.itt.service.entity.MasterCompany;

@Repository
public interface MasterCompanyRepository extends BaseRepository<MasterCompany, Integer> {
	@EntityGraph(value = "MasterCompany.withSubscriptionAndOnboarded", type = EntityGraph.EntityGraphType.LOAD)
	Page<MasterCompany> findAll(Specification<MasterCompany> spec, Pageable pageable);

	@Modifying
	@Transactional
	@Query("UPDATE MasterCompany m SET m.subscriptionTypeConfigId = :subscriptionType WHERE m.id IN :companyIds")
	Integer updateSubscriptionTypeByCompanyCodes(Integer subscriptionType, List<Integer> companyIds);
	
	@Modifying
	@Transactional
	@Query("UPDATE MasterCompany m SET m.subscriptionTypeConfigId = :subscriptionType WHERE m.parentId IS NULL OR m.parentId = 0")
	Integer updateSubscriptionTypeForAllCompanies(Integer subscriptionType);

	@Modifying
	@Transactional
	@Query("UPDATE MasterCompany m SET m.subscriptionTypeConfigId = :subscriptionType WHERE m.id = :companyId")
	Integer updateSubscriptionTypeByCompanyCode(Integer subscriptionType, Integer companyId);

	@Modifying
	@Transactional
	@Query("UPDATE MasterCompany m SET m.subscriptionTypeConfigId = :subscriptionType WHERE m.id IN :targetCompanyIds")
	Integer copyCustomerSubscription(Integer sourceCompanyId, List<Integer> targetCompanyIds, Integer subscriptionType);

	@Query("SELECT m FROM MasterCompany m WHERE m.parentId IS NULL OR m.parentId = 0")
	List<MasterCompany> getParentCompanies();
	
	@Query("SELECT mc.subscriptionTypeConfigId FROM MasterCompany mc WHERE mc.id = :companyId")
	Integer getSubscriptionTypeByCompanyCode(Integer companyId);
}
