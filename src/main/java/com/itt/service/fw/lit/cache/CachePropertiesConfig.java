package com.itt.service.fw.lit.cache;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CachePropertiesConfig {

    /**
     * Expose the already-bound RLCacheProperties under the simple bean name
     * 'cacheProperties' so SpEL like @cacheProperties works.
     */
    @Bean(name = "cacheProperties")
    @Primary
    public CacheProperties cachePropertiesAlias(CacheProperties bound) {
        return bound; // alias to the already-created, validated instance
    }
}
