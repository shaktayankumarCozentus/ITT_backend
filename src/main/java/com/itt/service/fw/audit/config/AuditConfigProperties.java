package com.itt.service.fw.audit.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit.config")
public class AuditConfigProperties {
    /**
     * How often to refresh the DB-driven audit configuration cache.
     * Supports ISO-8601 (e.g., PT5M) and shorthand (5m, 2h, 30s).
     */
    private Duration refreshInterval = Duration.ofMinutes(30); // default 30 minutes

    public Duration getRefreshInterval() { return refreshInterval; }
    public void setRefreshInterval(Duration refreshInterval) { this.refreshInterval = refreshInterval; }
}
