package com.itt.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.itt.service.entity.MapCompanyPetaPetd;

public interface MapCompanyPetaPetdRepository extends BaseRepository<MapCompanyPetaPetd, Integer> {

	@Modifying
	@Transactional
	@Query("UPDATE MapCompanyPetaPetd m SET m.petaPetdEnabledFlag = :petaCalling WHERE m.companyId IN :companyCodes")
	Integer updatePetaPetdCallingByCompanyCodes(@Param("petaCalling") Boolean petaCalling,
			@Param("companyCodes") List<Integer> companyCodes);
	
	@Modifying
	@Transactional
	@Query("UPDATE MapCompanyPetaPetd m SET m.petaPetdEnabledFlag = :petaCalling")
	Integer updatePetaPetdCallingForAllCompanies(@Param("petaCalling") Boolean petaCalling);

	@Modifying
	@Transactional
	@Query("UPDATE MapCompanyPetaPetd m SET m.petaPetdEnabledFlag = :petaCalling,"
			+ "m.oceanFrequency.id = :oceanFrequencyType, "
			+ "m.airFrequency.id = :airFrequencyType, "
			+ "m.railRoadFrequency.id = :railRoadFrequencyType "
			+ "WHERE m.companyId = :companyCode")
	Integer updatePetaPetdFrequencyByCompanyCodes(@Param("petaCalling") Boolean petaCalling,
			@Param("oceanFrequencyType") Integer oceanFrequencyType,
			@Param("airFrequencyType") Integer airFrequencyType,
			@Param("railRoadFrequencyType") Integer railRoadFrequencyType,
			@Param("companyCode") Integer companyCode);
}
