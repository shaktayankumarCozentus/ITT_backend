package com.itt.service.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UNIFIED CACHE CONFIGURATION
 * 
 * Consolidates all caching concerns for the application:
 * 1. Spring Cache Manager for @Cacheable annotations
 * 2. Application-level caching for configuration data
 * 3. Performance optimization for frequently accessed data
 * 
 * Note: LIT (Localization/Internationalization Text) cache is configured separately
 * in LitCacheConfiguration.java as it uses a specialized cache adapter pattern.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Primary cache manager for Spring Cache annotations (@Cacheable, @CacheEvict, etc.)
     * 
     * Handles caching for:
     * - Master configuration data (SKIN, ROLE_TYPE, LANDING_PAGE)
     * - Role privilege hierarchies
     * - User permissions and authentication data
     * - Frequently accessed database queries
     * 
     * @return CacheManager instance for application-wide caching
     */
    @Bean
    public CacheManager cacheManager() {
        // For production, consider replacing with Caffeine or Redis for better TTL and eviction
        return new ConcurrentMapCacheManager(
            // Configuration caches
            "masterConfigs",           // Cache for MasterConfig queries
            "skinGroups",              // Cache for skin group configurations
            
            // Security and permission caches
            "rolePrivileges",          // Cache for role privilege hierarchies  
            "privilegeHierarchy",      // Cache for role-specific privilege trees
            "privilegeRows",           // Cache for master privilege data
            "userPermissions",         // Cache for user-specific permissions
            
            // Application data caches
            "companyData",             // Cache for company-related information
            "departmentData",          // Cache for department structures
            "featureToggles"           // Cache for feature flag configurations
        );
    }
}
