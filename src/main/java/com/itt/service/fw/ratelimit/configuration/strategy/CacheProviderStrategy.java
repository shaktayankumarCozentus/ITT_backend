package com.itt.service.fw.ratelimit.configuration.strategy;

import javax.cache.CacheManager;

public interface CacheProviderStrategy {
    CacheManager provide();
}
