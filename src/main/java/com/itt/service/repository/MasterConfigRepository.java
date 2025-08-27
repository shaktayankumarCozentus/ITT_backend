package com.itt.service.repository;

import com.itt.service.entity.MasterConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasterConfigRepository extends JpaRepository<MasterConfig, Integer> {
	
	@Cacheable(value = "masterConfigs", key = "#configType")
	List<MasterConfig> findByConfigType(String configType);

	@Cacheable(value = "masterConfigs", key = "#keyCode")
	List<MasterConfig> findByKeyCode(String keyCode);
	
	/**
	 * PERFORMANCE OPTIMIZATION: Batch fetch configurations by type
	 * Reduces database round trips for configuration loading
	 * 
	 * @param configTypes List of configuration types to fetch
	 * @return List of configurations grouped by type
	 */
	@Cacheable(value = "masterConfigs", key = "#configTypes.toString()")
	@Query("SELECT m FROM MasterConfig m WHERE m.configType IN :configTypes ORDER BY m.configType, m.keyCode")
	List<MasterConfig> findByConfigTypes(@Param("configTypes") List<String> configTypes);
}
