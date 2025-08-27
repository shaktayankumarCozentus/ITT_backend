package com.itt.service.fw.ratelimit.configuration.strategy;

import com.itt.service.fw.ratelimit.configuration.RLCacheProperties;
import com.itt.service.fw.ratelimit.enums.CacheProviderType;
import com.itt.service.fw.ratelimit.utility.CacheProviderResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
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
public class RedisCacheProvider implements CacheProviderStrategy {

    private final RLCacheProperties rlCacheProperties;
    private final RedisProperties redisProperties;

    @Override
    public CacheManager provide() {
        log.info("Getting Redis Cache ..!!!");
        CacheManager cacheManager = CacheProviderResolver
                .resolve(CacheProviderType.REDIS).getCacheManager();

        if (cacheManager.getCache(CACHE_NAME) == null) {
            String redisUrl = String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort());

            Config redissonConfig = new Config();
            redissonConfig.useSingleServer()
                    .setAddress(redisUrl)
                    .setPassword(redisProperties.getPassword());

            MutableConfiguration<Object, Object> cacheConfig = new MutableConfiguration<>();
            cacheConfig.setStoreByValue(false);
            cacheConfig.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(
                    new Duration(TimeUnit.MINUTES, rlCacheProperties.getTtlMinutes())
            ));

            cacheManager.createCache(CACHE_NAME, RedissonConfiguration.fromConfig(redissonConfig, cacheConfig));
        }

        log.info("Redis Cache Provided.");
        return cacheManager;
    }
}
