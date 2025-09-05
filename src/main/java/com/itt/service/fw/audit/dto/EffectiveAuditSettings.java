package com.itt.service.fw.audit.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class EffectiveAuditSettings {
    private boolean enabled;
    private boolean logRequest;
    private boolean logResponse;
    private boolean logError;
    private boolean onlyOnError;
    private Set<String> maskFields;
}
