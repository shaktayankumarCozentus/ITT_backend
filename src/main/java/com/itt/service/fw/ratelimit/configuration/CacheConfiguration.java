package com.itt.service.fw.ratelimit.configuration;

import com.itt.service.fw.ratelimit.configuration.strategy.CacheProviderStrategy;
import com.itt.service.fw.ratelimit.configuration.strategy.CaffeineCacheProvider;
import com.itt.service.fw.ratelimit.configuration.strategy.RedisCacheProvider;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.UUID;

import static com.itt.service.fw.ratelimit.constant.AppConstant.CACHE_NAME;

@Configuration
@RequiredArgsConstructor
public class CacheConfiguration {

    private final RLCacheProperties rlCacheProperties;
    private final CaffeineCacheProvider caffeineCacheProvider;
    private final RedisCacheProvider redisCacheProvider;

    @Bean(name = "rate-limit-cache-manager")
    public CacheManager cacheManager() {
        CacheProviderStrategy strategy = switch (rlCacheProperties.getType()) {
            case CAFFEINE -> caffeineCacheProvider;
            case REDIS -> redisCacheProvider;
        };
        return strategy.provide();
    }

    @Bean
    public ProxyManager<UUID> proxyManager(CacheManager cacheManager) {
        Cache<UUID, byte[]> cache = cacheManager.getCache(CACHE_NAME);
        return new JCacheProxyManager<>(cache);
    }

    @Bean("stringProxyManager")
    public ProxyManager<String> stringProxyManager(CacheManager cacheManager) {
        Cache<String, byte[]> cache = cacheManager.getCache(CACHE_NAME);
        return new JCacheProxyManager<>(cache);
    }
}
