package com.itt.service.fw.ratelimit.utility;

import com.itt.service.fw.ratelimit.enums.CacheProviderType;
import org.springframework.stereotype.Component;
import com.itt.service.exception.CustomException;
import com.itt.service.enums.ErrorCode;

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
        throw new CustomException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, "No CachingProvider found for: " + providerType);
    }
}
