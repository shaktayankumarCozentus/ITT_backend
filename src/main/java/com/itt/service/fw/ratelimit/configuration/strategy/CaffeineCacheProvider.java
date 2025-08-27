package com.itt.service.fw.ratelimit.configuration.strategy;

import com.itt.service.fw.ratelimit.configuration.RLCacheProperties;
import com.itt.service.fw.ratelimit.enums.CacheProviderType;
import com.itt.service.fw.ratelimit.utility.CacheProviderResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

import static com.itt.service.fw.ratelimit.constant.AppConstant.CACHE_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaffeineCacheProvider implements CacheProviderStrategy {

    private final RLCacheProperties rlCacheProperties;

    @Override
    public CacheManager provide() {
        log.info("Getting Caffeine Cache ..!!!");
        CacheManager cacheManager = CacheProviderResolver
                .resolve(CacheProviderType.CAFFEINE).getCacheManager();

        MutableConfiguration<Object, Object> config = new MutableConfiguration<>()
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(
                        new Duration(TimeUnit.MINUTES, rlCacheProperties.getTtlMinutes())));

        if (cacheManager.getCache(CACHE_NAME) == null) {
            cacheManager.createCache(CACHE_NAME, config);
        }
        log.info("Caffeine Cache Provided.");
        return cacheManager;
    }
}
