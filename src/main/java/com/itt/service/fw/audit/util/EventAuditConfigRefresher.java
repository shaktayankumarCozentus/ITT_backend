package com.itt.service.fw.audit.util;

import com.itt.service.fw.audit.service.impl.EventAuditConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

// EventAuditConfigRefresher.java
@Component
@RequiredArgsConstructor
@Slf4j
public class EventAuditConfigRefresher {

    private final EventAuditConfigService service;

    @Value("${audit.config.refresh-interval:30m}") // default 30 minutes
    private Duration refreshInterval;
    /**
     * Scheduled task to refresh the event audit configuration at a fixed interval.
     * The interval is defined in the EventAuditConfigService.
     */
    @Scheduled(fixedDelayString =
            "#{T(org.springframework.boot.convert.DurationStyle)" +
                    ".detectAndParse('${audit.config.refresh-interval:30m}').toMillis()}")
    public void scheduledRefresh() {
        service.refresh();
        log.info("Audit config refreshed. Next refresh in ~{} minute(s)",
                refreshInterval.toMinutes());
    }
}


