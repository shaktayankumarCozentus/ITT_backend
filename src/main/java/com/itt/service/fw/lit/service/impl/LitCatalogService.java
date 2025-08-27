package com.itt.service.fw.lit.service.impl;

import com.itt.service.entity.LitMessage;
import com.itt.service.fw.lit.cache.CacheProperties;
import com.itt.service.fw.lit.dto.common.LitMessageDto;
import com.itt.service.fw.lit.service.LitCacheAdapter;
import com.itt.service.repository.LitMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class LitCatalogService {

    private final LitMessageRepository litMessageRepository;
    private final LitCacheAdapter cache;
    private final CacheProperties props;

    /**
     * Fetches all messages for a specific language and transforms them into a
     * flat Map of {LIT_CODE -> MESSAGE}.
     */
    @Transactional(readOnly = true)
    public Map<String, String> getMessagesForLanguage(String language) {
        List<LitMessage> messages = litMessageRepository.findByIdLanguageCode(language);

        return messages.stream()
                .collect(Collectors.toMap(
                        message -> message.getId().getLitCode(),
                        LitMessage::getMessage
                ));
    }

    /**
     * Fetches all messages from the database and groups them first by language,
     * then by LIT code, resulting in a nested Map structure.
     */
    @Transactional(readOnly = true)
    public Map<String, Map<String, String>> getAllMessagesGroupedByLanguage() {
        List<LitMessage> allMessages = litMessageRepository.findAll();

        return allMessages.stream()
                .collect(Collectors.groupingBy(
                        message -> message.getId().getLanguageCode(),
                        Collectors.toMap(
                                message -> message.getId().getLitCode(),
                                LitMessage::getMessage
                        )
                ));
    }

    // Auto-refresh ALL + BY_LANG at a fixed cadence
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).ofMinutes(@cacheProperties.refreshMinutes).toMillis()}")
    public void refreshCache() {
        List<LitMessageDto> all = toDtos(litMessageRepository.findAll());
        cache.putAll(all);
        cache.putLangMap(groupByLang(all));
    }

    private List<LitMessageDto> toDtos(List<LitMessage> entities) {
        return entities.stream()
                .map(e -> new LitMessageDto(
                        e.getId().getLitCode(),
                        e.getId().getLanguageCode(),
                        e.getMessage()))
                .toList();
    }

    private Map<String, List<LitMessageDto>> groupByLang(List<LitMessageDto> all) {
        return all.stream().collect(Collectors.groupingBy(LitMessageDto::languageCode));
    }

    // Expose props into SpEL for @Scheduled
    public CacheProperties getCacheProperties() {
        return props;
    }
}

