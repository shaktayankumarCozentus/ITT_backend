package com.itt.service.fw.lit.service.impl;

import com.itt.service.fw.lit.cache.CacheProperties;
import com.itt.service.fw.lit.service.LitCacheAdapter;
import com.itt.service.fw.lit.dto.common.LitMessageDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.*;

public class RedisLitCacheAdapter implements LitCacheAdapter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String keyAll;
    private final String keyLangPrefix;

    public RedisLitCacheAdapter(RedisTemplate<String, Object> redisTemplate, CacheProperties props) {
        this.redisTemplate = redisTemplate;
        this.redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        String prefix = props.redis().keyPrefix();
        this.keyAll = prefix + "all";
        this.keyLangPrefix = prefix + "lang:";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<List<LitMessageDto>> getAll() {
        Object val = redisTemplate.opsForValue().get(keyAll);
        return Optional.ofNullable((List<LitMessageDto>) val);
    }

    @Override
    public void putAll(List<LitMessageDto> all) {
        redisTemplate.opsForValue().set(keyAll, all);
    }

    @Override
    public void evictAll() {
        redisTemplate.delete(keyAll);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<List<LitMessageDto>> getByLang(String lang) {
        Object val = redisTemplate.opsForValue().get(keyLangPrefix + lang);
        return Optional.ofNullable((List<LitMessageDto>) val);
    }

    @Override
    public void putByLang(String lang, List<LitMessageDto> list) {
        redisTemplate.opsForValue().set(keyLangPrefix + lang, list);
    }

    @Override
    public void evictByLang(String lang) {
        redisTemplate.delete(keyLangPrefix + lang);
    }

    @Override
    public void putLangMap(Map<String, List<LitMessageDto>> langMap) {
        // bulk write; simple approach
        langMap.forEach(this::putByLang);
    }

    @Override
    public void evictAllLangs() {
        // if you maintain a set of langs as a key, you could scan-delete here.
        // For simplicity, we do nothing (optional to implement with SCAN).
    }
}
