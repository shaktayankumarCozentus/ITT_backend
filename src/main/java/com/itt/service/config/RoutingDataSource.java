package com.itt.service.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom routing datasource that determines which datasource to use based on
 * the current transaction context.
 * 
 * Routes to: - Write datasource for write operations (INSERT, UPDATE, DELETE) -
 * Read datasource for read operations (SELECT)
 * 
 * @author ITT Team
 * @version 1.0
 */
@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		DataSourceType dataSourceType = DataSourceContextHolder.getDataSourceType();
		log.info("[RoutingDataSource] Routing to DataSource: {}", dataSourceType);
		return dataSourceType;
	}
}
