package com.itt.service.fw.ratelimit.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CacheProviderType {
    REDIS("redisson"),
    CAFFEINE("caffeine");

    private final String providerHint;
}


