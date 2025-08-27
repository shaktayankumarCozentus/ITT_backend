package com.itt.service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Master configuration entity for the Service system.
 * 
 * <p>This entity serves as a flexible, centralized configuration store that
 * enables runtime configuration management without code deployments. It supports
 * various data types and configuration categories, making it ideal for feature
 * toggles, system parameters, and business configuration values.</p>
 * 
 * <h3>Configuration Management Features:</h3>
 * <ul>
 *   <li><strong>Type Safety:</strong> Supports integer and string value types</li>
 *   <li><strong>Categorization:</strong> Organized by config_type for logical grouping</li>
 *   <li><strong>Key-Based Access:</strong> Unique key_code for programmatic access</li>
 *   <li><strong>Runtime Changes:</strong> Modifications without application restarts</li>
 *   <li><strong>Activation Control:</strong> Time-based configuration activation</li>
 * </ul>
 * 
 * <h3>Database Schema:</h3>
 * <ul>
 *   <li><strong>Table:</strong> master_config</li>
 *   <li><strong>Primary Key:</strong> id (auto-generated)</li>
 *   <li><strong>Unique Constraints:</strong> (config_type, key_code) combination</li>
 *   <li><strong>Indexes:</strong> config_type, key_code for fast lookups</li>
 * </ul>
 * 
 * <h3>Configuration Types:</h3>
 * <p>Common configuration categories include:</p>
 * <ul>
 *   <li><strong>FEATURE_TOGGLE:</strong> Feature flags and experimental features</li>
 *   <li><strong>ROLE_TYPE:</strong> Role category definitions</li>
 *   <li><strong>LANDING_PAGE:</strong> Default navigation targets</li>
 *   <li><strong>SYSTEM_PARAM:</strong> System-wide parameters and limits</li>
 *   <li><strong>BUSINESS_RULE:</strong> Business logic configuration values</li>
 * </ul>
 * 
 * <h3>Value Types:</h3>
 * <ul>
 *   <li><strong>intValue:</strong> For numeric configurations (timeouts, limits, flags)</li>
 *   <li><strong>stringValue:</strong> For text configurations (URLs, messages, codes)</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Feature toggle configuration
 * MasterConfig asyncLogging = MasterConfig.builder()
 *     .configType("FEATURE_TOGGLE")
 *     .keyCode("ASYNC_LOGGING_ENABLED")
 *     .name("Async Logging Feature")
 *     .description("Enables asynchronous logging for better performance")
 *     .intValue(1)  // 1 = enabled, 0 = disabled
 *     .activatedDate(LocalDateTime.now())
 *     .createdById(1)  // system user ID
 *     .createdOn(LocalDateTime.now())
 *     .build();
 * 
 * // System parameter configuration
 * MasterConfig maxFileSize = MasterConfig.builder()
 *     .configType("SYSTEM_PARAM")
 *     .keyCode("MAX_UPLOAD_SIZE_MB")
 *     .name("Maximum Upload Size")
 *     .description("Maximum file upload size in megabytes")
 *     .intValue(10)
 *     .createdById(2)  // admin user ID
 *     .createdOn(LocalDateTime.now())
 *     .build();
 * 
 * // String configuration
 * MasterConfig defaultUrl = MasterConfig.builder()
 *     .configType("LANDING_PAGE")
 *     .keyCode("ADMIN_DASHBOARD_URL")
 *     .name("Admin Dashboard URL")
 *     .description("Default landing page for admin users")
 *     .stringValue("/admin/dashboard")
 *     .createdById(1)  // system user ID
 *     .createdOn(LocalDateTime.now())
 *     .build();
 * }</pre>
 * 
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li><strong>Naming:</strong> Use clear, descriptive key_code values</li>
 *   <li><strong>Documentation:</strong> Always provide meaningful descriptions</li>
 *   <li><strong>Versioning:</strong> Consider activation dates for staged rollouts</li>
 *   <li><strong>Validation:</strong> Implement application-level validation for critical configs</li>
 * </ul>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see FeatureToggleService
 * @see Role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "master_config")
public class MasterConfig {

    /**
     * Unique identifier for the configuration entry.
     * Auto-generated primary key using database identity column.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Configuration category or type for logical grouping.
     * 
     * <p>Used to organize related configuration entries into logical categories.
     * Common types include FEATURE_TOGGLE, SYSTEM_PARAM, ROLE_TYPE, etc.
     * Maximum length of 25 characters to ensure efficient indexing.</p>
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>FEATURE_TOGGLE - Feature flags and experimental features</li>
     *   <li>ROLE_TYPE - Role category definitions</li>
     *   <li>LANDING_PAGE - Navigation configuration</li>
     *   <li>SYSTEM_PARAM - System-wide parameters</li>
     * </ul>
     */
    @Column(name = "config_type", nullable = false, length = 25)
    private String configType;

