package com.itt.service.fw.lit.cache;

import com.itt.service.fw.ratelimit.enums.CacheProviderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Immutable, type-safe configuration for LIT cache.
 *
 * Prefix: {@code lit.cache}
 *
 * Example:
 * <pre>
 * lit:
 *   cache:
 *     type: memory   # memory | redis
 *     refresh-minutes: 10
 *     redis:
 *       key-prefix: "lit:"
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "cache")
public record CacheProperties(
        /**
         * Cache backend to use.
         */
        @NotNull @DefaultValue("CAFFEINE")
        CacheProviderType type,

        /**
         * Auto-refresh interval in minutes. Must be >= 1.
         */
        @Min(1) @DefaultValue("10")
        int refreshMinutes,

        /**
         * Redis-specific settings (used when type = REDIS).
         */
        @Valid @DefaultValue
        RedisCacheProperties redis
) {
    /** Convenience: refresh interval as Duration. */
    public Duration refreshInterval() {
        return Duration.ofMinutes(refreshMinutes);
    }

    /** Convenience flags to keep calling code expressive. */
    public boolean isMemory() { return type == CacheProviderType.CAFFEINE; }
    public boolean isRedis()  { return type == CacheProviderType.REDIS;  }
}
