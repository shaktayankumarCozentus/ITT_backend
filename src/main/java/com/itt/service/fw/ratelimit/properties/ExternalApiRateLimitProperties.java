package com.itt.service.fw.ratelimit.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for External API Rate Limiting.
 * 
 * This class provides comprehensive configuration support for external API rate limiting,
 * extending the existing rate limiting framework with authentication mechanism awareness.
 * It supports both global defaults and per-mechanism custom configurations.
 * 
 * Key Features:
 * - Global default rate limiting configuration
 * - Per-authentication mechanism customization (OAuth2, API Key, Basic Auth)
 * - Cache configuration for distributed environments
 * - Monitoring and alerting settings
 * - Environment-specific overrides support
 * 
 * Configuration Example:
 * ```yaml
 * external-api:
 *   rate-limiting:
 *     enabled: true
 *     default-config:
 *       requests-per-hour: 1000
 *       requests-per-minute: 100
 *       burst-capacity: 10
 *     cache:
 *       ttl-minutes: 60
 *       max-entries: 10000
 *     mechanisms:
 *       oauth2:
 *         requests-per-hour: 2000
 *         requests-per-minute: 200
 *       api-key:
 *         requests-per-hour: 500
 *         requests-per-minute: 50
 * ```
 * 
 * Developer Notes:
 * - Integrates seamlessly with existing rate limiting infrastructure
 * - Supports Spring Boot configuration properties auto-completion
 * - Provides reasonable defaults for quick setup
 * - Extensible for future authentication mechanisms
 * 
 * @author ITT Development Team
 * @version 1.0
 * @since 2024
 */
@Data
@ConfigurationProperties(prefix = "external-api.rate-limiting")
public class ExternalApiRateLimitProperties {
    
    /**
     * Whether external API rate limiting is enabled.
     * Default: false (must be explicitly enabled)
     */
    private boolean enabled = false;
    
    /**
     * Whether to use legacy @Value configuration.
     * When true, falls back to @Value properties for backward compatibility.
     * Default: true (for smooth migration)
     */
    private boolean useLegacyConfig = true;
    
    /**
     * Default rate limiting configuration applied to all external APIs.
     * These values are used when no mechanism-specific configuration is found.
     */
    @NestedConfigurationProperty
    private RateLimitConfig defaultConfig = new RateLimitConfig();
    
    /**
     * Cache configuration for distributed rate limiting.
     */
    @NestedConfigurationProperty
    private CacheConfig cache = new CacheConfig();
    
    /**
     * Monitoring and alerting configuration.
     */
    @NestedConfigurationProperty
    private MonitoringConfig monitoring = new MonitoringConfig();
    
    /**
     * Per-authentication mechanism rate limiting configurations.
     * Key: mechanism name (oauth2, api-key, basic-auth, etc.)
     * Value: specific rate limiting configuration for that mechanism
     */
    private Map<String, RateLimitConfig> mechanisms = new HashMap<>();
    
    /**
     * Rate limiting configuration structure.
     * Supports multi-tier rate limiting with hourly, minute, and burst limits.
     */
    @Data
    public static class RateLimitConfig {
        
        /**
         * Maximum requests allowed per hour.
         * This is the primary sustained usage limit.
         */
        private int requestsPerHour = 1000;
        
        /**
         * Maximum requests allowed per minute.
         * This provides burst protection within hourly limits.
         */
        private int requestsPerMinute = 100;
        
        /**
         * Immediate burst capacity for short-term spikes.
         * This allows temporary bursts above per-minute limits.
         */
        private int burstCapacity = 10;
        
        /**
         * Whether this configuration is enabled.
         * Allows selective enabling/disabling of mechanisms.
         */
        private boolean enabled = true;
        
        /**
         * Custom message to return when rate limit is exceeded.
         * If null, uses default message.
         */
        private String customMessage;
        
        /**
         * HTTP status code to return when rate limit is exceeded.
         * Default: 429 (Too Many Requests)
         */
        private int statusCode = 429;
    }
    
    /**
     * Cache configuration for distributed rate limiting.
     */
    @Data
    public static class CacheConfig {
        
        /**
         * Cache TTL in minutes for rate limiting buckets.
         * After this time, unused buckets are evicted from cache.
         */
        private int ttlMinutes = 60;
        
        /**
         * Maximum number of cache entries to maintain.
         * Prevents memory issues in high-traffic scenarios.
         */
        private int maxEntries = 10000;
        
        /**
         * Cache name prefix for external API rate limiting.
         * Helps distinguish from other cache entries.
         */
        private String cachePrefix = "external-api-rate-limit";
        
        /**
         * Whether to use distributed caching (Redis).
         * When false, uses local caching (Caffeine).
         */
        private boolean distributed = true;
    }
    
    /**
     * Monitoring and alerting configuration.
     */
    @Data
    public static class MonitoringConfig {
        
        /**
         * Whether to enable rate limiting metrics.
         * Provides insights into rate limiting effectiveness.
         */
        private boolean metricsEnabled = true;
        
        /**
         * Whether to log rate limiting events.
         * Useful for debugging and auditing.
         */
        private boolean loggingEnabled = true;
        
        /**
         * Log level for rate limiting events.
         * Options: TRACE, DEBUG, INFO, WARN, ERROR
         */
        private String logLevel = "INFO";
        
        /**
         * Whether to enable alerting when rate limits are exceeded.
         * Can trigger notifications to administrators.
         */
        private boolean alertingEnabled = false;
        
        /**
         * Threshold percentage for rate limit warnings.
         * Triggers alerts when usage exceeds this percentage of limits.
         */
        private int warningThresholdPercent = 80;
    }
    
    /**
     * Gets rate limiting configuration for a specific authentication mechanism.
     * 
     * @param mechanismName the authentication mechanism name
     * @return specific configuration or default if not found
     */
    public RateLimitConfig getConfigForMechanism(String mechanismName) {
        return mechanisms.getOrDefault(mechanismName, defaultConfig);
    }
    
    /**
     * Checks if rate limiting is enabled for a specific mechanism.
     * 
     * @param mechanismName the authentication mechanism name
     * @return true if enabled, false otherwise
     */
    public boolean isEnabledForMechanism(String mechanismName) {
        if (!enabled) {
            return false;
        }
        return getConfigForMechanism(mechanismName).isEnabled();
    }
}
