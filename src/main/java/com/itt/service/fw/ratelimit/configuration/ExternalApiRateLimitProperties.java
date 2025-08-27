package com.itt.service.fw.ratelimit.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ✅ PRODUCTION-READY: External API Rate Limiting Configuration Properties
 * =======================================================================
 * 
 * Configuration properties for external API rate limiting that integrates with
 * the existing rate limiting framework while providing separate controls for
 * external API authentication mechanisms.
 * 
 * 🔧 CONFIGURATION STRATEGY:
 * - Separate configuration from user-based rate limiting
 * - Environment-specific settings for different deployment contexts
 * - Default values suitable for production use
 * - Flexible configuration for different authentication mechanisms
 * 
 * 🎯 CONFIGURATION LAYERS:
 * - Global settings for all external APIs
 * - Per-mechanism configuration (API key, HMAC, OAuth2, mTLS)
 * - Default fallback values for unknown clients
 * - Administrative controls for enabling/disabling features
 * 
 * 📊 RATE LIMITING TIERS:
 * - Hourly limits for sustained usage control
 * - Minute limits for burst protection
 * - Immediate burst capacity for handling spikes
 * - Configurable time windows and capacities
 * 
 * 🚨 SECURITY CONSIDERATIONS:
 * - Conservative defaults to prevent abuse
 * - Administrative controls for emergency response
 * - Separate configuration from internal user limits
 * - Clear documentation for security teams
 * 
 * 📝 USAGE EXAMPLES:
 * 
 * application.yml (Production):
 * ```yaml
 * app:
 *   rate-limiting:
 *     external-api:
 *       enabled: true
 *       default:
 *         requests-per-hour: 1000
 *         requests-per-minute: 100
 *         burst-capacity: 10
 *       api-key:
 *         requests-per-hour: 5000
 *         requests-per-minute: 200
 *       oauth2:
 *         requests-per-hour: 10000
 *         requests-per-minute: 500
 * ```
 * 
 * application-dev.yml (Development):
 * ```yaml
 * app:
 *   rate-limiting:
 *     external-api:
 *       enabled: false  # Disable for development
 * ```
 * 
 * Environment Variables:
 * ```bash
 * APP_RATE_LIMITING_EXTERNAL_API_ENABLED=true
 * APP_RATE_LIMITING_EXTERNAL_API_DEFAULT_REQUESTS_PER_HOUR=1000
 * ```
 * 
 * @author Service Team
 * @version 1.0 - Initial Implementation with Multi-Tier Configuration
 * @since 2025.1
 * @see ExternalApiRateLimitingService
 * @see ExternalApiRateLimitFilter
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.rate-limiting.external-api")
public class ExternalApiRateLimitProperties {

    // ═══════════════════════════════════════════════════════════════
    // 🔧 GLOBAL CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Master switch to enable/disable external API rate limiting.
     * When disabled, all external API requests are allowed without limits.
     * 
     * Default: true (rate limiting enabled)
     * Environment: APP_RATE_LIMITING_EXTERNAL_API_ENABLED
     */
    private boolean enabled = true;
    
    /**
     * Whether to fail open (allow requests) when rate limiting service fails.
     * true = allow requests on errors (high availability)
     * false = block requests on errors (high security)
     * 
     * Default: true (fail open for availability)
     */
    private boolean failOpen = true;

    // ═══════════════════════════════════════════════════════════════
    // 🔧 DEFAULT RATE LIMITING CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Default rate limiting configuration applied to all external APIs
     * when no specific configuration is found.
     */
    private DefaultConfig defaultConfig = new DefaultConfig();
    
    @Data
    public static class DefaultConfig {
        /**
         * Maximum requests allowed per hour for unknown/default clients.
         * This is the primary rate limit for sustained usage.
         * 
         * Default: 1000 requests/hour
         */
        private int requestsPerHour = 1000;
        
        /**
         * Maximum requests allowed per minute for burst protection.
         * Prevents short-term spikes that could overwhelm the system.
         * 
         * Default: 100 requests/minute
         */
        private int requestsPerMinute = 100;
        
        /**
         * Immediate burst capacity for handling traffic spikes.
         * Allows temporary bursts above the per-minute limit.
         * 
         * Default: 10 requests in 10 seconds
         */
        private int burstCapacity = 10;
        
        /**
         * Burst time window in seconds.
         * Time period for the burst capacity limit.
         * 
         * Default: 10 seconds
         */
        private int burstWindowSeconds = 10;
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔧 AUTHENTICATION MECHANISM SPECIFIC CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Rate limiting configuration specific to API key authentication.
     * API keys typically have higher trust and may warrant higher limits.
     */
    private ApiKeyConfig apiKey = new ApiKeyConfig();
    
