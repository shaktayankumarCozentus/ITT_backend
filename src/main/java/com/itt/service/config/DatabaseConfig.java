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
@EnableJpaRepositories(basePackages = "com.itt.service.repository", entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
public class DatabaseConfig {

	private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

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
		config.setDriverClassName(DRIVER_CLASS_NAME);

		// Write-optimized connection pool settings
		config.setMaximumPoolSize(50);
		config.setMinimumIdle(10);
		config.setIdleTimeout(300000); // 5 minutes
		config.setMaxLifetime(1800000); // 30 minutes
		config.setConnectionTimeout(20000); // 20 seconds
		config.setLeakDetectionThreshold(60000); // 1 minute
		config.setValidationTimeout(5000); // 5 seconds
		config.setAutoCommit(false); // CRITICAL: Disable autocommit for transactions

		// Write-specific optimizations
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");

		// CRITICAL: MySQL batch optimization properties
		config.addDataSourceProperty("rewriteBatchedStatements", "true");
		config.addDataSourceProperty("useMultiQueries", "true");
		config.addDataSourceProperty("allowMultiQueries", "true");
		config.addDataSourceProperty("useLocalSessionState", "true");
		config.addDataSourceProperty("useLocalTransactionState", "true");

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
		config.setDriverClassName(DRIVER_CLASS_NAME);

		// Read-optimized connection pool settings (larger pool for read operations)
		config.setMaximumPoolSize(100);
		config.setMinimumIdle(20);
		config.setIdleTimeout(600000); // 10 minutes
		config.setMaxLifetime(1800000); // 30 minutes
		config.setConnectionTimeout(10000); // 10 seconds
		config.setLeakDetectionThreshold(60000); // 1 minute
		config.setValidationTimeout(3000); // 3 seconds
		config.setAutoCommit(true); // Read-only can use autocommit

		// Read-specific optimizations
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "500");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");
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

		Map<String, Object> properties = new HashMap<>();
		properties.put("hibernate.hbm2ddl.auto", "none");
		properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
		properties.put("hibernate.show_sql", false);
		properties.put("hibernate.format_sql", true);
		properties.put("hibernate.use_sql_comments", true);

		// =============================================================================
		// MYSQL BATCH OPTIMIZATION - STABLE CONFIGURATION
		// =============================================================================
		properties.put("hibernate.jdbc.batch_size", 50);
		properties.put("hibernate.order_inserts", true);
		properties.put("hibernate.order_updates", true);
		properties.put("hibernate.jdbc.batch_versioned_data", true);

		// MySQL IDENTITY optimizations
		properties.put("hibernate.id.new_generator_mappings", true);
		properties.put("hibernate.jdbc.use_get_generated_keys", true);

		// Enhanced batch fetch configuration
		properties.put("hibernate.default_batch_fetch_size", 16);
		properties.put("hibernate.jdbc.lob.non_contextual_creation", true);

		properties.put("hibernate.generate_statistics", false);

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
