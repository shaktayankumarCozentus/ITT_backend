package com.itt.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itt.service.entity.MapCompanySubscription;

public interface MapCompanySubscriptionRepository extends JpaRepository<MapCompanySubscription, Integer> {

	List<MapCompanySubscription> findByCompanyId(Integer companyCode);

	List<MapCompanySubscription> findByCompanyIdIn(List<Integer> companyCode);

	Integer deleteByCompanyId(Integer companyCode);

	Integer deleteByCompanyIdIn(List<Integer> companyCodes);
	
	void deleteAll();
}
