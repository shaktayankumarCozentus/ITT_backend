package com.itt.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import com.itt.service.exception.CustomException;
import com.itt.service.enums.ErrorCode;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration properties for database connection pools
 * Reads environment-specific settings from application.yml
 * Uses custom namespace to supplement Spring Boot's datasource config
 * 
 * @author ITT Team
 * @version 2.0
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "app.database")
public class DataSourceProperties {

    private String driverClassName;
    private PoolConfig write = new PoolConfig();
    private PoolConfig read = new PoolConfig();
    private JpaConfig jpa = new JpaConfig();

    /**
     * Validates configuration on application startup
     * This ensures that missing YAML configuration is detected early
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfigurationOnStartup() {
        try {
            log.info("Validating database configuration from application.yml...");
            validateDriverClassName();
            getWriteHikariConfig();
            getReadHikariConfig();
            getJpaConfig();
            log.info("✓ Database configuration validation successful - all environment-specific values loaded from YAML");
            log.info("Driver class: {}", driverClassName);
            log.info("Write pool: max={}, min={}", write.getMaximumPoolSize(), write.getMinimumIdle());
            log.info("Read pool: max={}, min={}", read.getMaximumPoolSize(), read.getMinimumIdle());
            log.info("JPA batch size: {}, show SQL: {}", jpa.getBatchSize(), jpa.getShowSql());
        } catch (CustomException e) {
            log.error("❌ Database configuration validation FAILED: {}", e.getMessage());
            log.error("Please check your application.yml for missing app.database configuration");
            throw e; // Stop application startup
        }
    }

    /**
     * Validates that driver class name is configured
     * @throws CustomException if driver class name is missing
     */
    private void validateDriverClassName() {
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "driverClassName must be configured in application.yml under app.database");
        }
    }

    @Data
    public static class PoolConfig {
        // Connection Pool Settings
        private int maximumPoolSize;
        private int minimumIdle;
        private long connectionTimeout;
        private long idleTimeout;
        private long maxLifetime;
        private long leakDetectionThreshold;
        private long validationTimeout;
        
        // MySQL Connection Properties
        private ConnectionProperties connectionProperties = new ConnectionProperties();
        
        /**
         * Validates that all required properties are configured
         * @throws CustomException if any required property is missing/invalid
         */
        public void validate() {
            if (maximumPoolSize <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "maximumPoolSize must be configured and > 0 in application.yml");
            }
            if (minimumIdle < 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "minimumIdle must be configured and >= 0 in application.yml");
            }
            if (connectionTimeout <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "connectionTimeout must be configured and > 0 in application.yml");
            }
            if (idleTimeout <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "idleTimeout must be configured and > 0 in application.yml");
            }
            if (maxLifetime <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "maxLifetime must be configured and > 0 in application.yml");
            }
            if (leakDetectionThreshold < 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "leakDetectionThreshold must be configured and >= 0 in application.yml");
            }
            if (validationTimeout <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "validationTimeout must be configured and > 0 in application.yml");
            }
            // Validate connection properties
            connectionProperties.validate();
        }
    }
    
    @Data
    public static class ConnectionProperties {
        // Statement Caching
        private Boolean cachePrepStmts;
        private Integer prepStmtCacheSize;
        private Integer prepStmtCacheSqlLimit;
        private Boolean useServerPrepStmts;
        
        // Batch Processing (Write-specific)
        private Boolean rewriteBatchedStatements;
        private Boolean useMultiQueries;
        private Boolean allowMultiQueries;
        private Boolean useLocalSessionState;
        private Boolean useLocalTransactionState;
        
        /**
         * Validates that all required connection properties are configured
         * @throws CustomException if any required property is missing
         */
        public void validate() {
            if (cachePrepStmts == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "cachePrepStmts must be configured in application.yml");
            }
            if (prepStmtCacheSize == null || prepStmtCacheSize <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "prepStmtCacheSize must be configured and > 0 in application.yml");
            }
            if (prepStmtCacheSqlLimit == null || prepStmtCacheSqlLimit <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "prepStmtCacheSqlLimit must be configured and > 0 in application.yml");
            }
            if (useServerPrepStmts == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "useServerPrepStmts must be configured in application.yml");
            }
            if (rewriteBatchedStatements == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "rewriteBatchedStatements must be configured in application.yml");
            }
            if (useMultiQueries == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "useMultiQueries must be configured in application.yml");
            }
            if (allowMultiQueries == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "allowMultiQueries must be configured in application.yml");
            }
            if (useLocalSessionState == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "useLocalSessionState must be configured in application.yml");
            }
            if (useLocalTransactionState == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "useLocalTransactionState must be configured in application.yml");
            }
        }
    }
    
    @Data
    public static class JpaConfig {
        // Schema Management
        private String ddlAuto;
        private String dialect;
        
        // SQL Logging
        private Boolean showSql;
        private Boolean formatSql;
        private Boolean useSqlComments;
        private Boolean generateStatistics;
        
        // Batch Processing
        private Integer batchSize;
        private Boolean orderInserts;
        private Boolean orderUpdates;
        private Boolean batchVersionedData;
        
        // Performance Settings
        private Boolean newGeneratorMappings;
        private Boolean useGetGeneratedKeys;
        private Integer defaultBatchFetchSize;
        private Boolean lobNonContextualCreation;
        
        /**
         * Validates that all required JPA properties are configured
         * @throws CustomException if any required property is missing
         */
        public void validate() {
            if (ddlAuto == null || ddlAuto.trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "ddlAuto must be configured in application.yml");
            }
            if (dialect == null || dialect.trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "dialect must be configured in application.yml");
            }
            if (showSql == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "showSql must be configured in application.yml");
            }
            if (formatSql == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "formatSql must be configured in application.yml");
            }
            if (useSqlComments == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "useSqlComments must be configured in application.yml");
            }
            if (generateStatistics == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "generateStatistics must be configured in application.yml");
            }
            if (batchSize == null || batchSize <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "batchSize must be configured and > 0 in application.yml");
            }
            if (orderInserts == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "orderInserts must be configured in application.yml");
            }
            if (orderUpdates == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "orderUpdates must be configured in application.yml");
            }
            if (batchVersionedData == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "batchVersionedData must be configured in application.yml");
            }
            if (newGeneratorMappings == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "newGeneratorMappings must be configured in application.yml");
            }
            if (useGetGeneratedKeys == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "useGetGeneratedKeys must be configured in application.yml");
            }
            if (defaultBatchFetchSize == null || defaultBatchFetchSize <= 0) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "defaultBatchFetchSize must be configured and > 0 in application.yml");
            }
            if (lobNonContextualCreation == null) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "lobNonContextualCreation must be configured in application.yml");
            }
        }
    }

    /**
     * Get write pool configuration with validation
     * @return validated write pool configuration
     * @throws CustomException if configuration is missing or invalid
     */
    public PoolConfig getWriteHikariConfig() {
        write.validate();
        return write;
    }

    /**
     * Get read pool configuration with validation
     * @return validated read pool configuration
     * @throws CustomException if configuration is missing or invalid
     */
    public PoolConfig getReadHikariConfig() {
        read.validate();
        return read;
    }
    
    /**
     * Get JPA configuration with validation
     * @return validated JPA configuration settings
     * @throws CustomException if configuration is missing or invalid
     */
    public JpaConfig getJpaConfig() {
        jpa.validate();
        return jpa;
    }
}