package com.itt.service.repository;

import org.springframework.stereotype.Repository;

import com.itt.service.entity.MasterCountry;

@Repository
public interface MasterCountryRepository extends BaseRepository<MasterCountry, Integer> {

	MasterCountry findByCountryCode(String countryCode);

	MasterCountry findByCountryName(String countryName);
}
