package com.itt.service.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Hybrid Database Configuration for Read-Write Separation
 * 
 * This configuration provides: - Primary datasource for write operations
 * (INSERT, UPDATE, DELETE) - Read-only datasource for read operations (SELECT)
 * - Automatic transaction management - Connection pooling optimization
 * 
 * @author ITT Team
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(DataSourceProperties.class)
@EnableJpaRepositories(basePackages = "com.itt.service.repository", entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
public class DatabaseConfig {

	@Autowired
	private DataSourceProperties dataSourceProperties;

	// ==========================================
	// WRITE DATASOURCE (PRIMARY)
	// ==========================================

	/**
	 * Primary datasource for write operations (INSERT, UPDATE, DELETE). Configured
	 * with write-optimized connection pool settings.
	 */
	@Primary
	@Bean(name = "writeDataSource")
	public DataSource writeDataSource(AwsSecrets awsSecrets) {
		HikariConfig config = new HikariConfig();

		log.info("Configuring write datasource...");

		// Write datasource configuration - Use effective methods for fallback support
		config.setJdbcUrl(buildJdbcUrl(awsSecrets.getDatabaseWriteHost(), awsSecrets.getDatabaseWritePort(),
				awsSecrets.getDatabaseWriteDatabase()));
		config.setUsername(awsSecrets.getDatabaseWriteUsername());
		config.setPassword(awsSecrets.getDatabaseWritePassword());
		config.setDriverClassName(dataSourceProperties.getDriverClassName());

		// Get environment-specific connection pool settings from YAML
		DataSourceProperties.PoolConfig writeConfig = dataSourceProperties.getWriteHikariConfig();
		DataSourceProperties.ConnectionProperties writeConnProps = writeConfig.getConnectionProperties();
		
		// Apply YAML-configured connection pool settings
		config.setMaximumPoolSize(writeConfig.getMaximumPoolSize());
		config.setMinimumIdle(writeConfig.getMinimumIdle());
		config.setIdleTimeout(writeConfig.getIdleTimeout());
		config.setMaxLifetime(writeConfig.getMaxLifetime());
		config.setConnectionTimeout(writeConfig.getConnectionTimeout());
		config.setLeakDetectionThreshold(writeConfig.getLeakDetectionThreshold());
		config.setValidationTimeout(writeConfig.getValidationTimeout());
		config.setAutoCommit(false); // CRITICAL: Disable autocommit for transactions

		// Write-specific optimizations from YAML configuration
		config.addDataSourceProperty("cachePrepStmts", String.valueOf(writeConnProps.getCachePrepStmts()));
		config.addDataSourceProperty("prepStmtCacheSize", String.valueOf(writeConnProps.getPrepStmtCacheSize()));
		config.addDataSourceProperty("prepStmtCacheSqlLimit", String.valueOf(writeConnProps.getPrepStmtCacheSqlLimit()));
		config.addDataSourceProperty("useServerPrepStmts", String.valueOf(writeConnProps.getUseServerPrepStmts()));

		// CRITICAL: MySQL batch optimization properties from YAML
		config.addDataSourceProperty("rewriteBatchedStatements", String.valueOf(writeConnProps.getRewriteBatchedStatements()));
		config.addDataSourceProperty("useMultiQueries", String.valueOf(writeConnProps.getUseMultiQueries()));
		config.addDataSourceProperty("allowMultiQueries", String.valueOf(writeConnProps.getAllowMultiQueries()));
		config.addDataSourceProperty("useLocalSessionState", String.valueOf(writeConnProps.getUseLocalSessionState()));
		config.addDataSourceProperty("useLocalTransactionState", String.valueOf(writeConnProps.getUseLocalTransactionState()));

		config.setPoolName("WritePool");

		log.info("Configured write datasource with pool size: {}", config.getMaximumPoolSize());
		return new HikariDataSource(config);
	}

	// ==========================================
	// READ DATASOURCE (SECONDARY)
	// ==========================================

	/**
	 * Secondary datasource for read operations (SELECT). Configured with
	 * read-optimized connection pool settings. Works for both dev and test
	 * environments with AWS configuration.
	 */
	@Bean(name = "readDataSource")
	public DataSource readDataSource(AwsSecrets awsSecrets) {
		HikariConfig config = new HikariConfig();

		log.info("Configuring read datasource...");

		// Read datasource configuration - Use effective methods for fallback support
		config.setJdbcUrl(buildJdbcUrl(awsSecrets.getDatabaseReadHost(), awsSecrets.getDatabaseReadPort(),
				awsSecrets.getDatabaseReadDatabase()));
		config.setUsername(awsSecrets.getDatabaseReadUsername());
		config.setPassword(awsSecrets.getDatabaseReadPassword());
		config.setDriverClassName(dataSourceProperties.getDriverClassName());

		// Get environment-specific connection pool settings from YAML
		DataSourceProperties.PoolConfig readConfig = dataSourceProperties.getReadHikariConfig();
		DataSourceProperties.ConnectionProperties readConnProps = readConfig.getConnectionProperties();
		
		// Apply YAML-configured connection pool settings
		config.setMaximumPoolSize(readConfig.getMaximumPoolSize());
		config.setMinimumIdle(readConfig.getMinimumIdle());
		config.setIdleTimeout(readConfig.getIdleTimeout());
		config.setMaxLifetime(readConfig.getMaxLifetime());
		config.setConnectionTimeout(readConfig.getConnectionTimeout());
		config.setLeakDetectionThreshold(readConfig.getLeakDetectionThreshold());
		config.setValidationTimeout(readConfig.getValidationTimeout());
		config.setAutoCommit(true); // Read-only can use autocommit

		// Read-specific optimizations from YAML configuration
		config.addDataSourceProperty("cachePrepStmts", String.valueOf(readConnProps.getCachePrepStmts()));
		config.addDataSourceProperty("prepStmtCacheSize", String.valueOf(readConnProps.getPrepStmtCacheSize()));
		config.addDataSourceProperty("prepStmtCacheSqlLimit", String.valueOf(readConnProps.getPrepStmtCacheSqlLimit()));
		config.addDataSourceProperty("useServerPrepStmts", String.valueOf(readConnProps.getUseServerPrepStmts()));
		config.setReadOnly(true); // Mark as read-only

		config.setPoolName("ReadPool");

		log.info("Configured read datasource with pool size: {}", config.getMaximumPoolSize());
		return new HikariDataSource(config);
	}

	/**
	 * Helper method to build JDBC URL from components.
	 */
	private String buildJdbcUrl(String host, String port, String database) {
		return "jdbc:mysql://" + host + ":" + port + "/" + database;
	}

	// ==========================================
	// ROUTING DATASOURCE
	// ==========================================

	@Bean(name = "routingDataSource")
	public DataSource routingDataSource(@Qualifier("writeDataSource") DataSource writeDataSource,
			@Qualifier("readDataSource") DataSource readDataSource) {

		RoutingDataSource routingDataSource = new RoutingDataSource();

		Map<Object, Object> dataSourceMap = new HashMap<>();
		dataSourceMap.put(DataSourceType.WRITE, writeDataSource);
		dataSourceMap.put(DataSourceType.READ, readDataSource);

		routingDataSource.setTargetDataSources(dataSourceMap);
		routingDataSource.setDefaultTargetDataSource(writeDataSource);

		log.info("Configured routing datasource with write and read targets");
		return routingDataSource;
	}

	// ==========================================
	// JPA CONFIGURATION
	// ==========================================

	@Primary
	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			@Qualifier("routingDataSource") DataSource routingDataSource) {

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setDataSource(routingDataSource);
		factory.setPackagesToScan("com.itt.service.entity");

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		factory.setJpaVendorAdapter(vendorAdapter);

		// Get environment-specific JPA configuration from YAML
		DataSourceProperties.JpaConfig jpaConfig = dataSourceProperties.getJpaConfig();

		Map<String, Object> properties = new HashMap<>();
		properties.put("hibernate.hbm2ddl.auto", jpaConfig.getDdlAuto());
		properties.put("hibernate.dialect", jpaConfig.getDialect());
		properties.put("hibernate.show_sql", jpaConfig.getShowSql());
		properties.put("hibernate.format_sql", jpaConfig.getFormatSql());
		properties.put("hibernate.use_sql_comments", jpaConfig.getUseSqlComments());

		// =============================================================================
		// MYSQL BATCH OPTIMIZATION - ENVIRONMENT-SPECIFIC CONFIGURATION
		// =============================================================================
		properties.put("hibernate.jdbc.batch_size", jpaConfig.getBatchSize());
		properties.put("hibernate.order_inserts", jpaConfig.getOrderInserts());
		properties.put("hibernate.order_updates", jpaConfig.getOrderUpdates());
		properties.put("hibernate.jdbc.batch_versioned_data", jpaConfig.getBatchVersionedData());

		// MySQL IDENTITY optimizations from YAML
		properties.put("hibernate.id.new_generator_mappings", jpaConfig.getNewGeneratorMappings());
		properties.put("hibernate.jdbc.use_get_generated_keys", jpaConfig.getUseGetGeneratedKeys());

		// Enhanced batch fetch configuration from YAML
		properties.put("hibernate.default_batch_fetch_size", jpaConfig.getDefaultBatchFetchSize());
		properties.put("hibernate.jdbc.lob.non_contextual_creation", jpaConfig.getLobNonContextualCreation());

		properties.put("hibernate.generate_statistics", jpaConfig.getGenerateStatistics());

		factory.setJpaPropertyMap(properties);

		log.info("Configured JPA EntityManagerFactory with routing datasource");
		return factory;
	}

	@Primary
	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {

		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());

		log.info("Configured JPA TransactionManager");
		return transactionManager;
	}
}
