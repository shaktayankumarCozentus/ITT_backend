package com.itt.service.fw.lit.service.impl;

import com.itt.service.fw.lit.service.LitCacheAdapter;
import com.itt.service.fw.lit.dto.common.LitMessageDto;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class InMemoryLitCacheAdapter implements LitCacheAdapter {

    private volatile List<LitMessageDto> allCache = null;

    private final Map<String, List<LitMessageDto>> byLangCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(60)) // safety TTL; refresh handled by scheduler
                    .<String, List<LitMessageDto>>build()
                    .asMap();

    @Override
    public Optional<List<LitMessageDto>> getAll() {
        return Optional.ofNullable(allCache);
    }

    @Override
    public void putAll(List<LitMessageDto> all) {
        this.allCache = List.copyOf(all);
    }

    @Override
    public void evictAll() {
        this.allCache = null;
    }

    @Override
    public Optional<List<LitMessageDto>> getByLang(String lang) {
        return Optional.ofNullable(byLangCache.get(lang));
    }

    @Override
    public void putByLang(String lang, List<LitMessageDto> list) {
        byLangCache.put(lang, List.copyOf(list));
    }

    @Override
    public void evictByLang(String lang) {
        byLangCache.remove(lang);
    }

    @Override
    public void putLangMap(Map<String, List<LitMessageDto>> langMap) {
        byLangCache.clear();
        // copy defensively
        langMap.forEach((k, v) -> byLangCache.put(k, List.copyOf(v)));
    }

    @Override
    public void evictAllLangs() {
        byLangCache.clear();
    }
}
