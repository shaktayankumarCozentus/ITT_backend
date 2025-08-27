package com.itt.service.fw.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditDetail(
        UUID traceId,
        String username,
        String roles,
        String httpMethod,
        String endpoint,
        String requestBody,
        String responseBody,
        String error,
        Instant start,
        long durationMs,
        int statusCode,
        String clientIp
) {}
