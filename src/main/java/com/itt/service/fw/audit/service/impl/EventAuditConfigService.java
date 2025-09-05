package com.itt.service.fw.audit.service.impl;

import com.itt.service.entity.UserEventAuditConfig;
import com.itt.service.fw.audit.dto.EffectiveAuditSettings;
import com.itt.service.repository.UserEventAuditConfigRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventAuditConfigService {

    private final UserEventAuditConfigRepository auditConfigRepository;
    private final AntPathMatcher ant = new AntPathMatcher();

    // simple in-memory cache; swap to Caffeine/Redis if you prefer
    private volatile List<UserEventAuditConfig> cache = List.of();

    @PostConstruct
    public void load() {
        refresh();
    }

    public void refresh() {
        cache = auditConfigRepository.findByEnabledTrue();
        log.info("Audit config cache refreshed, {} entries",cache.size());
    }


    public Optional<EffectiveAuditSettings> resolve(HttpServletRequest req, Method method) {
        if (req == null) return Optional.empty();

        final String path = req.getRequestURI();
        final String http = req.getMethod();

        // most specific first: EXACT > ANT > REGEX
        return cache.stream()
                .sorted(Comparator.comparingInt(c -> switch (c.getMatchType()) {
                    case "EXACT" -> 0;
                    case "ANT" -> 1;
                    default -> 2;
                }))
                .filter(c -> "ANY".equalsIgnoreCase(c.getHttpMethod()) || c.getHttpMethod().equalsIgnoreCase(http))
                .filter(c -> switch (c.getMatchType()) {
                    case "EXACT" -> path.equals(c.getPathPattern());
                    case "ANT" -> ant.match(c.getPathPattern(), path);
                    default -> path.matches(c.getPathPattern());
                })
                .findFirst()
                .map(this::toSettings);
    }

    private EffectiveAuditSettings toSettings(UserEventAuditConfig c) {
        Set<String> masks = (c.getMaskFields() == null || c.getMaskFields().isBlank())
                ? Set.of()
                : Arrays.stream(c.getMaskFields().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        return EffectiveAuditSettings.builder()
                .enabled(true)
                .logRequest(c.isLogRequest())
                .logResponse(c.isLogResponse())
                .logError(c.isLogError())
                .maskFields(masks)
                .build();
    }
}

