package com.itt.service.fw.ratelimit.utility;

import com.itt.service.fw.ratelimit.enums.CacheProviderType;
import org.springframework.stereotype.Component;

import javax.cache.spi.CachingProvider;
import java.util.Iterator;
import java.util.ServiceLoader;

@Component
public class CacheProviderResolver {

    public static CachingProvider resolve(CacheProviderType providerType) {
        Iterator<CachingProvider> providers = ServiceLoader.load(CachingProvider.class).iterator();

        while (providers.hasNext()) {
            CachingProvider provider = providers.next();
            String className = provider.getClass().getName().toLowerCase();

            if (className.contains(providerType.getProviderHint())) {
                return provider;
            }
        }
        throw new IllegalStateException("No CachingProvider found for: " + providerType);
    }
}
