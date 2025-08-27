package com.itt.service.fw.lit.service;

import com.itt.service.fw.lit.dto.common.LitMessageDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LitCacheAdapter {

    // ----- ALL -----
    Optional<List<LitMessageDto>> getAll();
    void putAll(List<LitMessageDto> all);
    void evictAll();

    // ----- BY LANG -----
    Optional<List<LitMessageDto>> getByLang(String lang);
    void putByLang(String lang, List<LitMessageDto> list);
    void evictByLang(String lang);

    // For bulk hydrate: replace current lang map
    void putLangMap(Map<String, List<LitMessageDto>> langMap);
    void evictAllLangs();
}
