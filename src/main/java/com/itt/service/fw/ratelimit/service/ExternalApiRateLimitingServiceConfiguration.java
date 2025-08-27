package com.itt.service.fw.ratelimit.service;

import com.itt.service.fw.ratelimit.properties.ExternalApiRateLimitProperties;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for External API Rate Limiting Service.
 * 
 * This configuration provides auto-configuration for the external API rate limiting
 * functionality when enabled via properties. It integrates with the existing
 * rate limiting infrastructure while extending it for external API support.
 * 
 * Key Features:
 * - Conditional activation based on configuration
 * - Integration with existing ProxyManager infrastructure
 * - Support for both legacy @Value and new properties-based configuration
 * - Comprehensive logging and monitoring capabilities
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
 * ```
 * 
 * Usage:
 * This configuration is automatically activated when rate limiting is enabled.
 * Developers can inject ExternalApiRateLimitingService anywhere in the application.
 * 
 * Developer Notes:
 * - Leverages existing Bucket4j infrastructure for consistency
 * - Supports both Redis and Caffeine caching backends
 * - Provides fallback configuration for smooth migration
 * - Thread-safe and production-ready implementation
 * 
 * @author ITT Development Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ExternalApiRateLimitProperties.class)
@ConditionalOnProperty(name = "external-api.rate-limiting.enabled", havingValue = "true", matchIfMissing = false)
public class ExternalApiRateLimitingServiceConfiguration {
    
    /**
     * Creates ExternalApiRateLimitingService bean.
     * 
     * This service extends the existing rate limiting framework to support
     * external API authentication mechanisms. It integrates seamlessly with
     * the current ProxyManager and caching infrastructure.
     * 
     * @param proxyManager the distributed bucket proxy manager (from existing infrastructure)
     * @param rateLimitProperties configuration properties for external API rate limiting
     * @return configured external API rate limiting service
     */
    @Bean
    public ExternalApiRateLimitingService externalApiRateLimitingService(
            @Qualifier("stringProxyManager") ProxyManager<String> proxyManager,
            ExternalApiRateLimitProperties rateLimitProperties) {
        
        log.info("🚀 Initializing External API Rate Limiting Service");
        log.info("📊 Configuration: Hourly limit: {}, Minute limit: {}, Burst capacity: {}", 
                rateLimitProperties.getDefaultConfig().getRequestsPerHour(),
                rateLimitProperties.getDefaultConfig().getRequestsPerMinute(),
                rateLimitProperties.getDefaultConfig().getBurstCapacity());
        
        return new ExternalApiRateLimitingService(proxyManager, rateLimitProperties);
    }
}