    @Data
    public static class ApiKeyConfig {
        /**
         * Whether API key rate limiting is enabled.
         * Can be disabled separately from other mechanisms.
         */
        private boolean enabled = true;
        
        /**
         * Higher limits for API key authentication due to registration process.
         */
        private int requestsPerHour = 5000;
        private int requestsPerMinute = 200;
        private int burstCapacity = 20;
        private int burstWindowSeconds = 10;
    }
    
    /**
     * Rate limiting configuration for HMAC signature authentication.
     * HMAC typically indicates enterprise clients with higher limits.
     */
    private HmacConfig hmac = new HmacConfig();
    
    @Data
    public static class HmacConfig {
        private boolean enabled = true;
        private int requestsPerHour = 10000;
        private int requestsPerMinute = 500;
        private int burstCapacity = 50;
        private int burstWindowSeconds = 10;
    }
    
    /**
     * Rate limiting configuration for OAuth2 client credentials.
     * OAuth2 clients are typically registered and trusted.
     */
    private OAuth2Config oauth2 = new OAuth2Config();
    
    @Data
    public static class OAuth2Config {
        private boolean enabled = true;
        private int requestsPerHour = 10000;
        private int requestsPerMinute = 500;
        private int burstCapacity = 50;
        private int burstWindowSeconds = 10;
    }
    
    /**
     * Rate limiting configuration for mutual TLS authentication.
     * mTLS provides the highest trust level and gets the highest limits.
     */
    private MtlsConfig mtls = new MtlsConfig();
    
    @Data
    public static class MtlsConfig {
        private boolean enabled = true;
        private int requestsPerHour = 50000;
        private int requestsPerMinute = 2000;
        private int burstCapacity = 100;
        private int burstWindowSeconds = 10;
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔧 CACHE AND PERFORMANCE CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Cache configuration for rate limiting buckets.
     */
    private CacheConfig cache = new CacheConfig();
    
    @Data
    public static class CacheConfig {
        /**
         * TTL for rate limiting cache entries in seconds.
         * After this time, buckets are evicted and recreated.
         * 
         * Default: 1 hour (3600 seconds)
         */
        private int ttlSeconds = 3600;
        
        /**
         * Maximum number of cached buckets.
         * Prevents memory exhaustion from too many unique API keys.
         * 
         * Default: 10000 buckets
         */
        private int maxSize = 10000;
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔧 MONITORING AND ALERTING CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Monitoring configuration for rate limiting metrics.
     */
    private MonitoringConfig monitoring = new MonitoringConfig();
    
    @Data
    public static class MonitoringConfig {
        /**
         * Whether to enable detailed metrics collection.
         * Includes per-API-key and per-endpoint metrics.
         */
        private boolean detailedMetrics = true;
        
        /**
         * Whether to log rate limiting events.
         * Useful for debugging and monitoring.
         */
        private boolean logEvents = true;
        
        /**
         * Threshold for rate limit warnings (percentage of limit).
         * Alerts when usage exceeds this percentage.
         * 
         * Default: 80% of rate limit
         */
        private int warningThresholdPercent = 80;
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔍 HELPER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Gets the configuration for a specific authentication mechanism.
     * 
     * @param mechanism the authentication mechanism
     * @return the configuration object for the mechanism
     */
    public Object getConfigForMechanism(String mechanism) {
        return switch (mechanism.toUpperCase()) {
            case "API_KEY" -> apiKey;
            case "HMAC" -> hmac;
            case "OAUTH2", "OAUTH2_CLIENT_CREDENTIALS" -> oauth2;
            case "MTLS" -> mtls;
            default -> defaultConfig;
        };
    }
    
    /**
     * Checks if rate limiting is enabled for a specific mechanism.
     * 
     * @param mechanism the authentication mechanism
     * @return true if rate limiting is enabled for the mechanism
     */
    public boolean isEnabledForMechanism(String mechanism) {
        if (!enabled) {
            return false;
        }
        
        return switch (mechanism.toUpperCase()) {
            case "API_KEY" -> apiKey.enabled;
            case "HMAC" -> hmac.enabled;
            case "OAUTH2", "OAUTH2_CLIENT_CREDENTIALS" -> oauth2.enabled;
            case "MTLS" -> mtls.enabled;
            default -> true; // Default is enabled
        };
    }
}
