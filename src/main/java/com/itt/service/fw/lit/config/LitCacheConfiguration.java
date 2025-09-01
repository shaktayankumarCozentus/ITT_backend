package com.itt.service.fw.lit.config;

import com.itt.service.fw.lit.cache.CacheProperties;
import com.itt.service.fw.lit.service.LitCacheAdapter;
import com.itt.service.fw.lit.service.impl.InMemoryLitCacheAdapter;
import com.itt.service.fw.lit.service.impl.RedisLitCacheAdapter;
import com.itt.service.exception.CustomException;
import com.itt.service.enums.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class LitCacheConfiguration {

    @Bean
    @ConditionalOnMissingBean(LitCacheAdapter.class)
    public LitCacheAdapter litCacheAdapter(CacheProperties props,
                                           RedisTemplate<String, Object> redisTemplate) {
        return switch (props.type().name().toLowerCase()) {
            case "redis" -> new RedisLitCacheAdapter(redisTemplate, props);
            case "memory" -> new InMemoryLitCacheAdapter();
            default -> throw new CustomException(ErrorCode.INVALID_DATA_FORMAT, "Unknown lit.cache.type: " + props.type());
        };
    }
}
