package com.itt.service.fw.ratelimit.configuration;

import com.itt.service.fw.ratelimit.enums.CacheProviderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cache")
public class RLCacheProperties {
    private CacheProviderType type = CacheProviderType.REDIS;
    private int ttlMinutes = 10;
}

