package com.itt.service.fw.lit.cache;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Redis-only configuration block.
 * Bound under "lit.cache.redis".
 */
public record RedisCacheProperties(
        /**
         * Key prefix for all Redis cache entries, e.g. "lit:".
         */
        @NotBlank @DefaultValue("lit:")
        String keyPrefix
) { }