    /**
     * Unique key code for programmatic access to this configuration.
     * 
     * <p>Should be descriptive, use UPPER_CASE_WITH_UNDERSCORES convention,
     * and be unique within the config_type. This is the primary lookup key
     * used by application code to retrieve configuration values.</p>
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>ASYNC_LOGGING_ENABLED</li>
     *   <li>MAX_UPLOAD_SIZE_MB</li>
     *   <li>SESSION_TIMEOUT_MINUTES</li>
     *   <li>ADMIN_DASHBOARD_URL</li>
     * </ul>
     */
    @Column(name = "key_code", nullable = false, length = 50)
    private String keyCode;

    /**
     * Human-readable name for this configuration.
     * 
     * <p>Used in administrative interfaces and documentation to provide
     * a user-friendly display name for the configuration entry.</p>
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * Detailed description explaining the purpose and impact of this configuration.
     * 
     * <p>Should explain:</p>
     * <ul>
     *   <li>What this configuration controls</li>
     *   <li>Valid values and their meanings</li>
     *   <li>Impact of changing the value</li>
     *   <li>Any dependencies or side effects</li>
     * </ul>
     */
    @Column(name = "description", length = 300)
    private String description;

    /**
     * Date and time when this configuration becomes active.
     * 
     * <p>Enables time-based configuration activation for staged rollouts,
     * scheduled feature releases, or configuration changes. If null,
     * the configuration is considered active immediately upon creation.</p>
     */
    @Column(name = "activated_date")
    private LocalDateTime activatedDate;

    /**
     * Integer value for numeric configurations.
     * 
     * <p>Used for:</p>
     * <ul>
     *   <li><strong>Boolean flags:</strong> 0 = false, 1 = true</li>
     *   <li><strong>Numeric limits:</strong> timeouts, size limits, counts</li>
     *   <li><strong>Enumeration values:</strong> status codes, type identifiers</li>
     *   <li><strong>Percentages:</strong> rollout percentages, thresholds</li>
     * </ul>
     */
    @Column(name = "int_value")
    private Integer intValue;

    /**
     * String value for text-based configurations.
     * 
     * <p>Used for:</p>
     * <ul>
     *   <li><strong>URLs:</strong> endpoints, redirect targets</li>
     *   <li><strong>Messages:</strong> error messages, notifications</li>
     *   <li><strong>Codes:</strong> configuration codes, identifiers</li>
     *   <li><strong>JSON:</strong> complex configuration objects (serialized)</li>
     * </ul>
     */
    @Column(name = "string_value", length = 500)
    private String stringValue;

    /**
     * Timestamp when this configuration was created.
     * 
     * <p>Required field for audit trails and configuration lifecycle tracking.</p>
     */
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    /**
     * Identifier of the user who created this configuration.
     * 
     * <p>Essential for audit trails and accountability in configuration management.
     * Contains the user ID (integer) of the person who created this entry.</p>
     */
    @Column(name = "created_by_id", nullable = false)
    private Integer createdById;

    /**
     * Timestamp when this configuration was last modified.
     * 
     * <p>Updated automatically whenever the configuration is changed.
     * Used for cache invalidation and change tracking.</p>
     */
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    /**
     * Identifier of the user who last modified this configuration.
     * 
     * <p>Tracks the most recent modifier for audit purposes. Contains the
     * user ID (integer) of the person who last updated this entry. May be null
     * if the configuration has never been updated since creation.</p>
     */
    @Column(name = "updated_by_id")
    private Integer updatedById;
    
    /**
     * Convenience method to check if this configuration is currently active.
     * 
     * <p>Considers both the activation date and current time to determine
     * if this configuration should be applied.</p>
     * 
     * @return true if the configuration is active, false otherwise
     */
    public boolean isActive() {
        return activatedDate == null || !activatedDate.isAfter(LocalDateTime.now());
    }
    
    /**
     * Convenience method to get the configuration value as a boolean.
     * 
     * <p>Interprets the intValue as a boolean where 1 = true, all other values = false.
     * Useful for feature toggle configurations.</p>
     * 
     * @return true if intValue is 1, false otherwise
     */
    public boolean getBooleanValue() {
        return Integer.valueOf(1).equals(this.intValue);
    }
    
    /**
     * Convenience method to get the effective configuration value.
     * 
     * <p>Returns the string value if present, otherwise converts the integer
     * value to string. Useful for generic configuration access.</p>
     * 
     * @return the configuration value as a string
     */
    public String getEffectiveValue() {
        if (stringValue != null && !stringValue.trim().isEmpty()) {
            return stringValue;
        }
        return intValue != null ? intValue.toString() : null;
    }
}
